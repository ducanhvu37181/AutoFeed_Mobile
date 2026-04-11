package com.example.autofeedmobile

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
import com.example.autofeedmobile.network.InventoryData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.ScheduleData
import com.example.autofeedmobile.network.ReportData
import com.example.autofeedmobile.network.RequestData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val title: String,
    val message: String,
    val color: Color,
    val indicatorColor: Color,
    val type: String // "Inventory", "Schedule", "Report", "Request"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    onLogout: () -> Unit = {},
    onBack: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val apiDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeParser = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val inventoryDeferred = async { RetrofitClient.instance.getInventory() }
                val schedulesDeferred = async { RetrofitClient.instance.getSchedulesByDate(userId, apiDateFormatter.format(Date())) }
                val reportsDeferred = async { RetrofitClient.instance.getReports(userId) }
                val requestsDeferred = async { RetrofitClient.instance.getRequests(userId) }

                val inventoryResponse = inventoryDeferred.await()
                val schedulesResponse = schedulesDeferred.await()
                val reportsResponse = reportsDeferred.await()
                val requestsResponse = requestsDeferred.await()

                val newNotifications = mutableListOf<AppNotification>()

                // 1. Inventory Notifications
                if (inventoryResponse.isSuccessful) {
                    val items = inventoryResponse.body()?.data ?: emptyList()
                    items.forEach { item ->
                        if (item.quantity == 0) {
                            newNotifications.add(AppNotification("Out of Stock", "${item.foodName} is empty", Color(0xFFFFEBEE), Color.Red, "Inventory"))
                        } else if (item.quantity in 1..2) {
                            newNotifications.add(AppNotification("Low Stock", "${item.foodName} below minimum (${item.quantity} left)", Color(0xFFFFF3E0), Color(0xFFFF9800), "Inventory"))
                        }
                    }
                }

                // 2. Schedule Notifications (5 mins before)
                if (schedulesResponse.isSuccessful) {
                    val schedules = schedulesResponse.body()?.data ?: emptyList()
                    val now = Calendar.getInstance()
                    schedules.forEach { schedule ->
                        if (!schedule.status.equals("Completed", ignoreCase = true)) {
                            try {
                                val startTime = Calendar.getInstance()
                                startTime.time = timeParser.parse(schedule.startTime!!)!!
                                
                                val diffMillis = startTime.timeInMillis - now.timeInMillis
                                val diffMinutes = diffMillis / (60 * 1000)
                                
                                if (diffMinutes in 0..5) {
                                    newNotifications.add(AppNotification("Upcoming Schedule", "${schedule.taskTitle} starts in $diffMinutes mins", Color(0xFFE8F5E9), Color(0xFF4CAF50), "Schedule"))
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }

                // 3. Reports Notifications (Approved/Rejected)
                if (reportsResponse.isSuccessful) {
                    val reports = reportsResponse.body()?.data ?: emptyList()
                    reports.forEach { report ->
                        if (report.status.equals("Approved", ignoreCase = true)) {
                            newNotifications.add(AppNotification("Report Approved", "Your report '${report.type}' has been approved", Color(0xFFE8F5E9), Color(0xFF4CAF50), "Report"))
                        } else if (report.status.equals("Rejected", ignoreCase = true)) {
                            newNotifications.add(AppNotification("Report Rejected", "Your report '${report.type}' was rejected", Color(0xFFFFEBEE), Color.Red, "Report"))
                        }
                    }
                }

                // 4. Requests Notifications (Approved/Rejected)
                if (requestsResponse.isSuccessful) {
                    val requests = requestsResponse.body()?.data ?: emptyList()
                    requests.forEach { request ->
                        if (request.status.equals("Approved", ignoreCase = true)) {
                            newNotifications.add(AppNotification("Request Approved", "Your request for '${request.type}' has been approved", Color(0xFFE8F5E9), Color(0xFF4CAF50), "Request"))
                        } else if (request.status.equals("Rejected", ignoreCase = true)) {
                            newNotifications.add(AppNotification("Request Rejected", "Your request for '${request.type}' was rejected", Color(0xFFFFEBEE), Color.Red, "Request"))
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showMenu = true }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = userFullName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Farmer",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
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
                            indicatorColor = noti.indicatorColor
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
