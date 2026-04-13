package com.example.foodnutritionaiassistant.data.repository

import com.example.foodnutritionaiassistant.data.model.Comment
import com.example.foodnutritionaiassistant.data.model.GroupCategory
import com.example.foodnutritionaiassistant.data.model.Post
import com.example.foodnutritionaiassistant.data.model.PostStatus
import com.example.foodnutritionaiassistant.data.model.PageResponse
import com.example.foodnutritionaiassistant.data.network.RetrofitClient
import com.example.foodnutritionaiassistant.data.storage.MinioHelper
import java.io.InputStream
import java.util.UUID

class CommunityRepository {

    private val api = RetrofitClient.apiService

    suspend fun getPostsByCategory(category: GroupCategory): List<Post> {
        return try {
            val response = api.getPosts(category.value)
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

    suspend fun getPostsPaged(
        category: GroupCategory?,
        search: String?,
        followedOnly: Boolean,
        userId: String?,
        page: Int,
        sortBy: String,
        sortDir: String
    ): PageResponse<Post>? {
        return try {
            val response = api.getPostsPaged(
                category = category?.value,
                search = search,
                followedOnly = followedOnly,
                userId = userId,
                page = page,
                sortBy = sortBy,
                sortDir = sortDir
            )
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPost(id: String): Post? {
        return try {
            val response = api.getPost(id)
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createPost(post: Post): Boolean {
        return try {
            val response = api.createPost(post)
            if (!response.isSuccessful) {
                println("Create Post Failed: Code ${response.code()}, Error: ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePost(id: String, post: Post): Boolean {
        return try {
            val response = api.updatePost(id, post)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun saveDraft(post: Post): Boolean {
        return try {
            val draftPost = post.copy(status = PostStatus.DRAFT)
            val response = api.createPost(draftPost)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun uploadImage(inputStream: InputStream, extension: String): String? {
        val fileName = "posts/${UUID.randomUUID()}.$extension"
        return MinioHelper.uploadImage(inputStream, fileName)
    }

    suspend fun getComments(postId: String): List<Comment> {
        return try {
             val response = api.getComments(postId)
             response.body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Boolean {
        return try {
            val response = api.addComment(comment)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun toggleCommentLike(commentId: String, userId: String): Comment? {
        return try {
            val response = api.likeComment(commentId, userId)
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteComment(commentId: String): Boolean {
        return try {
            val response = api.deleteComment(commentId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun toggleLike(postId: String, userId: String): Post? {
        return try {
            val response = api.likePost(postId, userId)
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun toggleFavorite(postId: String, userId: String): Post? {
        return try {
            val response = api.favoritePost(postId, userId)
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getFavoritePosts(userId: String): List<Post> {
        return try {
            val response = api.getFavoritePosts(userId)
            response.body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun recordView(postId: String, userId: String) {
        try {
            api.recordView(postId, userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getViewHistory(userId: String): List<Post> {
        return try {
            val response = api.getViewHistory(userId)
            response.body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun clearViewHistory(userId: String) {
        try {
            api.clearViewHistory(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getPostsByAuthor(authorId: String): List<Post> {
        return try {
            val response = api.getPostsByAuthor(authorId)
            response.body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            val response = api.deletePost(postId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
