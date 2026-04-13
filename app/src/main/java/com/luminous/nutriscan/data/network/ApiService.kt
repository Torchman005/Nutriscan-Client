package com.example.foodnutritionaiassistant.data.network

import com.example.foodnutritionaiassistant.data.model.Comment
import com.example.foodnutritionaiassistant.data.model.Post
import com.example.foodnutritionaiassistant.data.model.PageResponse
import com.example.foodnutritionaiassistant.ui.viewmodel.UserProfile
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 用户接口
    @POST("/api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<UserProfile>

    @POST("/api/users/register")
    suspend fun register(@Body user: UserProfile): Response<UserProfile>

    @PUT("/api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UserProfile): Response<UserProfile>

    @GET("/api/users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserProfile>
    
    // 反馈接口
    @POST("/api/feedback")
    suspend fun submitFeedback(@Body feedback: Feedback): Response<Feedback>

    @POST("/api/users/{id}/follow/{targetId}")
    suspend fun followUser(@Path("id") id: String, @Path("targetId") targetId: String): Response<UserProfile>

    @POST("/api/users/{id}/unfollow/{targetId}")
    suspend fun unfollowUser(@Path("id") id: String, @Path("targetId") targetId: String): Response<UserProfile>

    // 社区接口
    @GET("/api/posts")
    suspend fun getPosts(@Query("category") category: String? = null): Response<List<Post>>

    @GET("/api/posts/paged")
    suspend fun getPostsPaged(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("followedOnly") followedOnly: Boolean = false,
        @Query("userId") userId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<PageResponse<Post>>

    @POST("/api/posts")
    suspend fun createPost(@Body post: Post): Response<Post>

    @PUT("/api/posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body post: Post): Response<Post>
    
    @GET("/api/posts/{id}")
    suspend fun getPost(@Path("id") id: String): Response<Post>

    @POST("/api/posts/{id}/like")
    suspend fun likePost(@Path("id") id: String, @Query("userId") userId: String): Response<Post>

    @POST("/api/posts/{id}/favorite")
    suspend fun favoritePost(@Path("id") id: String, @Query("userId") userId: String): Response<Post>

    @GET("/api/posts/favorites")
    suspend fun getFavoritePosts(@Query("userId") userId: String): Response<List<Post>>

    @POST("/api/posts/{id}/view")
    suspend fun recordView(@Path("id") id: String, @Query("userId") userId: String): Response<Void>

    @GET("/api/posts/history")
    suspend fun getViewHistory(@Query("userId") userId: String): Response<List<Post>>

    @DELETE("/api/posts/history")
    suspend fun clearViewHistory(@Query("userId") userId: String): Response<Void>

    @GET("/api/posts/user/{userId}")
    suspend fun getPostsByAuthor(@Path("userId") userId: String): Response<List<Post>>

    @DELETE("/api/posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Void>

    // 评论接口
    @GET("/api/comments")
    suspend fun getComments(@Query("postId") postId: String): Response<List<Comment>>

    @POST("/api/comments")
    suspend fun addComment(@Body comment: Comment): Response<Comment>

    @POST("/api/comments/{id}/like")
    suspend fun likeComment(@Path("id") id: String, @Query("userId") userId: String): Response<Comment>

    @DELETE("/api/comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: String): Response<Void>

    // 食物识别接口
    @Multipart
    @POST("/api/analysis/food")
    suspend fun analyzeFood(
        @Part image: MultipartBody.Part,
        @Query("userId") userId: String? = null
    ): Response<Map<String, Any>>
    
    @POST("/api/analysis/food/url")
    suspend fun analyzeFoodByUrl(@Body request: Map<String, String>): Response<Map<String, Any>>

    @GET("/api/analysis/history")
    suspend fun getAnalysisHistory(@Query("userId") userId: String): Response<List<FoodRecognitionRecord>>

    @POST("/api/analysis/manual")
    suspend fun addManualRecord(@Body record: ManualFoodRecordRequest): Response<FoodRecognitionRecord>

    @POST("/api/calories")
    suspend fun addCalorieRecord(@Body record: CalorieRecordRequest): Response<Unit>

    @POST("/api/tags")
    suspend fun addTag(@Body tag: Tag): Response<Tag>

    @GET("/api/tags/user/{userId}")
    suspend fun getUserTags(@Path("userId") userId: String): Response<List<Tag>>

    @DELETE("/api/tags")
    suspend fun removeTag(@Query("userId") userId: String, @Query("tag") tag: String): Response<Void>

    @GET("/api/statistics/today")
    suspend fun getTodayStatistics(@Query("userId") userId: String): Response<UserStatistics>

    @GET("/api/statistics/date")
    suspend fun getStatisticsForDate(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<UserStatistics>

    @GET("/api/statistics/week")
    suspend fun getWeekStatistics(@Query("userId") userId: String): Response<List<UserStatistics>>

    @PUT("/api/users/{id}/weight")
    suspend fun updateWeight(@Path("id") id: String, @Body request: WeightUpdateRequest): Response<UserProfile>
}

data class Tag(
    val id: String? = null,
    val userId: String,
    val tag: String
)

data class Feedback(
    val id: String? = null,
    val userId: String,
    val userNickname: String,
    val content: String,
    val contactInfo: String? = null,
    val type: String = "OTHER"
)

data class FoodRecognitionRecord(
    val id: String,
    val userId: String,
    val imageUrl: String,
    val foodName: String? = null,
    val calories: String? = null,
    val createdAt: String,
    val rawResult: Map<String, Any>? = null
)

data class UserStatistics(
    val date: String? = null,
    val totalCalories: Double = 0.0,
    val calorieGoal: Double = 2000.0,
    val calorieProgress: Double = 0.0,
    val protein: Double = 0.0,
    val carbohydrates: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val weight: Double = 0.0,
    val targetWeight: Double = 0.0,
    val weightChange: Double = 0.0,
    val bmi: Double = 0.0,
    val bmr: Double = 1500.0,
    val achievementRate: Int = 0,
    val foodCount: Int = 0,
    val dayStreak: Int = 0
)

data class CalorieRecordRequest(
    val userId: String,
    val calories: Double,
    val recordedAt: String? = null
)

data class ManualFoodRecordRequest(
    val userId: String,
    val foodName: String,
    val calories: Double,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbohydrates: Double = 0.0
)

data class WeightUpdateRequest(
    val weight: Double
)
