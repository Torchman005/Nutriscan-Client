package com.example.foodnutritionaiassistant.config

object AppConfig {
    // Note: 10.0.2.2 is the special alias to your host loopback interface (i.e., 127.0.0.1 on your development machine)
    // If you are testing on a real device, change this to your computer's LAN IP address (e.g., 192.168.1.x)
    const val HOST_IP = "118.195.219.166"

    const val MINIO_ENDPOINT = "http://118.195.219.166:9000"
    const val MINIO_ACCESS_KEY = "minioadmin"
    const val MINIO_SECRET_KEY = "minioadmin"
    const val MINIO_BUCKET_NAME = "nutriscan"

    const val BACKEND_BASE_URL = "http://118.195.219.166:8080/"
}
