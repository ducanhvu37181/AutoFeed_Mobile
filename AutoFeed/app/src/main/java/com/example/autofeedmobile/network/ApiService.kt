package com.example.autofeedmobile.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/User/reset-password")
    suspend fun forgotPassword(@Body dto: ResetPasswordDto): Response<Unit>

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

    @GET("api/Request/{id}")
    suspend fun getRequestDetail(@Path("id") id: Int): Response<RequestDetailResponse>

    @Multipart
    @POST("api/Request")
    suspend fun createRequest(
        @Part("userId") userId: okhttp3.RequestBody,
        @Part("type") type: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody,
        @Part file: okhttp3.MultipartBody.Part? = null
    ): Response<Unit>

    @GET("api/Inventory")
    suspend fun getInventory(
        @Query("search") search: String? = null,
        @Query("type") type: String? = null
    ): Response<InventoryListResponse>

    @PUT("api/Inventory/{id}")
    suspend fun updateInventory(
        @Path("id") id: Int,
        @Body dto: UpdateInventoryDto
    ): Response<Unit>

    @GET("api/Report/user/{userId}")
    suspend fun getReports(@Path("userId") userId: Int): Response<ReportListResponse>

    @GET("api/Report/{id}")
    suspend fun getReportDetail(@Path("id") id: Int): Response<ReportDetailResponse>

    @Multipart
    @POST("api/Report")
    suspend fun createReport(
        @Part("userId") userId: okhttp3.RequestBody,
        @Part("type") type: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody,
        @Part file: okhttp3.MultipartBody.Part? = null
    ): Response<Unit>

    @GET("api/User/{id}")
    suspend fun getUserProfile(@Path("id") id: Int): Response<UserProfileResponse>

    @PUT("api/User/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body profile: UpdateProfileDto
    ): Response<Unit>

    @PATCH("api/User/{id}/change-password")
    suspend fun changePassword(
        @Path("id") id: Int,
        @Body dto: ChangePasswordDto
    ): Response<Unit>

    @Multipart
    @POST("api/User/{id}/avatar")
    suspend fun updateAvatar(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    @GET("api/Flock")
    suspend fun getFlocks(): Response<FlockListResponse>

    @GET("api/Flock/{id}")
    suspend fun getFlockDetail(@Path("id") id: Int): Response<FlockDetailResponse>

    @PUT("api/Flock/{id}")
    suspend fun updateFlock(
        @Path("id") id: Int,
        @Body flock: UpdateFlockDto
    ): Response<Unit>

    @PUT("api/Flock/transfer-quantity-to-flock")
    suspend fun transferFlock(@Body dto: TransferFlockDto): Response<Unit>

    @PUT("api/Flock/transfer-quantity-back-to-flock")
    suspend fun transferBackToFlock(@Body dto: TransferFlockDto): Response<Unit>

    @GET("api/LargeChicken")
    suspend fun getLargeChickens(): Response<LargeChickenListResponse>

    @GET("api/LargeChicken/{id}")
    suspend fun getLargeChickenDetail(@Path("id") id: Int): Response<LargeChickenDetailResponse>

    @PUT("api/LargeChicken/{id}")
    suspend fun updateLargeChicken(
        @Path("id") id: Int,
        @Body chicken: UpdateLargeChickenDto
    ): Response<Unit>

    @Multipart
    @POST("api/LargeChicken/{id}/avatar")
    suspend fun updateLargeChickenAvatar(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    @GET("api/Barn")
    suspend fun getBarns(): Response<List<BarnData>>

    @GET("api/Barn/{id}")
    suspend fun getBarnDetail(@Path("id") id: Int): Response<BarnData>
}
