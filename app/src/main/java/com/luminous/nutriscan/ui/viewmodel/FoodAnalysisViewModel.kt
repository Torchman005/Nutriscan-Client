package com.example.foodnutritionaiassistant.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodnutritionaiassistant.data.network.FoodRecognitionRecord
import com.example.foodnutritionaiassistant.data.repository.FoodAnalysisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FoodAnalysisViewModel(private val repository: FoodAnalysisRepository) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var analysisResult by mutableStateOf<Map<String, Any>?>(null)
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var error by mutableStateOf<String?>(null)
    
    var history by mutableStateOf<List<FoodRecognitionRecord>>(emptyList())

    fun fetchHistory(userId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getHistory(userId)
            }
            result.fold(
                onSuccess = { history = it },
                onFailure = { /* Ignore or log */ }
            )
        }
    }

    fun analyzeImage(context: Context, uri: Uri, userId: String? = null) {
        viewModelScope.launch {
            isLoading = true
            error = null
            analysisResult = null
            selectedImageUri = uri

            try {
                val file = uriToFile(context, uri)
                if (file != null) {
                    val result = withContext(Dispatchers.IO) {
                        repository.analyzeFood(file, userId)
                    }
                    result.fold(
                        onSuccess = { data ->
                            analysisResult = data
                            if (userId != null) fetchHistory(userId)
                        },
                        onFailure = { exception ->
                            error = "Analysis failed: ${exception.message}"
                            exception.printStackTrace()
                        }
                    )
                } else {
                    error = "Failed to process image file"
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    fun analyzeImageByUrl(imageUrl: String, userId: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            analysisResult = null
            selectedImageUri = Uri.parse(imageUrl)
            
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.analyzeFoodByUrl(imageUrl, userId)
                }
                
                result.fold(
                    onSuccess = { data ->
                        analysisResult = data
                        fetchHistory(userId)
                    },
                    onFailure = { exception ->
                         error = "Analysis failed: ${exception.message}"
                         exception.printStackTrace()
                    }
                )
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        try {
            var inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // 2. Calculate inSampleSize (Max 1920px)
            val maxDimension = 1920
            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }
            
            inputStream = context.contentResolver.openInputStream(uri)
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            if (bitmap == null) return null
            
            // 4. Compress to file (JPEG 80%)
            val file = File(context.cacheDir, "compressed_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun clearResult() {
        analysisResult = null
        selectedImageUri = null
        error = null
    }
}
