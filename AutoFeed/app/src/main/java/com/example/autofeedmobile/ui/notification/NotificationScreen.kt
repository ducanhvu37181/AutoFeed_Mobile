package com.example.autofeedmobile.ui.notification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.autofeedmobile.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val color: Color,
    val indicatorColor: Color,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    onLogout: () -> Unit = {},
    onBack: () -> Unit,
    onNavigateToSchedule: (Int) -> Unit = {},
    onNavigateToRequest: (Int) -> Unit = {},
    onNavigateToReport: (Int) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val apiDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val schedulesDeferred = async { RetrofitClient.instance.getSchedulesByDate(userId, apiDateFormatter.format(Date())) }
                val reportsDeferred = async { RetrofitClient.instance.getReports(userId) }
                val requestsDeferred = async { RetrofitClient.instance.getRequests(userId) }

                val schedulesResponse = schedulesDeferred.await()
                val reportsResponse = reportsDeferred.await()
                val requestsResponse = requestsDeferred.await()

                val newNotifications = mutableListOf<AppNotification>()

                // 2. Schedules
                if (schedulesResponse.isSuccessful) {
                    schedulesResponse.body()?.data?.forEach { schedule ->
                        if (!schedule.status.equals("Completed", ignoreCase = true)) {
                            val title = "Upcoming Schedule"
                            val msg = "Task: ${schedule.taskTitle} is pending"
                            newNotifications.add(AppNotification(schedule.schedId, title, msg, Color(0xFFE8F5E9), Color(0xFF4CAF50), "Schedule"))
                        }
                    }
                }

                // 3. Reports
                if (reportsResponse.isSuccessful) {
                    reportsResponse.body()?.data?.forEach { report ->
                        val status = report.status.lowercase()
                        if (status == "reviewed" || status == "approved" || status == "completed" || status == "rejected") {
                            val isRejected = status == "rejected"
                            val title = if (isRejected) "Report Rejected" else "Report Reviewed"
                            val displayStatus = if (status == "rejected") "rejected" else "Reviewed"
                            val msg = "Your report '${report.type}' is $displayStatus"
                            newNotifications.add(AppNotification(report.reportId, title, msg, if (isRejected) Color(0xFFFFEBEE) else Color(0xFFE8F5E9), if (isRejected) Color.Red else Color(0xFF4CAF50), "Report"))

                            val key = "rep_${report.reportId}_$status"
                            if (NotificationHelper.shouldNotify(context, key)) {
                                NotificationHelper.showNotification(context, title, msg, report.reportId + 3000)
                                NotificationHelper.markAsNotified(context, key)
                            }
                        }
                    }
                }

                // 4. Requests
                if (requestsResponse.isSuccessful) {
                    requestsResponse.body()?.data?.forEach { request ->
                        val status = request.status.lowercase()
                        if (status == "approved" || status == "rejected") {
                            val isRejected = status == "rejected"
                            val title = if (isRejected) "Request Rejected" else "Request Approved"
                            val displayStatus = if (status == "rejected") "rejected" else "approved"
                            val msg = "Your request for '${request.type}' is $displayStatus"
                            newNotifications.add(AppNotification(request.requestId, title, msg, if (isRejected) Color(0xFFFFEBEE) else Color(0xFFE8F5E9), if (isRejected) Color.Red else Color(0xFF4CAF50), "Request"))

                            val key = "req_${request.requestId}_$status"
                            if (NotificationHelper.shouldNotify(context, key)) {
                                NotificationHelper.showNotification(context, title, msg, request.requestId + 2000)
                                NotificationHelper.markAsNotified(context, key)
                            }
                        }
                    }
                }

                notifications = newNotifications
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userAvatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(userAvatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(200.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = userFullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    text = "Farmer",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text("Logout", color = Color(0xFF455A64)) },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = Color(0xFF455A64)
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00897B))
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00897B))
            } else if (notifications.isEmpty()) {
                Text(
                    "No notifications at the moment",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { noti ->
                        NotificationItem(
                            title = noti.title,
                            message = noti.message,
                            color = noti.color,
                            indicatorColor = noti.indicatorColor,
                            onClick = {
                                when (noti.type) {
                                    "Schedule" -> onNavigateToSchedule(noti.id)
                                    "Request" -> onNavigateToRequest(noti.id)
                                    "Report" -> onNavigateToReport(noti.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    message: String,
    color: Color,
    indicatorColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(indicatorColor)
            )
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                    Text(message, fontSize = 12.sp, color = Color(0xFF455A64))
                }
            }
        }
    }
}
