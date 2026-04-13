package com.example.foodnutritionaiassistant.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnutritionaiassistant.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import android.content.Context
import android.net.Uri

import com.example.foodnutritionaiassistant.data.network.Feedback
import com.example.foodnutritionaiassistant.data.storage.MinioHelper

enum class LoginType {
    WECHAT, PHONE
}

enum class GroupCategory(val displayName: String) {
    HEALTH("养生"),
    FITNESS("健身"),
    TODDLER("幼儿");

    companion object {
        fun fromName(name: String): GroupCategory {
            return entries.find { it.name == name } ?: FITNESS
        }
    }
}

data class UserProfile(
    val id: String? = null, 
    var nickname: String = "",
    var avatarUrl: String? = "",
    var gender: Int = 0, // 0:未知, 1:男, 2:女
    var birthDate: LocalDate = LocalDate.of(2000, 1, 1),
    var region: String? = "未填写", // 修改为可空类型，默认值为"未填写"
    var bio: String = "",
    var height: Float = 170f,
    var weight: Float = 60f,
    var targetWeight: Double? = null,
    var loginType: LoginType = LoginType.PHONE,
    var phoneNumber: String? = "",
    var wechatOpenId: String? = "",
    var groupCategory: GroupCategory = GroupCategory.FITNESS,
    var customTags: List<String> = emptyList(),
    var following: Set<String> = emptySet()
)

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    var userProfile by mutableStateOf(UserProfile())
        private set

    var phoneNumber by mutableStateOf("")
    var verificationCode by mutableStateOf("")
    var isCodeSent by mutableStateOf(false)
    var countdown by mutableStateOf(60)

    var isLoggedIn by mutableStateOf(false)
    var isFirstLogin by mutableStateOf(false) // 是否需要设置资料
    var isRefreshingProfile by mutableStateOf(false)
    var isCheckingLogin by mutableStateOf(false)

    var todayStatistics by mutableStateOf<com.example.foodnutritionaiassistant.data.network.UserStatistics?>(null)
    var weekStatistics by mutableStateOf<List<com.example.foodnutritionaiassistant.data.network.UserStatistics>>(emptyList())
    var isLoadingStatistics by mutableStateOf(false)

    var authError by mutableStateOf<String?>(null)

    fun refreshProfile() {
        if (userProfile.id == null) return
        viewModelScope.launch {
            isRefreshingProfile = true
            val result = withContext(Dispatchers.IO) {
                userRepository.getUserById(userProfile.id!!)
            }
            if (result != null) {
                userProfile = result
                val tags = withContext(Dispatchers.IO) {
                    userRepository.getUserTags(userProfile.id!!)
                }
                userProfile = userProfile.copy(customTags = tags)
            }
            isRefreshingProfile = false
        }
    }

    fun sendVerificationCode() {
        if (phoneNumber.isBlank()) return
        
        // Mock sending code (In real app, call API)
        viewModelScope.launch {
            verificationCode = "123456" 
            
            isCodeSent = true
            countdown = 60
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            isCodeSent = false
        }
    }

    fun checkLoginStatus(context: Context) {
        isCheckingLogin = true
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedUserId = sharedPrefs.getString("user_id", null)

        if (savedUserId != null) {
            // 乐观登录
            userProfile = userProfile.copy(id = savedUserId)
            isLoggedIn = true
            isFirstLogin = false

            viewModelScope.launch {
                try {
                    val user = withTimeoutOrNull(6000L) {
                        withContext(Dispatchers.IO) {
                            userRepository.getUserById(savedUserId)
                        }
                    }
                    if (user != null) {
                        val tags = withTimeoutOrNull(3000L) {
                            withContext(Dispatchers.IO) {
                                userRepository.getUserTags(user.id ?: "")
                            }
                        } ?: emptyList()
                        userProfile = user.copy(customTags = tags, region = user.region ?: "")
                        isLoggedIn = true
                        isFirstLogin = false
                    } else {
                        // 失败则还原状态
                        isLoggedIn = false
                        userProfile = UserProfile()
                    }
                } finally {
                    isCheckingLogin = false
                }
            }
        } else {
            isLoggedIn = false
            isFirstLogin = false
            isCheckingLogin = false
        }
    }

    fun loginWithPhone(context: Context, onSuccess: (Boolean) -> Unit) {
        if (phoneNumber.isBlank()) {
            authError = "请输入手机号"
            return
        }
        if (verificationCode.isBlank()) {
            authError = "请输入验证码"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val response = withTimeoutOrNull(6000L) {
                userRepository.loginRaw(LoginType.PHONE, phoneNumber, verificationCode)
            }
            withContext(Dispatchers.Main) {
                if (response == null) {
                    authError = "登录超时，请检查网络"
                    return@withContext
                }
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        val tags = withContext(Dispatchers.IO) {
                            userRepository.getUserTags(user.id ?: "")
                        }
                        userProfile = user.copy(customTags = tags, region = user.region ?: "")
                        isLoggedIn = true
                        isFirstLogin = false
                        saveLoginState(context, user.id)
                        authError = null
                        onSuccess(false)
                    } else {
                        authError = "登录失败，请稍后再试"
                    }
                    return@withContext
                }

                when (response.code()) {
                    401 -> authError = "验证码错误"
                    404 -> {
                        if (verificationCode != "123456") {
                            authError = "验证码错误"
                            return@withContext
                        }
                        userProfile = userProfile.copy(
                            loginType = LoginType.PHONE,
                            phoneNumber = phoneNumber,
                            region = userProfile.region ?: ""
                        )
                        isLoggedIn = true
                        isFirstLogin = true
                        authError = null
                        onSuccess(true)
                    }
                    else -> authError = "登录失败：${response.code()}"
                }
            }
        }
    }

    fun loginWithWeChat(context: Context, onSuccess: (Boolean) -> Unit) {
        val mockOpenId = "wx_123456"

        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.loginRaw(LoginType.WECHAT, mockOpenId)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        val tags = withContext(Dispatchers.IO) {
                            userRepository.getUserTags(user.id ?: "")
                        }
                        userProfile = user.copy(customTags = tags)
                        isLoggedIn = true
                        isFirstLogin = false
                        saveLoginState(context, user.id)
                        authError = null
                        onSuccess(false)
                    } else {
                        authError = "登录失败，请稍后再试"
                    }
                    return@withContext
                }

                when (response.code()) {
                    404 -> {
                        userProfile = userProfile.copy(
                            loginType = LoginType.WECHAT,
                            wechatOpenId = mockOpenId
                        )
                        isLoggedIn = true
                        isFirstLogin = true
                        authError = null
                        onSuccess(true)
                    }
                    else -> authError = "登录失败：${response.code()}"
                }
            }
        }
    }
    
    private fun saveLoginState(context: Context, userId: String?) {
        if (userId == null) return
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("user_id", userId)
            apply()
        }
    }

    fun updateNickname(name: String) {
        userProfile = userProfile.copy(nickname = name)
    }

    fun updateGender(gender: Int) {
        userProfile = userProfile.copy(gender = gender)
    }

    fun updateBirthDate(date: LocalDate) {
        userProfile = userProfile.copy(birthDate = date)
    }

    fun updateHeight(height: Float) {
        userProfile = userProfile.copy(height = height)
    }

    fun updateWeight(weight: Float) {
        userProfile = userProfile.copy(weight = weight)
    }

    fun updateGroupCategory(category: GroupCategory) {
        userProfile = userProfile.copy(groupCategory = category)
    }

    fun updateUserGroupInDb(category: GroupCategory) {
        userProfile = userProfile.copy(groupCategory = category)
        viewModelScope.launch(Dispatchers.IO) {
            val success = userRepository.updateUserGroup(userProfile)
            if (success) {
                println("User group updated successfully in DB")
            } else {
                println("Failed to update user group in DB")
            }
        }
    }

    fun submitProfile(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val hasId = !userProfile.id.isNullOrBlank()
            val success = if (hasId) {
                userRepository.updateUserProfile(userProfile)
            } else {
                userRepository.registerUser(userProfile)
            }
            if (success) {
                if (hasId) {
                    withContext(Dispatchers.Main) {
                        saveLoginState(context, userProfile.id)
                    }
                } else {
                    val user = userRepository.getUser(
                        userProfile.loginType,
                        if (userProfile.loginType == LoginType.PHONE) userProfile.phoneNumber ?: "" else userProfile.wechatOpenId ?: "",
                        if (userProfile.loginType == LoginType.PHONE) "123456" else null
                    )

                    if (user?.id != null) {
                        withContext(Dispatchers.Main) {
                            userProfile = user
                            saveLoginState(context, user.id)
                        }
                    }
                }
            } else {
                println("Failed to persist user profile")
            }
            withContext(Dispatchers.Main) {
                isFirstLogin = false
            }
        }
    }

    fun logout(context: Context) {
        isLoggedIn = false
        userProfile = UserProfile()
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove("user_id")
            apply()
        }
    }

    fun updateUserProfile(
        nickname: String,
        gender: Int,
        birthDate: LocalDate,
        region: String?,
        bio: String?,
        avatarUri: Uri?,
        context: Context,
        height: Float? = null,
        weight: Float? = null,
        groupCategory: GroupCategory? = null,
        targetWeight: Double? = null,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. 上传新头像
            var newAvatarUrl = userProfile.avatarUrl
            if (avatarUri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(avatarUri)
                    if (inputStream != null) {
                        val fileName = "avatars/${java.util.UUID.randomUUID()}.jpg"
                        val url = MinioHelper.uploadImage(inputStream, fileName)
                        if (url != null) {
                            newAvatarUrl = url
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. 更新本地状态
            val updatedProfile = userProfile.copy(
                nickname = nickname,
                gender = gender,
                birthDate = birthDate,
                region = region ?: "",
                bio = bio ?: "",
                avatarUrl = newAvatarUrl,
                phoneNumber = userProfile.phoneNumber ?: "",
                height = height ?: userProfile.height,
                weight = weight ?: userProfile.weight,
                groupCategory = groupCategory ?: userProfile.groupCategory,
                targetWeight = targetWeight ?: userProfile.targetWeight
            )

            // 3. 更新数据库
            val success = userRepository.updateUserProfile(updatedProfile)
            if (success) {
                withContext(Dispatchers.Main) {
                    userProfile = updatedProfile
                    onResult(true)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun updateTargetWeightInDb(targetWeight: Double, onResult: (Boolean) -> Unit) {
        if (userProfile.id == null) {
            onResult(false)
            return
        }
        val updatedProfile = userProfile.copy(targetWeight = targetWeight)
        viewModelScope.launch(Dispatchers.IO) {
            val success = userRepository.updateUserProfile(updatedProfile)
            withContext(Dispatchers.Main) {
                if (success) {
                    userProfile = updatedProfile
                }
                onResult(success)
            }
        }
    }

    fun addCustomTag(tag: String) {
        if (tag.isBlank() || userProfile.customTags.contains(tag)) return
        
        val newTags = userProfile.customTags + tag
        userProfile = userProfile.copy(customTags = newTags)
        
        viewModelScope.launch {
            val success = userRepository.addTag(userProfile.id ?: "", tag)
            if (!success) {
                val revertedTags = userProfile.customTags - tag
                userProfile = userProfile.copy(customTags = revertedTags)
            }
        }
    }

    fun removeCustomTag(tag: String) {
         if (tag.isBlank()) return

        val newTags = userProfile.customTags - tag
        userProfile = userProfile.copy(customTags = newTags)
        
        viewModelScope.launch {
            val success = userRepository.removeTag(userProfile.id ?: "", tag)
            if (!success) {
                val revertedTags = userProfile.customTags + tag
                userProfile = userProfile.copy(customTags = revertedTags)
            }
        }
    }

    fun addManualCalorieRecord(
        foodName: String,
        calories: Double,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (userProfile.id == null) {
            onResult(false, "未登录，无法保存记录")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val (success, message) = userRepository.addManualRecord(userProfile.id!!, foodName, calories)
            withContext(Dispatchers.Main) {
                if (success) {
                    fetchTodayStatistics()
                    fetchWeekStatistics()
                    onResult(true, null)
                } else {
                    onResult(false, message ?: "保存失败，请检查网络或后端服务")
                }
            }
        }
    }

    fun fetchTodayStatistics() {
        if (userProfile.id == null) return

        viewModelScope.launch {
            isLoadingStatistics = true
            val stats = withContext(Dispatchers.IO) {
                userRepository.getTodayStatistics(userProfile.id!!)
            }
            todayStatistics = stats
            isLoadingStatistics = false
        }
    }

    fun fetchWeekStatistics() {
        if (userProfile.id == null) return

        viewModelScope.launch {
            isLoadingStatistics = true
            val stats = withContext(Dispatchers.IO) {
                userRepository.getWeekStatistics(userProfile.id!!)
            }
            weekStatistics = stats
            if (todayStatistics == null && weekStatistics.isNotEmpty()) {
                todayStatistics = weekStatistics.last()
            }
            isLoadingStatistics = false
        }
    }

    fun submitFeedback(
        content: String,
        contactInfo: String?,
        type: String,
        onResult: (Boolean) -> Unit
    ) {
        if (userProfile.id == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val feedback = Feedback(
                userId = userProfile.id!!,
                userNickname = userProfile.nickname,
                content = content,
                contactInfo = contactInfo,
                type = type
            )
            val success = withContext(Dispatchers.IO) {
                userRepository.submitFeedback(feedback)
            }
            onResult(success)
        }
    }

    fun followOrUnfollowUser(targetUserId: String) {
        val currentUserId = userProfile.id ?: return
        val isFollowing = userProfile.following.contains(targetUserId)

        val newFollowing = if (isFollowing) {
            userProfile.following - targetUserId
        } else {
            userProfile.following + targetUserId
        }
        userProfile = userProfile.copy(following = newFollowing)

        viewModelScope.launch {
            val success = if (isFollowing) {
                userRepository.unfollowUser(currentUserId, targetUserId)
            } else {
                userRepository.followUser(currentUserId, targetUserId)
            }

            if (!success) {
                val revertedFollowing = if (isFollowing) {
                    userProfile.following + targetUserId
                } else {
                    userProfile.following - targetUserId
                }
                userProfile = userProfile.copy(following = revertedFollowing)
            }
        }
    }

    fun startPhoneRegistration(onResult: (Boolean, String?) -> Unit) {
        if (phoneNumber.isBlank()) {
            onResult(false, "请输入手机号")
            return
        }
        if (verificationCode.isBlank()) {
            onResult(false, "请输入验证码")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.loginRaw(LoginType.PHONE, phoneNumber, verificationCode)
            withContext(Dispatchers.Main) {
                when (response.code()) {
                    200 -> onResult(false, "手机号已注册，请直接登录")
                    401 -> onResult(false, "验证码错误")
                    404 -> {
                        val seedProfile = userProfile.copy(
                            loginType = LoginType.PHONE,
                            phoneNumber = phoneNumber,
                            nickname = if (userProfile.nickname.isBlank()) "新用户" else userProfile.nickname
                        )
                        viewModelScope.launch(Dispatchers.IO) {
                            val registerResponse = userRepository.registerUserRaw(seedProfile)
                            withContext(Dispatchers.Main) {
                                if (registerResponse.isSuccessful && registerResponse.body() != null) {
                                    userProfile = registerResponse.body()!!
                                    isLoggedIn = true
                                    isFirstLogin = true
                                    onResult(true, null)
                                } else if (registerResponse.code() == 409) {
                                    onResult(false, "手机号已注册，请直接登录")
                                } else {
                                    onResult(false, "注册失败：${registerResponse.code()}")
                                }
                            }
                        }
                    }
                    else -> onResult(false, "注册失败：${response.code()}")
                }
            }
        }
    }

    fun updateWeightInDb(weight: Float, onResult: (Boolean) -> Unit) {
        if (userProfile.id == null) {
            onResult(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = userRepository.updateUserWeight(userProfile.id!!, weight.toDouble())
            withContext(Dispatchers.Main) {
                if (success) {
                    userProfile = userProfile.copy(weight = weight)
                }
                onResult(success)
            }
        }
    }
}
