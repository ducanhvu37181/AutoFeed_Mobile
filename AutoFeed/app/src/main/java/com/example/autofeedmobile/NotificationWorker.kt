package com.example.autofeedmobile

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autofeedmobile.network.RetrofitClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Worker started")
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
            // 1. Inventory
            val invResponse = apiService.getInventory()
            if (invResponse.isSuccessful) {
                invResponse.body()?.data?.forEach { item ->
                    if (item.quantity < 3) {
                        val key = "inv_${item.inventId}_low"
                        if (NotificationHelper.shouldNotify(applicationContext, key)) {
                            NotificationHelper.showNotification(
                                applicationContext,
                                if (item.quantity == 0) "Out of Stock" else "Low Stock",
                                "${item.foodName} is low in stock (${item.quantity})",
                                item.inventId
                            )
                            NotificationHelper.markAsNotified(applicationContext, key)
                        }
                    }
                }
            }

            // 2. Schedules
            val schedResponse = apiService.getSchedules(userId)
            if (schedResponse.isSuccessful) {
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                schedResponse.body()?.data?.forEach { schedule ->
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
                                        schedule.schedId + 1000
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
                    if (status == "approved" || status == "reviewed" || status == "rejected") {
                        val key = "req_${request.requestId}_$status"
                        if (NotificationHelper.shouldNotify(applicationContext, key)) {
                            NotificationHelper.showNotification(
                                applicationContext,
                                if (status == "rejected") "Request Rejected" else "Request Approved",
                                "Your request for '${request.type}' is now $status",
                                request.requestId + 2000
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
                    if (status == "reviewed" || status == "approved" || status == "rejected") {
                        val key = "rep_${report.reportId}_$status"
                        if (NotificationHelper.shouldNotify(applicationContext, key)) {
                            NotificationHelper.showNotification(
                                applicationContext,
                                if (status == "rejected") "Report Rejected" else "Report Approved",
                                "Your report '${report.type}' is now $status",
                                report.reportId + 3000
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
