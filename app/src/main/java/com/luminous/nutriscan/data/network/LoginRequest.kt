package com.example.foodnutritionaiassistant.data.network

data class LoginRequest(
    val phoneNumber: String? = null,
    val password: String? = null,
    val loginType: String, // "PHONE" or "WECHAT"
    val wechatOpenId: String? = null
)
