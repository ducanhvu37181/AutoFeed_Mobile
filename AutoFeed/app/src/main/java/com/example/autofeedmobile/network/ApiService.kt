package com.example.autofeedmobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/Auth/login")
    suspend fun login(
        @Query("usernameOrEmail") usernameOrEmail: String,
        @Query("password") password: String
    ): Response<LoginResponse>

    @GET("api/Schedule/user/{userId}")
    suspend fun getSchedules(@Path("userId") userId: Int): Response<ScheduleListResponse>

    @GET("api/Schedule/user/{userId}/date")
    suspend fun getSchedulesByDate(
        @Path("userId") userId: Int,
        @Query("date") date: String
    ): Response<ScheduleListResponse>

    @GET("api/Schedule/{id}")
    suspend fun getScheduleDetail(@Path("id") id: Int): Response<ScheduleDetailResponse>

    @PATCH("api/Schedule/{id}/status")
    suspend fun updateScheduleStatus(
        @Path("id") id: Int,
        @Body status: String
    ): Response<Unit>

    @GET("api/Request/user/{userId}")
    suspend fun getRequests(@Path("userId") userId: Int): Response<RequestListResponse>
}
