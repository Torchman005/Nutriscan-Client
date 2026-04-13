package com.example.foodnutritionaiassistant.ui.viewmodel

import android.util.Log
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnutritionaiassistant.data.model.GroupCategory
import com.example.foodnutritionaiassistant.data.model.Post
import com.example.foodnutritionaiassistant.data.model.PostStatus
import com.example.foodnutritionaiassistant.data.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

import com.example.foodnutritionaiassistant.data.model.Comment

import kotlinx.coroutines.withContext

class CommunityViewModel : ViewModel() {
    private val communityRepository = CommunityRepository()

    var posts = mutableStateListOf<Post>()
        private set
    var isLoading by mutableStateOf(false)
    var currentCategory by mutableStateOf(GroupCategory.WELLNESS)
    var isCategoryInitialized by mutableStateOf(false)

    var currentPage by mutableStateOf(0)
    var isLastPage by mutableStateOf(false)
    var isFollowedOnly by mutableStateOf(false)
    var sortBy by mutableStateOf("createdAt") // "createdAt" or "likeCount"
    var sortDir by mutableStateOf("desc")

    var postTitle by mutableStateOf("")
    var postContent by mutableStateOf("")
    var selectedGroup by mutableStateOf(GroupCategory.WELLNESS)
    var postImages = mutableStateListOf<String>() // List of image URIs (local or remote)
    var postTags = mutableStateListOf<String>()
    var currentTagInput by mutableStateOf("")
    
    var isPublishing by mutableStateOf(false)
    var publishSuccess by mutableStateOf<Boolean?>(null)

    var comments = mutableStateListOf<Comment>()
        private set
    var commentContent by mutableStateOf("")
    var isSendingComment by mutableStateOf(false)
    var replyToComment by mutableStateOf<Comment?>(null)
    
    var rootComments = mutableStateListOf<Comment>()
    private set
    var repliesMap = mutableMapOf<String, MutableList<Comment>>()
    var expandedComments = mutableStateListOf<String>()

    val usedTags = listOf("减肥", "增肌", "早餐", "低卡", "高蛋白", "瑜伽", "宝宝辅食", "养生茶")

    init {
        loadPosts(currentCategory)
    }

    private val _searchQuery = mutableStateOf("")
    var searchQuery: String
        get() = _searchQuery.value
        set(value) {
            _searchQuery.value = value
        }

    fun getFilteredPosts(): List<Post> {
        return posts
    }

    fun getFilteredMyPosts(): List<Post> {
        return if (searchQuery.isBlank()) myPosts 
        else myPosts.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }
    
    fun getFilteredFavoritePosts(): List<Post> {
        return if (searchQuery.isBlank()) favoritePosts 
        else favoritePosts.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }
    
