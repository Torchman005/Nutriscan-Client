package com.example.foodnutritionaiassistant.data.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.foodnutritionaiassistant.config.AppConfig
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = AppConfig.BACKEND_BASE_URL
    
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val localDateAdapter = object : TypeAdapter<LocalDate>() {
        override fun write(out: JsonWriter, value: LocalDate?) {
            out.value(value?.toString())
        }

        override fun read(input: JsonReader): LocalDate? {
            return LocalDate.parse(input.nextString())
        }
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, localDateAdapter)
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Match Spring Boot default for Date/LocalDateTime
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
