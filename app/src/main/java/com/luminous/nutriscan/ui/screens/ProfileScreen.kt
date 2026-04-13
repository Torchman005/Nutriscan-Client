package com.example.foodnutritionaiassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale

import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.Period

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.foodnutritionaiassistant.ui.viewmodel.GroupCategory

import com.example.foodnutritionaiassistant.ui.viewmodel.CommunityViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.LaunchedEffect
import com.example.foodnutritionaiassistant.data.model.Post
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyPostsScreen(
    userProfile: com.example.foodnutritionaiassistant.ui.viewmodel.UserProfile,
    communityViewModel: CommunityViewModel,
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedPosts = remember { androidx.compose.runtime.mutableStateListOf<String>() }
    val selectedPostForDetailState = remember { mutableStateOf<Post?>(null) }
    var selectedPostForDetail by selectedPostForDetailState
    
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(userProfile.id) {
        userProfile.id?.let { communityViewModel.loadMyPosts(it) }
    }

    LaunchedEffect(Unit) {
        communityViewModel.searchQuery = ""
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        IconButton(onClick = { 
                            isSelectionMode = false 
                            selectedPosts.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Selection")
                        }
                        Text(
                            text = "已选择 ${selectedPosts.size} 项",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (selectedPosts.isNotEmpty()) {
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color.Red)
                            }
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "我的发布",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (!isSelectionMode) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = communityViewModel.searchQuery,
                            onValueChange = { communityViewModel.searchQuery = it },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            placeholder = { Text("搜索我的发布...", color = Color.Gray, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF81C784),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5)
                            )
                        )
                    }
                }
            }

            if (communityViewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val displayPosts = communityViewModel.getFilteredMyPosts()
                androidx.compose.foundation.lazy.LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayPosts.size) { index ->
                        val post = displayPosts[index]
                        val isSelected = selectedPosts.contains(post.id)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CommunityPostItem(
                                post = post,
                                onClick = {
                                    if (isSelectionMode) {
                                        if (isSelected) {
                                            post.id?.let { selectedPosts.remove(it) }
                                        } else {
                                            post.id?.let { selectedPosts.add(it) }
                                        }
                                    } else {
                                        selectedPostForDetail = post
                                    }
                                },
                                onLike = {},
                                onFavorite = {},
                                currentUserId = userProfile.id ?: "",
                                showFavoriteIcon = false
                            )
                            
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .combinedClickable(
                                        onClick = {
                                            if (isSelectionMode) {
                                                if (isSelected) {
                                                    post.id?.let { selectedPosts.remove(it) }
                                                } else {
                                                    post.id?.let { selectedPosts.add(it) }
                                                }
                                            } else {
                                                selectedPostForDetail = post
                                            }
                                        },
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                post.id?.let { selectedPosts.add(it) }
                                            }
                                        }
                                    )
                                    .background(if (isSelected) Color.Black.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(8.dp))
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF66BB6A),
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        selectedPostForDetail?.let { post ->
            val latestPost = communityViewModel.myPosts.find { it.id == post.id } ?: post
            PostDetailDialog(
                post = latestPost,
                viewModel = communityViewModel,
                userViewModel = viewModel,
                onDismiss = { selectedPostForDetail = null }
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("确认删除") },
                text = { Text("确定要删除这 ${selectedPosts.size} 条帖子吗？删除后无法恢复。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            communityViewModel.deletePosts(selectedPosts.toList())
                            showDeleteConfirmDialog = false
                            isSelectionMode = false
                            selectedPosts.clear()
                            android.widget.Toast.makeText(context, "删除成功", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("删除", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    communityViewModel: CommunityViewModel = viewModel(),
    onLoginClick: () -> Unit
) {
    val userProfile = viewModel.userProfile
    val isLoggedIn = viewModel.isLoggedIn
    val context = LocalContext.current

    if (!isLoggedIn) {
        LoginPrompt(onLoginClick)
    } else {
        ProfileContent(viewModel, communityViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(viewModel: UserViewModel, communityViewModel: CommunityViewModel) {
    val userProfile = viewModel.userProfile
    val age = Period.between(userProfile.birthDate, LocalDate.now()).years
    var showGroupSelectionDialog by remember { mutableStateOf(false) }
    var showTargetWeightDialog by remember { mutableStateOf(false) }
    var targetWeightInput by remember { mutableStateOf("") }
    var targetWeightError by remember { mutableStateOf<String?>(null) }
    var isUpdatingTargetWeight by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }
    var heightInput by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf<String?>(null) }
    var isUpdatingHeight by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    var weightError by remember { mutableStateOf<String?>(null) }
    var isUpdatingWeight by remember { mutableStateOf(false) }
    var showBirthDateDialog by remember { mutableStateOf(false) }
    var isUpdatingBirthDate by remember { mutableStateOf(false) }
    val birthDateMillis = remember(userProfile.birthDate) {
        userProfile.birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = birthDateMillis)
    var showFavorites by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showMyPosts by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHelpCenter by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    val selectedFavoritePostState = remember { mutableStateOf<Post?>(null) }
    var selectedFavoritePost by selectedFavoritePostState

    val selectedHistoryPostState = remember { mutableStateOf<Post?>(null) }
    var selectedHistoryPost by selectedHistoryPostState

    val context = LocalContext.current
    val saveProfileUpdate: (Float?, Float?, LocalDate?, (Boolean) -> Unit) -> Unit = { height, weight, birthDate, onResult ->
        viewModel.updateUserProfile(
            nickname = userProfile.nickname,
            gender = userProfile.gender,
            birthDate = birthDate ?: userProfile.birthDate,
            region = userProfile.region,
            bio = userProfile.bio,
            avatarUri = null,
            context = context,
            height = height,
            weight = weight,
            groupCategory = userProfile.groupCategory,
            targetWeight = userProfile.targetWeight,
            onResult = onResult
        )
    }

    if (showFavorites) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showFavorites = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text("我的收藏", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = communityViewModel.searchQuery,
                            onValueChange = { communityViewModel.searchQuery = it },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            placeholder = { Text("搜索收藏...", color = Color.Gray, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF81C784),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5)
                            )
                        )
                    }
                }
                
                LaunchedEffect(userProfile.id) {
                    communityViewModel.searchQuery = ""
                    userProfile.id?.let { communityViewModel.loadFavoritePosts(it) }
                }
                
                if (communityViewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        val currentUserId = userProfile.id ?: ""
                        val allFavoritePosts = communityViewModel.getFilteredFavoritePosts()
                        val displayPosts = allFavoritePosts.filter { 
                            it.favoritedUserIds.contains(currentUserId)
                        }

                        items(displayPosts.size, key = { index -> displayPosts[index].id ?: index }) { index ->
                            val post = displayPosts[index]
                            CommunityPostItem(
                                post = post,
                                onClick = { selectedFavoritePost = post },
                                onLike = { p -> 
                                    communityViewModel.toggleLike(p, currentUserId) 
                                },
                                onFavorite = { p -> 
                                    communityViewModel.toggleFavorite(p, currentUserId)
                                },
                                currentUserId = currentUserId,
                                showFavoriteIcon = false
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            
            selectedFavoritePost?.let { post ->
                val latestPost = communityViewModel.favoritePosts.find { it.id == post.id } ?: post

                PostDetailDialog(
                    post = latestPost,
                    viewModel = communityViewModel,
                    userViewModel = viewModel,
                    onDismiss = { selectedFavoritePost = null }
                )
            }
        }
        return
    }
    
    if (showHistory) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showHistory = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text("浏览历史", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        if (communityViewModel.historyPosts.isNotEmpty()) {
                            IconButton(onClick = {
                                showClearHistoryDialog = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = Color.Gray)
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = communityViewModel.searchQuery,
                            onValueChange = { communityViewModel.searchQuery = it },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            placeholder = { Text("搜索浏览历史...", color = Color.Gray, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF81C784),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5)
                            )
                        )
                    }
                }
                
                LaunchedEffect(userProfile.id) {
                    communityViewModel.searchQuery = ""
                    userProfile.id?.let { communityViewModel.loadViewHistory(it) }
                }
                
                if (communityViewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        val currentUserId = userProfile.id ?: ""
                        val displayPosts = communityViewModel.getFilteredHistoryPosts()
                        if (displayPosts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("暂无浏览历史", color = Color.Gray)
                                }
                            }
                        } else {
                            items(displayPosts.size) { index ->
                                val post = displayPosts[index]
                                CommunityPostItem(
                                    post = post,
                                    onClick = { selectedHistoryPost = post },
                                    onLike = { p -> communityViewModel.toggleLike(p, currentUserId) },
                                    onFavorite = { p -> communityViewModel.toggleFavorite(p, currentUserId) },
                                    currentUserId = currentUserId,
                                    showFavoriteIcon = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
            
            selectedHistoryPost?.let { post ->
                val latestPost = communityViewModel.historyPosts.find { it.id == post.id } ?: post
                PostDetailDialog(
                    post = latestPost,
                    viewModel = communityViewModel,
                    userViewModel = viewModel,
                    onDismiss = { selectedHistoryPost = null }
                )
            }

            if (showClearHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { showClearHistoryDialog = false },
                    title = { Text("确认清空") },
                    text = { Text("确定要清空所有浏览历史吗？") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                userProfile.id?.let { communityViewModel.clearViewHistory(it) }
                                showClearHistoryDialog = false
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearHistoryDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }
        }
        return
    }

    if (showMyPosts) {
        MyPostsScreen(
            userProfile = userProfile,
            communityViewModel = communityViewModel,
            viewModel = viewModel,
            onBack = { showMyPosts = false }
        )
        return
    }

    if (showSettings) {
        SettingsScreen(
            viewModel = viewModel,
            onBack = { showSettings = false }
        )
        return
    }

    if (showHelpCenter) {
        HelpCenterScreen(
            onBack = { showHelpCenter = false }
        )
        return
    }

    if (showFeedback) {
        FeedbackScreen(
            viewModel = viewModel,
            onBack = { showFeedback = false }
        )
        return
    }

    val refreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshingProfile,
        onRefresh = { viewModel.refreshProfile() },
        state = refreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF81C784)),
                contentAlignment = Alignment.Center
            ) {
                if (!userProfile.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(userProfile.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Hello, ${userProfile.nickname}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                val bioText = if (userProfile.bio.isBlank()) "开始今天的美好之旅吧~" else userProfile.bio
                Text(bioText, fontSize = 14.sp, color = Color.Gray)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF66BB6A))
            Spacer(modifier = Modifier.width(8.dp))
            Text("个人数据", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.clickable {
                heightInput = userProfile.height.toString()
                heightError = null
                showHeightDialog = true
            }) {
                StatCard("${userProfile.height}cm", "身高")
            }
            Box(modifier = Modifier.clickable {
                weightInput = userProfile.weight.toString()
                weightError = null
                showWeightDialog = true
            }) {
                StatCard("${userProfile.weight}kg", "体重")
            }
            Box(modifier = Modifier.clickable {
                showBirthDateDialog = true
            }) {
                StatCard("${age}岁", "年龄")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(modifier = Modifier.clickable {
                targetWeightInput = userProfile.targetWeight?.toString() ?: ""
                targetWeightError = null
                showTargetWeightDialog = true
            }) {
                val targetWeightText = userProfile.targetWeight?.let { "${String.format("%.1f", it)}kg" } ?: "--"
                StatCard(targetWeightText, "目标体重")
            }
        }

        if (showTargetWeightDialog) {
            AlertDialog(
                onDismissRequest = { if (!isUpdatingTargetWeight) showTargetWeightDialog = false },
                title = { Text("设置目标体重") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = targetWeightInput,
                            onValueChange = {
                                targetWeightInput = it
                                targetWeightError = null
                            },
                            label = { Text("目标体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = targetWeightError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (targetWeightError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(targetWeightError ?: "", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val value = targetWeightInput.toDoubleOrNull()
                            if (value == null || value <= 0.0) {
                                targetWeightError = "请输入有效的目标体重"
                                return@TextButton
                            }
                            isUpdatingTargetWeight = true
                            viewModel.updateUserProfile(
                                nickname = userProfile.nickname,
                                gender = userProfile.gender,
                                birthDate = userProfile.birthDate,
                                region = userProfile.region,
                                bio = userProfile.bio,
                                avatarUri = null,
                                context = context,
                                height = null,
                                weight = null,
                                groupCategory = userProfile.groupCategory,
                                targetWeight = value,
                                onResult = { success ->
                                    isUpdatingTargetWeight = false
                                    if (success) {
                                        showTargetWeightDialog = false
                                    } else {
                                        targetWeightError = "保存失败，请稍后再试"
                                    }
                                }
                            )
                        },
                        enabled = !isUpdatingTargetWeight
                    ) {
                        Text(if (isUpdatingTargetWeight) "保存中..." else "保存")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTargetWeightDialog = false },
                        enabled = !isUpdatingTargetWeight
                    ) {
                        Text("取消")
                    }
                }
            )
        }

        if (showBirthDateDialog) {
            DatePickerDialog(
                onDismissRequest = { if (!isUpdatingBirthDate) showBirthDateDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis == null) return@TextButton
                            isUpdatingBirthDate = true
                            val selectedDate = java.time.Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            saveProfileUpdate(null, null, selectedDate) { success ->
                                isUpdatingBirthDate = false
                                if (success) {
                                    showBirthDateDialog = false
                                }
                            }
                        },
                        enabled = !isUpdatingBirthDate
                    ) {
                        Text(if (isUpdatingBirthDate) "保存中..." else "保存")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBirthDateDialog = false },
                        enabled = !isUpdatingBirthDate
                    ) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showHeightDialog) {
            AlertDialog(
                onDismissRequest = { if (!isUpdatingHeight) showHeightDialog = false },
                title = { Text("设置身高") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = {
                                heightInput = it
                                heightError = null
                            },
                            label = { Text("身高 (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = heightError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (heightError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(heightError ?: "", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val value = heightInput.toFloatOrNull()
                            if (value == null || value <= 0f) {
                                heightError = "请输入有效的身高"
                                return@TextButton
                            }
                            isUpdatingHeight = true
                            saveProfileUpdate(value, null, null) { success ->
                                isUpdatingHeight = false
                                if (success) {
                                    showHeightDialog = false
                                } else {
                                    heightError = "保存失败，请稍后再试"
                                }
                            }
                        },
                        enabled = !isUpdatingHeight
                    ) {
                        Text(if (isUpdatingHeight) "保存中..." else "保存")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showHeightDialog = false },
                        enabled = !isUpdatingHeight
                    ) {
                        Text("取消")
                    }
                }
            )
        }

        if (showWeightDialog) {
            AlertDialog(
                onDismissRequest = { if (!isUpdatingWeight) showWeightDialog = false },
                title = { Text("设置体重") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = {
                                weightInput = it
                                weightError = null
                            },
                            label = { Text("体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = weightError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (weightError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(weightError ?: "", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val value = weightInput.toFloatOrNull()
                            if (value == null || value <= 0f) {
                                weightError = "请输入有效的体重"
                                return@TextButton
                            }
                            isUpdatingWeight = true
                            showWeightDialog = false
                            viewModel.updateWeightInDb(value) { success ->
                                isUpdatingWeight = false
                                if (!success) {
                                    weightError = "保存失败，请稍后再试"
                                }
                            }
                        },
                        enabled = !isUpdatingWeight
                    ) {
                        Text(if (isUpdatingWeight) "保存中..." else "保存")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showWeightDialog = false },
                        enabled = !isUpdatingWeight
                    ) {
                        Text("取消")
                    }
                }
            )
        }

        // Group Field (Read Only)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showGroupSelectionDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("所属群体", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                
                Surface(
                    color = when(userProfile.groupCategory) {
                        GroupCategory.HEALTH -> Color(0xFFA5D6A7)
                        GroupCategory.FITNESS -> Color(0xFF90CAF9)
                        GroupCategory.TODDLER -> Color(0xFFFFF59D)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = userProfile.groupCategory.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = if (userProfile.groupCategory == GroupCategory.TODDLER) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        if (showGroupSelectionDialog) {
            AlertDialog(
                onDismissRequest = { showGroupSelectionDialog = false },
                title = { Text("修改所属群体") },
                text = {
                    Column {
                        GroupCategory.entries.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateUserGroupInDb(category)
                                        showGroupSelectionDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = userProfile.groupCategory == category,
                                    onClick = {
                                        viewModel.updateUserGroupInDb(category)
                                        showGroupSelectionDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category.displayName)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showGroupSelectionDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("常用工具", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        ToolItem("我的发布", Icons.Default.Edit, onClick = { showMyPosts = true })
        ToolItem("我的设置", Icons.Default.Settings, onClick = { showSettings = true })
        ToolItem("我的收藏", Icons.Default.Star, onClick = { showFavorites = true })
        ToolItem("浏览历史", Icons.Default.DateRange, onClick = { showHistory = true })
        ToolItem("帮助中心", Icons.Default.Info, onClick = { showHelpCenter = true })
        ToolItem("问题反馈", Icons.Default.Email, onClick = { showFeedback = true })
        }
    }
}

@Composable
fun StatCard(value: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ToolItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 16.sp)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}