    fun getFilteredHistoryPosts(): List<Post> {
        return if (searchQuery.isBlank()) historyPosts 
        else historyPosts.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    fun loadPosts(category: GroupCategory, reset: Boolean = false, userId: String? = null) {
        if (reset) {
            currentPage = 0
            isLastPage = false
            posts.clear()
        }
        if (isLastPage && !reset) return
        
        currentCategory = category
        viewModelScope.launch {
            if (reset) isLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    communityRepository.getPostsPaged(
                        category = category,
                        search = searchQuery,
                        followedOnly = isFollowedOnly,
                        userId = userId,
                        page = currentPage,
                        sortBy = sortBy,
                        sortDir = sortDir
                    )
                }
                
                if (result != null) {
                    if (reset) posts.clear()
                    
                    val newPosts = result.content.filter { newPost -> 
                        posts.none { it.id == newPost.id }
                    }
                    posts.addAll(newPosts)
                    
                    isLastPage = result.last
                    if (!isLastPage) currentPage++
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommunityViewModel", "Error loading posts", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    fun toggleFollowedOnly(userId: String?) {
        isFollowedOnly = !isFollowedOnly
        loadPosts(currentCategory, reset = true, userId = userId)
    }
    
    fun setSortOrder(by: String, dir: String, userId: String?) {
        sortBy = by
        sortDir = dir
        loadPosts(currentCategory, reset = true, userId = userId)
    }
    
    fun refreshPosts(userId: String?) {
        loadPosts(currentCategory, reset = true, userId = userId)
    }

    fun addImage(uri: String) {
        if (postImages.size < 9) {
            postImages.add(uri)
        }
    }

    fun removeImage(uri: String) {
        postImages.remove(uri)
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && !postTags.contains(tag)) {
            postTags.add(tag)
        }
        currentTagInput = ""
    }

    fun removeTag(tag: String) {
        postTags.remove(tag)
    }

    fun publishPost(context: Context, authorId: String, authorNickname: String, authorAvatar: String?) {
        if (postTitle.isBlank() || postContent.isBlank()) return

        isPublishing = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uploadedImageUrls = mutableListOf<String>()
                if (postImages.isNotEmpty()) {
                    for (uriString in postImages) {
                        val uri = Uri.parse(uriString)
                        if (uri.scheme?.startsWith("http") == true) {
                            uploadedImageUrls.add(uriString)
                        } else {
                            try {
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
                                    val url = communityRepository.uploadImage(inputStream, extension)
                                    if (url != null) {
                                        uploadedImageUrls.add(url)
                                    } else {
                                        Log.e("CommunityViewModel", "Failed to upload image: $uri")
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e("CommunityViewModel", "Exception processing image: $uri", e)
                            }
                        }
                    }
                    
                    if (uploadedImageUrls.isEmpty()) {
                    }
                }

                val newPost = Post(
                    authorId = authorId,
                    authorName = authorNickname,
                    authorAvatar = authorAvatar,
                    title = postTitle,
                    content = postContent,
                    images = uploadedImageUrls,
                    category = selectedGroup.value,
                    tags = postTags.toList(),
                    status = PostStatus.PUBLISHED
                )

                val success = communityRepository.createPost(newPost)
                
                withContext(Dispatchers.Main) {
                    isPublishing = false
                    publishSuccess = success
                    if (success) {
                        resetCreateForm()
                        loadPosts(currentCategory)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isPublishing = false
                    publishSuccess = false
                    println("Publish Post Exception: ${e.message}")
                }
            }
        }
    }
    
    fun saveDraft(authorId: String, authorNickname: String) {
        viewModelScope.launch(Dispatchers.IO) {
             val newPost = Post(
                authorId = authorId,
                authorName = authorNickname,
                authorAvatar = null,
                title = postTitle,
                content = postContent,
                images = emptyList(), 
                category = selectedGroup.value,
                tags = postTags.toList(),
                status = PostStatus.DRAFT
            )
            communityRepository.saveDraft(newPost)
        }
    }

    fun updatePost(context: Context, postId: String, authorId: String, authorNickname: String, authorAvatar: String?) {
        if (postTitle.isBlank() || postContent.isBlank()) return

        isPublishing = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Upload Images (similar logic to publishPost)
                val uploadedImageUrls = mutableListOf<String>()
                if (postImages.isNotEmpty()) {
                    for (uriString in postImages) {
                        if (uriString.startsWith("http")) {
                            uploadedImageUrls.add(uriString)
                        } else {
                            val uri = Uri.parse(uriString)
                            try {
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
                                    val url = communityRepository.uploadImage(inputStream, extension)
                                    if (url != null) {
                                        uploadedImageUrls.add(url)
                                    } else {
                                        Log.e("CommunityViewModel", "Failed to upload image: $uri")
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e("CommunityViewModel", "Exception processing image: $uri", e)
                            }
                        }
                    }
                }

                val updatedPost = Post(
                    id = postId,
                    authorId = authorId,
                    authorName = authorNickname,
                    authorAvatar = authorAvatar,
                    title = postTitle,
                    content = postContent,
                    images = uploadedImageUrls,
                    category = selectedGroup.value,
                    tags = postTags.toList(),
                    status = PostStatus.PUBLISHED
                )

                val success = communityRepository.updatePost(postId, updatedPost)
                
                withContext(Dispatchers.Main) {
                    isPublishing = false
                    publishSuccess = success
                    if (success) {
                        resetCreateForm()
                        reloadPost(postId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isPublishing = false
                    publishSuccess = false
                }
            }
        }
    }
    
    fun prepareEditPost(post: Post) {
        postTitle = post.title
        postContent = post.content
        postImages.clear()
        postImages.addAll(post.images)
        postTags.clear()
        postTags.addAll(post.tags)
        selectedGroup = GroupCategory.entries.find { it.value == post.category } ?: GroupCategory.WELLNESS
    }

    private fun resetCreateForm() {
        postTitle = ""
        postContent = ""
        postImages.clear()
        postTags.clear()
        selectedGroup = GroupCategory.WELLNESS
    }
    
    fun resetPublishStatus() {
        publishSuccess = null
    }

    var isReloadingPost by mutableStateOf(false)

    fun reloadPost(postId: String) {
        viewModelScope.launch {
            isReloadingPost = true
            try {
                val updatedPostDeferred = async(Dispatchers.IO) { communityRepository.getPost(postId) }
                val commentsDeferred = async(Dispatchers.IO) { communityRepository.getComments(postId) }
                
                val updatedPost = updatedPostDeferred.await()
                val commentsResult = commentsDeferred.await()
                
                if (updatedPost != null) {
                    updatePostInList(updatedPost)
                }
                comments.clear()
                comments.addAll(commentsResult)
                organizeComments(commentsResult)
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isReloadingPost = false
            }
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    communityRepository.getComments(postId)
                }
                comments.clear()
                comments.addAll(result)
                organizeComments(result)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommunityViewModel", "Error loading comments", e)
            }
        }
    }

