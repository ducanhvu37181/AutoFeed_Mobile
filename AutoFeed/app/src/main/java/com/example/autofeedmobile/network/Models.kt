package com.example.autofeedmobile.network

data class UserResponse(
    val userId: Int,
    val username: String,
    val fullName: String,
    val roleId: Int
)

data class LoginResponse(
    val message: String?,
    val token: String?,
    val user: UserResponse?
)
