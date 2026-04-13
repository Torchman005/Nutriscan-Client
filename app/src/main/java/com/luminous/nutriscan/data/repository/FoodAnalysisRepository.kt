package com.example.foodnutritionaiassistant.data.repository

import com.example.foodnutritionaiassistant.data.network.ApiService
import com.example.foodnutritionaiassistant.data.network.FoodRecognitionRecord
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FoodAnalysisRepository(private val apiService: ApiService) {

    suspend fun analyzeFood(file: File, userId: String? = null): Result<Map<String, Any>> {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        
        return try {
            val response = apiService.analyzeFood(body, userId)
            handleResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun analyzeFoodByUrl(imageUrl: String, userId: String): Result<Map<String, Any>> {
        return try {
            val request = mapOf("imageUrl" to imageUrl, "userId" to userId)
            val response = apiService.analyzeFoodByUrl(request)
            handleResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun <T> handleResponse(response: retrofit2.Response<T>): Result<T> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.success(body)
            } else {
                return Result.failure(Exception("Response body is null"))
            }
        } else {
            return Result.failure(Exception("HTTP Error: ${response.code()} ${response.message()}"))
        }
    }

    suspend fun getHistory(userId: String): Result<List<FoodRecognitionRecord>> {
        return try {
            val response = apiService.getAnalysisHistory(userId)
            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                Result.success(body)
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
