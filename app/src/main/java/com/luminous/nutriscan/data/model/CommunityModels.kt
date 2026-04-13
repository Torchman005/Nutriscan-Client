package com.example.foodnutritionaiassistant.data.model

import java.util.Date

enum class GroupCategory(val displayName: String, val value: String) {
    WELLNESS("养生", "wellness"),
    FITNESS("健身", "fitness"),
    TODDLER("幼儿", "toddler");

    companion object {
        fun fromValue(value: String): GroupCategory? = entries.find { it.value == value }
    }
}

enum class PostStatus {
    DRAFT,
    PUBLISHED,
    DELETED
}

data class Post(
    val id: String? = null,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val title: String,
    val content: String,
    val images: List<String>,
    val tags: List<String>,
    val category: String,
    val status: PostStatus = PostStatus.PUBLISHED,
    val likeCount: Int = 0,
    val favoriteCount: Int = 0,
    val likedUserIds: List<String> = emptyList(),
    val favoritedUserIds: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class Comment(
    val id: String? = null,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val content: String,
    val createdAt: String? = null,
    val likeCount: Int = 0,
    val likedUserIds: List<String>? = emptyList(),
    val replyToUserName: String? = null,
    val replyToUserId: String? = null
)

data class PageResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val last: Boolean,
    val size: Int,
    val number: Int
)
