package com.example.autofeedmobile.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

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

    @POST("api/Request")
    suspend fun createRequest(@Body request: CreateRequestDto): Response<Unit>

    @GET("api/Inventory")
    suspend fun getInventory(
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<InventoryListResponse>

    @GET("api/Report/user/{userId}")
    suspend fun getReports(@Path("userId") userId: Int): Response<ReportListResponse>

    @POST("api/Report")
    suspend fun createReport(@Body report: CreateReportDto): Response<Unit>

    @GET("api/User/{id}")
    suspend fun getUserProfile(@Path("id") id: Int): Response<UserProfileResponse>

    @PATCH("api/User/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body profile: UpdateProfileDto
    ): Response<Unit>

    @Multipart
    @POST("api/User/{id}/avatar")
    suspend fun updateAvatar(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>
}