    private fun organizeComments(allComments: List<Comment>) {
        rootComments.clear()
        repliesMap.clear()
        
        // 1. Identify direct roots (those without replyToUserId or replyToUserId not found in list)
        val commentMap = allComments.associateBy { it.id }
        
        allComments.forEach { comment ->
            // If replyToUserId is not in map, it's a root (orphan or deleted parent).
            if (comment.replyToUserId == null || !commentMap.containsKey(comment.replyToUserId)) {
                rootComments.add(comment)
            } else {
                var current = comment
                var depth = 0
                while (current.replyToUserId != null && commentMap.containsKey(current.replyToUserId) && depth < 20) {
                    val parent = commentMap[current.replyToUserId]
                    if (parent == null) break 
                    
                    if (parent.replyToUserId == null || !commentMap.containsKey(parent.replyToUserId)) {
                        current = parent
                        break
                    }
                    current = parent
                    depth++
                }
                
                val rootId = current.id ?: ""
                if (rootId.isNotEmpty()) {
                    if (!repliesMap.containsKey(rootId)) {
                        repliesMap[rootId] = mutableListOf()
                    }
                    repliesMap[rootId]?.add(comment)
                } else {
                    rootComments.add(comment)
                }
            }
        }
    }

    fun toggleCommentExpand(commentId: String) {
        if (expandedComments.contains(commentId)) {
            expandedComments.remove(commentId)
        } else {
            expandedComments.add(commentId)
        }
    }
    
    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                communityRepository.deleteComment(commentId)
            }
            if (success) {
                val commentMap = comments.associateBy { it.id }
                val childrenMap = comments.groupBy { it.replyToUserId }
                
                val toDelete = mutableSetOf<String>()
                toDelete.add(commentId)
                
                val stack = mutableListOf(commentId)
                while (stack.isNotEmpty()) {
                    val currentId = stack.removeAt(0)
                    val children = childrenMap[currentId]
                    if (children != null) {
                        for (child in children) {
                            if (child.id != null) {
                                toDelete.add(child.id)
                                stack.add(child.id)
                            }
                        }
                    }
                }
                
                val updatedList = comments.filter { it.id !in toDelete }
                comments.clear()
                comments.addAll(updatedList)
                organizeComments(updatedList)
            }
        }
    }

    fun sendComment(postId: String, authorId: String, authorName: String, authorAvatar: String?) {
        if (commentContent.isBlank()) return
        isSendingComment = true
        viewModelScope.launch {
             val newComment = Comment(
                 postId = postId,
                 authorId = authorId,
                 authorName = authorName,
                 authorAvatar = authorAvatar,
                 content = commentContent,
                 replyToUserName = replyToComment?.authorName,
                 replyToUserId = replyToComment?.id
             )
             val success = withContext(Dispatchers.IO) {
                 communityRepository.addComment(newComment)
             }
             if (success) {
                 commentContent = ""
                 replyToComment = null
                 loadComments(postId)
             }
             isSendingComment = false
        }
    }

    fun toggleCommentLike(comment: Comment, userId: String) {
        viewModelScope.launch {
            val updatedComment = withContext(Dispatchers.IO) {
                communityRepository.toggleCommentLike(comment.id ?: return@withContext null, userId)
            }
            if (updatedComment != null) {
                val index = comments.indexOfFirst { it.id == updatedComment.id }
                if (index != -1) {
                    comments[index] = updatedComment
                    organizeComments(comments.toList())
                }
            }
        }
    }

    fun toggleLike(post: Post, userId: String) {
        viewModelScope.launch {
            val updatedPost = withContext(Dispatchers.IO) {
                communityRepository.toggleLike(post.id ?: return@withContext null, userId)
            }
            if (updatedPost != null) {
                updatePostInList(updatedPost)
            }
        }
    }

    fun toggleFavorite(post: Post, userId: String) {
        viewModelScope.launch {
            val updatedPost = withContext(Dispatchers.IO) {
                communityRepository.toggleFavorite(post.id ?: return@withContext null, userId)
            }
            if (updatedPost != null) {
                updatePostInList(updatedPost)
            }
        }
    }

    private fun updatePostInList(updatedPost: Post) {
        val index = posts.indexOfFirst { it.id == updatedPost.id }
        if (index != -1) {
            posts[index] = updatedPost
        }
        
        val favIndex = favoritePosts.indexOfFirst { it.id == updatedPost.id }
        if (favIndex != -1) {
             favoritePosts[favIndex] = updatedPost
        } else {
        }
    }
    
    var favoritePosts = mutableStateListOf<Post>()
        private set
        
    fun loadFavoritePosts(userId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    communityRepository.getFavoritePosts(userId)
                }
                favoritePosts.clear()
                favoritePosts.addAll(result)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommunityViewModel", "Error loading favorite posts", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    var historyPosts = mutableStateListOf<Post>()
        private set
        
    fun loadViewHistory(userId: String) {
        if (userId.isBlank()) {
            historyPosts.clear()
            return
        }
        viewModelScope.launch {
            isLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    communityRepository.getViewHistory(userId)
                }
                historyPosts.clear()
                historyPosts.addAll(result)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommunityViewModel", "Error loading view history", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    fun recordView(postId: String, userId: String) {
        if (userId.isBlank() || postId.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                communityRepository.recordView(postId, userId)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommunityViewModel", "Error recording view", e)
            }
        }
    }
    
    fun clearViewHistory(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            communityRepository.clearViewHistory(userId)
            withContext(Dispatchers.Main) {
                historyPosts.clear()
            }
        }
    }
    
    var myPosts = mutableStateListOf<Post>()
        private set
        
    fun loadMyPosts(userId: String) {
        viewModelScope.launch {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                communityRepository.getPostsByAuthor(userId)
            }
            myPosts.clear()
            myPosts.addAll(result)
            isLoading = false
        }
    }
    
    fun deletePosts(postIds: List<String>) {
        viewModelScope.launch {
            val toRemove = postIds.toSet()
            myPosts.removeIf { toRemove.contains(it.id) }
            posts.removeIf { toRemove.contains(it.id) }
            
            withContext(Dispatchers.IO) {
                postIds.forEach { id ->
                    communityRepository.deletePost(id)
                }
            }
        }
    }
}
