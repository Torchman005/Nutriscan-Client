package com.example.foodnutritionaiassistant.data.storage

import android.util.Log
import com.example.foodnutritionaiassistant.config.AppConfig
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.SetBucketPolicyArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object MinioHelper {
    private const val TAG = "MinioHelper"
    private var minioClient: MinioClient? = null

    private var isPolicyChecked = false

    private fun getMinioClient(): MinioClient {
        if (minioClient == null) {
            try {
                minioClient = MinioClient.builder()
                    .endpoint(AppConfig.MINIO_ENDPOINT)
                    .credentials(AppConfig.MINIO_ACCESS_KEY, AppConfig.MINIO_SECRET_KEY)
                    .build()

                minioClient?.setTimeout(10000, 10000, 10000)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MinIO client", e)
                throw e
            }
        }
        return minioClient!!
    }

    private suspend fun ensureBucketPolicy(client: MinioClient) {
        if (isPolicyChecked) return
        
        try {
            val bucketExists = client.bucketExists(io.minio.BucketExistsArgs.builder().bucket(AppConfig.MINIO_BUCKET_NAME).build())
            if (!bucketExists) {
                 client.makeBucket(io.minio.MakeBucketArgs.builder().bucket(AppConfig.MINIO_BUCKET_NAME).build())
            }
            
            val policy = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {"AWS": ["*"]},
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::${AppConfig.MINIO_BUCKET_NAME}/*"]
                }
              ]
            }
            """.trimIndent()
            
            client.setBucketPolicy(
                io.minio.SetBucketPolicyArgs.builder()
                    .bucket(AppConfig.MINIO_BUCKET_NAME)
                    .config(policy)
                    .build()
            )
            Log.d(TAG, "Bucket policy set to public read")
            isPolicyChecked = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set bucket policy", e)
        }
    }

    suspend fun uploadImage(
        inputStream: InputStream,
        fileName: String,
        contentType: String = "image/jpeg"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val client = getMinioClient()

            // Ensure bucket exists and is public (checking every time or once per session)
            ensureBucketPolicy(client)

            client.putObject(
                PutObjectArgs.builder()
                    .bucket(AppConfig.MINIO_BUCKET_NAME)
                    .`object`(fileName)
                    .stream(inputStream, -1, 10485760)
                    .contentType(contentType)
                    .build()
            )

            "${AppConfig.MINIO_ENDPOINT}/${AppConfig.MINIO_BUCKET_NAME}/$fileName"
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
}
