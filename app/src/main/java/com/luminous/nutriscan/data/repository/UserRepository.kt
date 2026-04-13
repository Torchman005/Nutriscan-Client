package com.example.foodnutritionaiassistant.data.repository

import com.example.foodnutritionaiassistant.data.network.LoginRequest
import com.example.foodnutritionaiassistant.data.network.RetrofitClient
import com.example.foodnutritionaiassistant.ui.viewmodel.LoginType
import com.example.foodnutritionaiassistant.ui.viewmodel.UserProfile
import com.example.foodnutritionaiassistant.data.network.Feedback
import com.example.foodnutritionaiassistant.data.network.Tag
import retrofit2.Response

class UserRepository {

    private val api = RetrofitClient.apiService

    suspend fun checkUserExists(loginType: LoginType, identifier: String, passwordOrCode: String? = null): Boolean {
        return try {
            val request = LoginRequest(
                phoneNumber = if (loginType == LoginType.PHONE) identifier else null,
                wechatOpenId = if (loginType == LoginType.WECHAT) identifier else null,
                loginType = loginType.name,
                password = passwordOrCode
            )
            val response = api.login(request)
            response.isSuccessful && response.body() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUser(loginType: LoginType, identifier: String, passwordOrCode: String? = null): UserProfile? {
        return try {
            val request = LoginRequest(
                phoneNumber = if (loginType == LoginType.PHONE) identifier else null,
                wechatOpenId = if (loginType == LoginType.WECHAT) identifier else null,
                loginType = loginType.name,
                password = passwordOrCode
            )
            val response = api.login(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserById(id: String): UserProfile? {
        return try {
            val response = api.getUser(id)
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun registerUser(userProfile: UserProfile): Boolean {
        return try {
            val response = api.register(userProfile)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun registerUserRaw(userProfile: UserProfile): Response<UserProfile> {
        return api.register(userProfile)
    }

    suspend fun updateUserGroup(userProfile: UserProfile): Boolean {
        return updateUserProfile(userProfile)
    }

    suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        return try {
            if (userProfile.id != null) {
                val response = api.updateUser(userProfile.id!!, userProfile)
                response.isSuccessful
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun followUser(userId: String, targetUserId: String): Boolean {
        return try {
            val response = api.followUser(userId, targetUserId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unfollowUser(userId: String, targetUserId: String): Boolean {
        return try {
            val response = api.unfollowUser(userId, targetUserId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun submitFeedback(feedback: Feedback): Boolean {
        return try {
            val response = api.submitFeedback(feedback)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun addTag(userId: String, tagName: String): Boolean {
        return try {
            val response = api.addTag(Tag(userId = userId, tag = tagName))
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserTags(userId: String): List<String> {
        return try {
            val response = api.getUserTags(userId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.tag }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun removeTag(userId: String, tagName: String): Boolean {
        return try {
            val response = api.removeTag(userId, tagName)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getTodayStatistics(userId: String): com.example.foodnutritionaiassistant.data.network.UserStatistics? {
        return try {
            val response = api.getTodayStatistics(userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getWeekStatistics(userId: String): List<com.example.foodnutritionaiassistant.data.network.UserStatistics> {
        return try {
            val response = api.getWeekStatistics(userId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addManualRecord(
        userId: String,
        foodName: String,
        calories: Double,
        protein: Double = 0.0,
        fat: Double = 0.0,
        carbs: Double = 0.0
    ): Pair<Boolean, String?> {
        return try {
            val response = api.addCalorieRecord(
                com.example.foodnutritionaiassistant.data.network.CalorieRecordRequest(
                    userId = userId,
                    calories = calories
                )
            )
            if (response.isSuccessful) {
                true to null
            } else {
                val errorBody = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val message = errorBody ?: "HTTP ${response.code()}"
                false to message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false to (e.message ?: "Network error")
        }
    }

    suspend fun loginRaw(loginType: LoginType, identifier: String, passwordOrCode: String? = null): Response<UserProfile> {
        val request = LoginRequest(
            phoneNumber = if (loginType == LoginType.PHONE) identifier else null,
            wechatOpenId = if (loginType == LoginType.WECHAT) identifier else null,
            loginType = loginType.name,
            password = passwordOrCode
        )
        return api.login(request)
    }

    suspend fun updateUserWeight(userId: String, weight: Double): Boolean {
        return try {
            val response = api.updateWeight(userId, com.example.foodnutritionaiassistant.data.network.WeightUpdateRequest(weight))
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
