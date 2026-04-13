package com.example.foodnutritionaiassistant.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.compose.AsyncImagePainter
import com.example.foodnutritionaiassistant.data.model.GroupCategory
import com.example.foodnutritionaiassistant.data.model.Post
import com.example.foodnutritionaiassistant.data.model.Comment
import com.example.foodnutritionaiassistant.ui.viewmodel.CommunityViewModel
import com.example.foodnutritionaiassistant.ui.viewmodel.UserViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import java.text.SimpleDateFormat
import java.util.Locale

import android.widget.Toast
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.util.lerp
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

// 显式消除使用试验性API的报错或警告
@OptIn(ExperimentalMaterial3Api::class)
// Composable注解标注的函数代表这是一个UI树
@Composable
fun CommunityScreen(
    userViewModel: UserViewModel,
// 默认参数函数，当调用该函数时未传递viewModel参数时，会使用默认值
    viewModel: CommunityViewModel = viewModel()
) {
    val tabs = GroupCategory.entries
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    val refreshState = rememberPullToRefreshState()
    
    LaunchedEffect(Unit) {
        if (!viewModel.isCategoryInitialized) {
            val userGroup = userViewModel.userProfile.groupCategory
            val targetCategory = when (userGroup.name) {
                "HEALTH" -> GroupCategory.WELLNESS
                "FITNESS" -> GroupCategory.FITNESS
                "TODDLER" -> GroupCategory.TODDLER
                else -> GroupCategory.WELLNESS
            }
            viewModel.loadPosts(targetCategory, reset = true, userId = userViewModel.userProfile.id)
            viewModel.isCategoryInitialized = true
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    placeholder = { Text("搜索感兴趣的内容", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF81C784),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.refreshPosts(userViewModel.userProfile.id)
                        }
                    ),
                    trailingIcon = {
                         if (viewModel.searchQuery.isNotEmpty()) {
                             IconButton(onClick = { viewModel.refreshPosts(userViewModel.userProfile.id) }) {
                                 Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF66BB6A))
                             }
                         }
                    }
                )
            }
            
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(viewModel.currentCategory),
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                divider = {},
                indicator = {}
            ) {
                tabs.forEach { category ->
                    val isSelected = viewModel.currentCategory == category
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.loadPosts(category, reset = true, userId = userViewModel.userProfile.id) },
                        text = {
                            Text(
                                text = category.displayName,
                                fontSize = if (isSelected) 18.sp else 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.Gray
                            )
                        }
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = viewModel.isFollowedOnly,
                    onClick = { viewModel.toggleFollowedOnly(userViewModel.userProfile.id) },
                    label = { Text("已关注") },
                    leadingIcon = if (viewModel.isFollowedOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F5E9),
                        selectedLabelColor = Color(0xFF66BB6A),
                        selectedLeadingIconColor = Color(0xFF66BB6A)
                    )
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { viewModel.setSortOrder("createdAt", "desc", userViewModel.userProfile.id) }
                    ) {
                        Text("最新", fontWeight = if (viewModel.sortBy == "createdAt") FontWeight.Bold else FontWeight.Normal,
                             color = if (viewModel.sortBy == "createdAt") Color(0xFF66BB6A) else Color.Gray)
                    }
                    TextButton(
                        onClick = { viewModel.setSortOrder("likeCount", "desc", userViewModel.userProfile.id) }
                    ) {
                        Text("最热", fontWeight = if (viewModel.sortBy == "likeCount") FontWeight.Bold else FontWeight.Normal,
                             color = if (viewModel.sortBy == "likeCount") Color(0xFF66BB6A) else Color.Gray)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PullToRefreshBox(
                isRefreshing = viewModel.isLoading,
                onRefresh = { viewModel.refreshPosts(userViewModel.userProfile.id) },
                state = refreshState,
                modifier = Modifier.weight(1f)
            ) {
                if (viewModel.posts.isEmpty() && viewModel.isLoading && viewModel.currentPage == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF66BB6A))
                    }
                } else {
                    val displayPosts = viewModel.getFilteredPosts()
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayPosts) { post ->
                            CommunityPostItem(
                                post = post, 
                                onClick = { 
                                    Log.d("CommunityScreen", "Post clicked: ${post.id}")
                                    selectedPost = post 
                                },
                                onLike = { p -> viewModel.toggleLike(p, userViewModel.userProfile.id ?: "") },
                                onFavorite = { p -> viewModel.toggleFavorite(p, userViewModel.userProfile.id ?: "") },
                                currentUserId = userViewModel.userProfile.id ?: "",
                                currentUserAvatar = userViewModel.userProfile.avatarUrl,
                                isFollowing = userViewModel.userProfile.following.contains(post.authorId)
                            )
                        }
                        
                        if (!viewModel.isLastPage && displayPosts.isNotEmpty()) {
                             item(span = StaggeredGridItemSpan.FullLine) {
                                 LaunchedEffect(Unit) {
                                     viewModel.loadPosts(viewModel.currentCategory, reset = false, userId = userViewModel.userProfile.id)
                                 }
                                 Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                     CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF66BB6A))
                                 }
                             }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreatePostDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF66BB6A),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Post")
        }

        if (showCreatePostDialog) {
            CreatePostDialog(
                viewModel = viewModel,
                userViewModel = userViewModel,
                onDismiss = { 
                    if (viewModel.postTitle.isNotBlank() || viewModel.postContent.isNotBlank()) {
                         viewModel.saveDraft(
                             authorId = userViewModel.userProfile.id ?: "temp_id",
                             authorNickname = userViewModel.userProfile.nickname
                         )
                    }
                    showCreatePostDialog = false 
                },
                onSuccess = { showCreatePostDialog = false }
            )
        }
        
        selectedPost?.let { post ->
            Log.d("CommunityScreen", "Showing details for post: ${post.id}")
            val latestPost = viewModel.posts.find { it.id == post.id } ?: post
            
            LaunchedEffect(latestPost.id) {
                latestPost.id?.let { viewModel.recordView(it, userViewModel.userProfile.id ?: "") }
            }
            
            PostDetailDialog(
                post = latestPost,
                viewModel = viewModel,
                userViewModel = userViewModel,
                onDismiss = { 
                    Log.d("CommunityScreen", "Dismissing post details")
                    selectedPost = null 
                }
            )
        }
    }
}

