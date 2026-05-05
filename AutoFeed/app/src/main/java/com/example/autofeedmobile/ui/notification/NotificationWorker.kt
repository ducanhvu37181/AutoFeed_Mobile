package com.example.autofeedmobile.ui.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autofeedmobile.data.SessionManager
import com.example.autofeedmobile.network.RetrofitClient
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Worker started at ${java.time.LocalTime.now()}")
        val sessionManager = SessionManager(applicationContext)
        val user = sessionManager.fetchUser() ?: run {
            Log.d("NotificationWorker", "No user found in session")
            return Result.success()
        }
        val userId = user.userId
        val token = sessionManager.fetchAuthToken()
        RetrofitClient.setAuthToken(token)

        val apiService = RetrofitClient.instance

        try {
            // 1 & 2. Schedules (New from today and Upcoming)
            val schedResponse = apiService.getSchedules(userId)
            if (schedResponse.isSuccessful) {
                val schedules = schedResponse.body()?.data ?: emptyList()
                val today = LocalDate.now()
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ISO_DATE_TIME

                schedules.forEach { schedule ->
                    // A. New Schedule Notification (For today or future)
                    try {
                        val scheduleDate = schedule.startDate?.let {
                            if (it.contains("T")) LocalDate.parse(it.substringBefore("T"))
                            else LocalDate.parse(it)
                        }
                        if (scheduleDate != null && !scheduleDate.isBefore(today)) {
                            val key = "sched_${schedule.schedId}_new"
                            if (NotificationHelper.shouldNotify(applicationContext, key)) {
                                val dateDesc = if (scheduleDate.isEqual(today)) "today" else "on $scheduleDate"
                                NotificationHelper.showNotification(
                                    applicationContext,
                                    "New Schedule Assigned",
                                    "New task '${schedule.taskTitle}' is scheduled $dateDesc",
                                    schedule.schedId + 4000,
                                    "Schedule"
                                )
                                NotificationHelper.markAsNotified(applicationContext, key)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NotificationWorker", "Error parsing startDate: ${schedule.startDate}", e)
                    }

                    // B. Upcoming Schedules (Starting Soon)
                    if (schedule.status.lowercase() == "pending" && schedule.startTime != null) {
                        try {
                            val startTime = LocalDateTime.parse(schedule.startTime, formatter)
                            if (startTime.isAfter(now) && startTime.isBefore(now.plusMinutes(5))) {
                                val key = "sched_${schedule.schedId}_starting"
                                if (NotificationHelper.shouldNotify(applicationContext, key)) {
                                    NotificationHelper.showNotification(
                                        applicationContext,
                                        "Upcoming Schedule",
                                        "Schedule ${schedule.taskTitle} starts soon",
                                        schedule.schedId + 1000,
                                        "Schedule"
                                    )
                                    NotificationHelper.markAsNotified(applicationContext, key)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            // 3. Requests
            val reqResponse = apiService.getRequests(userId)
            if (reqResponse.isSuccessful) {
                reqResponse.body()?.data?.forEach { request ->
                    val status = request.status.lowercase()
                    if (status == "approved" || status == "rejected") {
                        val key = "req_${request.requestId}_$status"
                        if (NotificationHelper.shouldNotify(applicationContext, key)) {
                            val isRejected = status == "rejected"
                            val title = if (isRejected) "Request Rejected" else "Request Approved"
                            val displayStatus = if (isRejected) "rejected" else "approved"
                            NotificationHelper.showNotification(
                                applicationContext,
                                title,
                                "Your request for '${request.type}' is now $displayStatus",
                                request.requestId + 2000,
                                "Requests"
                            )
                            NotificationHelper.markAsNotified(applicationContext, key)
                        }
                    }
                }
            }

            // 4. Reports
            val repResponse = apiService.getReports(userId)
            if (repResponse.isSuccessful) {
                repResponse.body()?.data?.forEach { report ->
                    val status = report.status.lowercase()
                    if (status == "reviewed" || status == "approved" || status == "completed" || status == "rejected") {
                        val key = "rep_${report.reportId}_$status"
                        if (NotificationHelper.shouldNotify(applicationContext, key)) {
                            val isRejected = status == "rejected"
                            val title = if (isRejected) "Report Rejected" else "Report Updated"
                            val displayStatus = status.replaceFirstChar { it.uppercase() }
                            NotificationHelper.showNotification(
                                applicationContext,
                                title,
                                "Your report #${report.reportId} ('${report.type}') is now $displayStatus",
                                report.reportId + 3000,
                                "Reports"
                            )
                            NotificationHelper.markAsNotified(applicationContext, key)
                        }
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
