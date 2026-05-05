package com.example.autofeedmobile.network

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val userId: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val avatarUrl: String?,
    val roleId: Int,
    val status: Boolean = true
)

data class UserProfileResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: UserResponse,
    val description: String
)

data class UpdateProfileDto(
    val roleId: Int,
    val email: String,
    val fullName: String,
    val phone: String,
    val username: String
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
    val createdAt: String?,
    @SerializedName("url")
    val fileUrl: String?
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

data class RequestDetailResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: RequestData,
    val description: String
)

data class InventoryData(
    val inventId: Int?,
    val foodId: Int?,
    val foodName: String?,
    val foodType: String?,
    val quantity: Int?,
    val weightPerBag: Int?,
    val totalWeight: Int?,
    val expiredDate: String?,
    val status: String?
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
    val createDate: String?,
    @SerializedName("url")
    val fileUrl: String?
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

data class ReportDetailResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: ReportData,
    val description: String
)

data class ChangePasswordDto(
    val oldPassword: String,
    val newPassword: String
)

data class ResetPasswordDto(
    val email: String
)

data class FlockData(
    val flockId: Int,
    val name: String,
    val quantity: Int,
    val weight: Double,
    val dob: String?,
    @SerializedName("doB")
    val doB: String? = null,
    val healthStatus: String,
    val note: String?,
    val ageInMonths: Int,
    @SerializedName("isActive")
    val isActive: Boolean,
    val transferDate: String? = null,
    val barnId: Int? = null
)

data class FlockListResponse(
    val success: Boolean,
    val data: List<FlockData>,
    val description: String?
)

data class FlockDetailResponse(
    val success: Boolean,
    val data: FlockData,
    val description: String?
)

data class LargeChickenData(
    val chickenLid: Int,
    val flockId: Int,
    val name: String,
    val weight: Double,
    val age: Int,
    val healthStatus: String,
    val note: String?,
    @SerializedName("url")
    val imageUrl: String?,
    @SerializedName("isActive")
    val isActive: Boolean,
    val barnId: Int? = null,
    val ageInMonths: Int? = null,
    val flockName: String? = null,
    val exportDate: String? = null
)

data class UpdateLargeChickenDto(
    val flockId: Int,
    val name: String,
    val weight: Double,
    val age: Int,
    val healthStatus: String,
    val note: String?
)

data class UpdateFlockDto(
    val flockId: Int,
    val name: String,
    val healthStatus: String,
    val weight: Double,
    val note: String?
)

data class TransferFlockDto(
    val sourceFlockId: Int,
    val targetFlockId: Int
)

data class UpdateInventoryDto(
    val quantity: Int,
    val expiredDate: String
)

data class LargeChickenListResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: List<LargeChickenData>,
    val description: String?
)

data class BarnData(
    val barnId: Int,
    val temperature: Double,
    val humidity: Double,
    val foodAmount: Double,
    val waterAmount: Double,
    val type: String,
    val area: Int,
    val createDate: String,
    val status: String,
    val foodToday: Double? = null,
    val foodWeek: Double? = null,
    val foodMonth: Double? = null
)

data class BarnListResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: List<BarnData>,
    val description: String?
)

data class BarnDetailResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: BarnData,
    val description: String?
)

data class LargeChickenDetailResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: LargeChickenData,
    val description: String?
)

data class BarnImageData(
    val imageBarnId: Int,
    val barnId: Int,
    val barnType: String?,
    @SerializedName("url")
    val imageUrl: String,
    val description: String?,
    val captureDate: String
)

data class BarnImageListResponse(
    val status: Boolean,
    val httpCode: Int,
    val data: List<BarnImageData>?,
    val description: String?
)

data class FoodRuleDetailData(
    val foodRuleDetailID: Int,
    val foodId: Int,
    val foodName: String,
    val feedHour: Int,
    val feedMinute: Int,
    val amount: Int,
    val description: String?,
    val status: Boolean
)

data class FeedingRuleData(
    val ruleId: Int,
    val description: String,
    val times: Int,
    val startDate: String,
    val endDate: String,
    val chickenLid: Int?,
    val chickenName: String?,
    val flockId: Int?,
    val flockName: String?,
    val status: String,
    val details: List<FoodRuleDetailData>
)