@Composable
fun CommunityPostItem(
    post: Post,
    onClick: () -> Unit,
    onLike: (Post) -> Unit = {},
    onFavorite: (Post) -> Unit = {},
    currentUserId: String = "",
    currentUserAvatar: String? = null,
    showFavoriteIcon: Boolean = true,
    isFollowing: Boolean = false
) {
    val isLiked = post.likedUserIds.contains(currentUserId)
    val isFavorited = post.favoritedUserIds.contains(currentUserId)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            val imageUrl = post.images.firstOrNull()
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .listener(
                            onSuccess = { _, _ -> android.util.Log.d("CommunityScreen", "Image loaded successfully: $imageUrl") },
                            onError = { _, result -> android.util.Log.e("CommunityScreen", "Image load failed: $imageUrl", result.throwable) }
                        )
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 250.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                         val avatarUrl = if (!post.authorAvatar.isNullOrEmpty()) {
                             post.authorAvatar
                         } else if (post.authorId == currentUserId && !currentUserAvatar.isNullOrEmpty()) {
                             currentUserAvatar
                         } else {
                             null
                         }
                         
                         if (avatarUrl != null) {
                             AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                 contentScale = ContentScale.Crop,
                                 onLoading = { Log.d("CommunityScreen", "Loading avatar: $avatarUrl") },
                                 onError = { Log.e("CommunityScreen", "Error loading avatar: $avatarUrl") }
                            )
                         } else {
                             Box(
                                 modifier = Modifier
                                     .size(16.dp)
                                     .clip(CircleShape)
                                     .background(Color.LightGray),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     imageVector = Icons.Default.Person,
                                     contentDescription = null,
                                     tint = Color.White,
                                     modifier = Modifier.size(12.dp)
                                 )
                             }
                         }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.authorName, 
                            fontSize = 10.sp, 
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isFollowing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "已关注",
                                fontSize = 8.sp,
                                color = Color(0xFF66BB6A),
                                modifier = Modifier
                                    .border(1.dp, Color(0xFF66BB6A), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 2.dp, vertical = 0.dp)
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp).clickable { onLike(post) }, 
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(post.likeCount.toString(), fontSize = 10.sp, color = Color.Gray)
                        
                        if (showFavoriteIcon) {
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).clickable { onFavorite(post) },
                                tint = if (isFavorited) Color(0xFFFFC107) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreatePostDialog(
    viewModel: CommunityViewModel,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    isEdit: Boolean = false,
    postToEdit: Post? = null
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        if (isEdit && postToEdit != null) {
            viewModel.prepareEditPost(postToEdit)
        } else {
            viewModel.selectedGroup = viewModel.currentCategory
        }
        viewModel.resetPublishStatus()
    }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris ->
        uris.forEach { uri ->
            viewModel.addImage(uri.toString())
        }
    }
    
    val defaultTags = listOf("减肥", "增肌", "早餐", "低卡", "高蛋白", "瑜伽", "宝宝辅食", "养生茶")
    val usedTags = (defaultTags + (userViewModel.userProfile.customTags)).distinct()
    
    var showDeleteTagDialog by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(viewModel.publishSuccess) {
        if (viewModel.publishSuccess == true) {
            Toast.makeText(context, if (isEdit) "修改成功" else "发布成功", Toast.LENGTH_SHORT).show()
            
            viewModel.postTags.forEach { tag ->
                if (!defaultTags.contains(tag)) {
                    userViewModel.addCustomTag(tag)
                }
            }
            
            onSuccess()
        } else if (viewModel.publishSuccess == false) {
             Toast.makeText(context, if (isEdit) "修改失败，请重试" else "发布失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Text(if (isEdit) "编辑笔记" else "发布笔记", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = { 
                             if (isEdit && postToEdit != null) {
                                 viewModel.updatePost(
                                     context = context,
                                     postId = postToEdit.id ?: "",
                                     authorId = userViewModel.userProfile.id ?: "temp_id",
                                     authorNickname = userViewModel.userProfile.nickname,
                                     authorAvatar = userViewModel.userProfile.avatarUrl
                                 )
                             } else {
                                 viewModel.publishPost(
                                     context = context,
                                     authorId = userViewModel.userProfile.id ?: "temp_id",
                                     authorNickname = userViewModel.userProfile.nickname,
                                     authorAvatar = userViewModel.userProfile.avatarUrl
                                 )
                             }
                        },
                        enabled = !viewModel.isPublishing
                    ) {
                        if (viewModel.isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isEdit) "保存" else "发布", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .clickable { 
                                     photoPickerLauncher.launch(
                                         PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                     )
                                }
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Image", tint = Color.Gray)
                        }
                        
                        viewModel.postImages.forEach { uri ->
                            Box(modifier = Modifier.size(100.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                )
                                IconButton(
                                    onClick = { viewModel.removeImage(uri) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = viewModel.postTitle,
                        onValueChange = { viewModel.postTitle = it },
                        placeholder = { Text("填写标题会有更多人赞哦~") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Divider(color = Color(0xFFEEEEEE))

                    OutlinedTextField(
                        value = viewModel.postContent,
                        onValueChange = { viewModel.postContent = it },
                        placeholder = { Text("添加正文") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    
                    Divider(color = Color(0xFFEEEEEE))
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("添加标签", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.currentTagInput,
                            onValueChange = { viewModel.currentTagInput = it },
                            // placeholder = { Text("输入自定义标签") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.addTag(viewModel.currentTagInput) },
                            enabled = viewModel.currentTagInput.isNotBlank(),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("添加")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val allTags = listOf("减肥", "增肌", "早餐", "低卡", "高蛋白", "瑜伽", "宝宝辅食", "养生茶") + userViewModel.userProfile.customTags
                        allTags.distinct().forEach { tag ->
                            val isSelected = viewModel.postTags.contains(tag)
                            val isCustom = !listOf("减肥", "增肌", "早餐", "低卡", "高蛋白", "瑜伽", "宝宝辅食", "养生茶").contains(tag)
                            
                            SuggestionChip(
                                onClick = { 
                                    if (isSelected) viewModel.removeTag(tag) else viewModel.addTag(tag)
                                },
                                label = { Text(tag) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                                    labelColor = if (isSelected) Color(0xFF66BB6A) else Color.Black
                                ),
                                border = null,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                         if (isSelected) viewModel.removeTag(tag) else viewModel.addTag(tag)
                                    },
                                    onLongClick = {
                                        if (isCustom) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showDeleteTagDialog = tag
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showDeleteTagDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteTagDialog = null },
            title = { Text("删除标签") },
            text = { Text("确定要删除标签“${showDeleteTagDialog}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteTagDialog?.let { tag ->
                            userViewModel.removeCustomTag(tag)
                            viewModel.removeTag(tag)
                        }
                        showDeleteTagDialog = null
                        Toast.makeText(context, "标签删除成功", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTagDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PostDetailDialog(
    post: Post,
    viewModel: CommunityViewModel,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    LaunchedEffect(post.id, userViewModel.userProfile.id) {
        val postId = post.id
        val userId = userViewModel.userProfile.id
        if (!postId.isNullOrBlank() && !userId.isNullOrBlank()) {
            viewModel.recordView(postId, userId)
        }
    }

    LaunchedEffect(post.id) {
        if (post.id != null) {
            viewModel.loadComments(post.id)
        }
    }
    
    if (showEditDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            userViewModel = userViewModel,
            onDismiss = { showEditDialog = false },
            onSuccess = { showEditDialog = false },
            isEdit = true,
            postToEdit = post
        )
    }

    if (showDeleteDialog && commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除评论") },
            text = { Text("确定要删除这条评论吗？") },
            confirmButton = {
                TextButton(onClick = {
                    commentToDelete?.id?.let { viewModel.deleteComment(it) }
                    showDeleteDialog = false
                    commentToDelete = null
                }) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Box(modifier = Modifier.fillMaxSize()) {
                PullToRefreshBox(
                    isRefreshing = viewModel.isReloadingPost,
                    onRefresh = { post.id?.let { viewModel.reloadPost(it) } },
                    state = refreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Back")
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val avatarUrl = if (!post.authorAvatar.isNullOrEmpty()) {
                                post.authorAvatar
                            } else if (post.authorId == userViewModel.userProfile.id && !userViewModel.userProfile.avatarUrl.isNullOrEmpty()) {
                                userViewModel.userProfile.avatarUrl
                            } else {
                                null
                            }
                            
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        val isFollowing = userViewModel.userProfile.following.contains(post.authorId)
                        val isSelf = post.authorId == userViewModel.userProfile.id
                        
                        if (!isSelf) {
                            Button(
                                onClick = { userViewModel.followOrUnfollowUser(post.authorId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) Color.Gray else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isFollowing) Color.Gray else Color(0xFF66BB6A)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (isFollowing) "已关注" else "关注", 
                                    color = if (isFollowing) Color.White else Color(0xFF66BB6A), 
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp)) 
                        }
                    }
                }

                item { Divider(color = Color(0xFFEEEEEE), thickness = 1.dp) }

                    if (post.images.isNotEmpty()) {
                        item {
                            val pagerState = rememberPagerState(pageCount = { post.images.size })
                            
                            Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    val imageUrl = post.images[page]
                                    
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                val pageOffset = (
                                                    (pagerState.currentPage - page) + pagerState
                                                        .currentPageOffsetFraction
                                                    ).absoluteValue
                                
                                                alpha = lerp(
                                                    start = 0.5f,
                                                    stop = 1f,
                                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                                )
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                
                                if (post.images.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 16.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        repeat(post.images.size) { iteration ->
                                            val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray
                                            Box(
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .size(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = post.title, 
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = post.content, 
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = Color(0xFF333333)
                                )
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(modifier = Modifier.wrapContentWidth()) {
                                post.tags.forEach { tag ->
                                    Text(
                                        text = "#$tag ", 
                                        color = Color(0xFF1E88E5),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("发布于 ${post.createdAt ?: "未知时间"}", color = Color.Gray, fontSize = 12.sp)
                            Divider(modifier = Modifier.padding(vertical = 20.dp), color = Color(0xFFEEEEEE))
                        }
                    }

                    item {
                        Text(
                            text = "共 ${viewModel.comments.size} 条评论", 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    // 5. Comments List (Using Root Comments)
                    items(viewModel.rootComments) { comment ->
                        val isCommentLiked = userViewModel.userProfile.id?.let { userId -> 
                            comment.likedUserIds?.contains(userId) == true 
                        } == true
                        val isAuthor = comment.authorId == userViewModel.userProfile.id
                        val replies = viewModel.repliesMap[comment.id] ?: emptyList()
                        val isExpanded = viewModel.expandedComments.contains(comment.id)
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { viewModel.replyToComment = comment },
                                        onLongClick = {
                                            if (isAuthor) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                commentToDelete = comment
                                                showDeleteDialog = true
                                            }
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                val commentAvatarUrl = if (!comment.authorAvatar.isNullOrEmpty()) {
                                    comment.authorAvatar
                                } else if (comment.authorId == userViewModel.userProfile.id && !userViewModel.userProfile.avatarUrl.isNullOrEmpty()) {
                                    userViewModel.userProfile.avatarUrl
                                } else {
                                    null
                                }
                                
                                if (commentAvatarUrl != null) {
                                    AsyncImage(
                                        model = commentAvatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(comment.authorName, fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(comment.content, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(comment.createdAt ?: "", fontSize = 10.sp, color = Color.LightGray)
                                        Spacer(modifier = Modifier.weight(1f))
                                        
                                    Icon(
                                        imageVector = if (isCommentLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like Comment",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { 
                                                 userViewModel.userProfile.id?.let { userId ->
                                                     val newLiked = !isCommentLiked
                                                     val newCount = if (newLiked) comment.likeCount + 1 else maxOf(0, comment.likeCount - 1)
                                                     val newLikedIds = if (newLiked) {
                                                         (comment.likedUserIds ?: emptyList()) + userId
                                                     } else {
                                                         (comment.likedUserIds ?: emptyList()) - userId
                                                     }
                                                     
                                                     val updatedComment = comment.copy(likeCount = newCount, likedUserIds = newLikedIds)
                                                     val index = viewModel.comments.indexOfFirst { it.id == comment.id }
                                                     if (index != -1) {
                                                         viewModel.comments[index] = updatedComment
                                                     }
                                                     
                                                     viewModel.toggleCommentLike(comment, userId)
                                                     Toast.makeText(context, if(newLiked) "点赞成功" else "取消点赞", Toast.LENGTH_SHORT).show()
                                                 }
                                            },
                                        tint = if (isCommentLiked) Color.Red else Color.Gray
                                    )
                                        if (comment.likeCount > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(comment.likeCount.toString(), fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    
                                    if (replies.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .clickable { viewModel.toggleCommentExpand(comment.id ?: "") }
                                                .padding(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isExpanded) "收起回复" else "展开 ${replies.size} 条回复",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            if (isExpanded && replies.isNotEmpty()) {
                                Column(modifier = Modifier.padding(start = 40.dp)) {
                                    replies.forEach { reply ->
                                        val isReplyLiked = userViewModel.userProfile.id?.let { userId -> 
                                            reply.likedUserIds?.contains(userId) == true 
                                        } == true
                                        val isReplyAuthor = reply.authorId == userViewModel.userProfile.id
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = { viewModel.replyToComment = reply },
                                                    onLongClick = {
                                                        if (isReplyAuthor) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            commentToDelete = reply
                                                            showDeleteDialog = true
                                                        }
                                                    }
                                                )
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            val replyAvatarUrl = if (!reply.authorAvatar.isNullOrEmpty()) {
                                                reply.authorAvatar
                                            } else if (reply.authorId == userViewModel.userProfile.id && !userViewModel.userProfile.avatarUrl.isNullOrEmpty()) {
                                                userViewModel.userProfile.avatarUrl
                                            } else {
                                                null
                                            }
                                            
                                            if (replyAvatarUrl != null) {
                                                AsyncImage(
                                                    model = replyAvatarUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(reply.authorName, fontSize = 11.sp, color = Color.Gray)
                                                    if (!reply.replyToUserName.isNullOrEmpty()) {
                                                        Text(" 回复 ", fontSize = 11.sp, color = Color.Gray)
                                                        Text("@${reply.replyToUserName}", fontSize = 11.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(reply.content, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(reply.createdAt ?: "", fontSize = 10.sp, color = Color.LightGray)
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    
                                                    Icon(
                                                        imageVector = if (isReplyLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                        contentDescription = "Like Reply",
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { 
                                                                 userViewModel.userProfile.id?.let { userId ->
                                                                     val newLiked = !isReplyLiked
                                                                     val newCount = if (newLiked) reply.likeCount + 1 else maxOf(0, reply.likeCount - 1)
                                                                     val newLikedIds = if (newLiked) {
                                                                         (reply.likedUserIds ?: emptyList()) + userId
                                                                     } else {
                                                                         (reply.likedUserIds ?: emptyList()) - userId
                                                                     }
                                                                     
                                                                     val updatedReply = reply.copy(likeCount = newCount, likedUserIds = newLikedIds)
                                                                     val index = viewModel.comments.indexOfFirst { it.id == reply.id }
                                                                     if (index != -1) {
                                                                         viewModel.comments[index] = updatedReply
                                                                     }
                                                                     
                                                                     viewModel.toggleCommentLike(reply, userId)
                                                                 }
                                                            },
                                                        tint = if (isReplyLiked) Color.Red else Color.Gray
                                                    )
                                                    if (reply.likeCount > 0) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(reply.likeCount.toString(), fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                        Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(start = 32.dp))
                                    }
                                }
                            }
                            
                            Divider(color = Color(0xFFEEEEEE))
                        }
                    }
                
                if (viewModel.replyToComment != null) {
                    item {
                        Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "回复 @${viewModel.replyToComment?.authorName}", 
                            fontSize = 12.sp, 
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Cancel Reply", 
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.replyToComment = null }
                        )
                    }
                    }
                }

                // Bottom Bar (Comment Input)
                item {
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    OutlinedTextField(
                        value = viewModel.commentContent,
                        onValueChange = { viewModel.commentContent = it },
                        placeholder = { Text("说点什么...", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 100.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5)
                        )
                    )
                    
                    // Send Button (only show if has content)
                                    if (viewModel.commentContent.isNotBlank()) {
                                         Spacer(modifier = Modifier.width(8.dp))
                                         val context = LocalContext.current
                                         IconButton(
                             onClick = { 
                                 if (post.id != null) {
                                     viewModel.sendComment(
                                         postId = post.id,
                                         authorId = userViewModel.userProfile.id ?: "temp_id",
                                         authorName = userViewModel.userProfile.nickname,
                                         authorAvatar = userViewModel.userProfile.avatarUrl
                                     )
                                     Toast.makeText(context, "评论发布成功", Toast.LENGTH_SHORT).show()
                                 }
                             },
                             enabled = !viewModel.isSendingComment
                         ) {
                             if (viewModel.isSendingComment) {
                                 CircularProgressIndicator(modifier = Modifier.size(24.dp))
                             } else {
                                 Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF66BB6A))
                             }
                         }
                    } else {
                        val currentUserId = userViewModel.userProfile.id ?: ""
                        
                        val isLiked = post.likedUserIds.contains(currentUserId)
                        val isFavorited = post.favoritedUserIds.contains(currentUserId)

                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.clickable { viewModel.toggleLike(post, currentUserId) },
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(post.likeCount.toString())
                        Spacer(modifier = Modifier.width(16.dp))
                        val context = LocalContext.current
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null, 
                            tint = if (isFavorited) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.clickable { 
                                viewModel.toggleFavorite(post, currentUserId) 
                                Toast.makeText(context, if (isFavorited) "已取消收藏" else "收藏成功", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                }
            }
        }
        
        val isSelf = post.authorId == userViewModel.userProfile.id
        if (isSelf) {
             FloatingActionButton(
                 onClick = { showEditDialog = true },
                 modifier = Modifier
                     .align(Alignment.BottomEnd)
                     .padding(24.dp)
                     .padding(bottom = 80.dp),
                 containerColor = Color(0xFF66BB6A),
                 contentColor = Color.White,
                 shape = CircleShape
             ) {
                 Icon(Icons.Default.Edit, contentDescription = "Edit Post")
             }
        }
      }
    }
  }
}
