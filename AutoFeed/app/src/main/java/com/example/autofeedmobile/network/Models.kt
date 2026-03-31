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

data class ScheduleData(
    val schedId: Int,
    val userId: Int,
    val taskId: Int,
    val cbarnId: Int,
    val description: String,
    val note: String?,
    val priority: String,
    val status: String,
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val barnId: Int,
    val taskTitle: String,
    val username: String
)

data class ScheduleListResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: List<ScheduleData>,
    val description: String
)

data class ScheduleDetailResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: ScheduleData,
    val description: String
)

data class RequestData(
    val requestId: Int,
    val userId: Int,
    val type: String,
    val description: String,
    val status: String,
    val createdAt: String
)

data class RequestListResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: List<RequestData>?,
    val description: String
)

data class CreateRequestDto(
    val userId: Int,
    val type: String,
    val description: String
)

data class InventoryData(
    val inventId: Int,
    val foodId: Int,
    val foodName: String,
    val foodType: String,
    val quantity: Int,
    val weightPerBag: Int,
    val totalWeight: Int,
    val expiredDate: String,
    val status: String
)

data class InventoryListResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: List<InventoryData>,
    val description: String
)

data class ReportData(
    val reportId: Int,
    val userId: Int,
    val userName: String,
    val userRole: String?,
    val type: String,
    val description: String,
    val status: String,
    val createDate: String
)

data class ReportListResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: List<ReportData>?,
    val description: String
)

data class CreateReportDto(
    val userId: Int,
    val type: String,
    val description: String
)
