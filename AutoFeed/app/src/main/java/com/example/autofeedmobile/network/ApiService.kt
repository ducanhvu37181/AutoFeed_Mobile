package com.example.autofeedmobile.network

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/Auth/login")
    suspend fun login(
        @Query("usernameOrEmail") usernameOrEmail: String,
        @Query("password") password: String
    ): Response<LoginResponse>
}
