package com.example.autofeedmobile.ui.dashboard

import com.example.autofeedmobile.ui.schedule.ScheduleTask
import com.example.autofeedmobile.ui.schedule.ScheduleDetailContent
import com.example.autofeedmobile.ui.request.RequestDetailContent
import com.example.autofeedmobile.ui.report.ReportDetailContent
import com.example.autofeedmobile.util.formatTimeOnly


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.InventoryData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.ScheduleData
import com.example.autofeedmobile.network.ReportData
import com.example.autofeedmobile.network.RequestData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    onLogout: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToRequests: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToChickenManagement: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var schedules by remember { mutableStateOf<List<ScheduleData>>(emptyList()) }
    var inventoryList by remember { mutableStateOf<List<InventoryData>>(emptyList()) }
    var reports by remember { mutableStateOf<List<ReportData>>(emptyList()) }
    var requests by remember { mutableStateOf<List<RequestData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Key to force recomposition of avatar when URL changes
    val avatarUrlKey = remember(userAvatarUrl) { userAvatarUrl }

    // Detail state
    var selectedTaskDetail by remember { mutableStateOf<ScheduleTask?>(null) }
    var selectedTaskId by remember { mutableIntStateOf(-1) }
    var selectedRequestId by remember { mutableIntStateOf(-1) }
    var selectedReportId by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isDetailLoading by remember { mutableStateOf(false) }

    val apiDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val fullDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }

    fun isRecent(dateString: String?): Boolean {
        if (dateString == null) return false
        return try {
            val date = if (dateString.contains("T")) {
                fullDateFormatter.parse(dateString)
            } else {
                apiDateFormatter.parse(dateString)
            }
            val now = Calendar.getInstance()
            val twoDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
            date?.after(twoDaysAgo.time) == true
        } catch (e: Exception) {
            false
        }
    }

    fun fetchDashboardData() {
        isLoading = true
        scope.launch {
            try {
                // Fetch Today's Schedules
                val today = apiDateFormatter.format(Date())
                val scheduleResponse = RetrofitClient.instance.getSchedulesByDate(userId, today)
                if (scheduleResponse.isSuccessful) {
                    schedules = (scheduleResponse.body()?.data ?: emptyList())
                        .filter { !it.status.equals("Disabled", ignoreCase = true) }
                        .sortedWith(compareBy<ScheduleData> { it.status.equals("Completed", ignoreCase = true) }
                            .thenBy { it.startTime })
                }
                
                // Fetch Inventory for Summary
                val inventoryResponse = RetrofitClient.instance.getInventory()
                if (inventoryResponse.isSuccessful) {
                    inventoryList = inventoryResponse.body()?.data ?: emptyList()
                }

                // Fetch Reports for Summary
                val reportResponse = RetrofitClient.instance.getReports(userId)
                if (reportResponse.isSuccessful) {
                    reports = reportResponse.body()?.data ?: emptyList()
                }

                // Fetch Requests for Summary
                val requestResponse = RetrofitClient.instance.getRequests(userId)
                if (requestResponse.isSuccessful) {
                    requests = requestResponse.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                isLoading = false
            }
        }
    }

    fun updateStatus(id: Int, newStatus: String) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.updateScheduleStatus(id, newStatus)
                if (response.isSuccessful) {
                    fetchDashboardData() // Refresh dashboard
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    LaunchedEffect(userId) {
        fetchDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AutoFeed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Dashboard Overview", color = Color.White, fontSize = 14.sp)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                        if (inventoryList.any { it.quantity < 3 }) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUrlKey.isNullOrEmpty()) {
                            AsyncImage(
                                model = avatarUrlKey,
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
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
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = false,
                    onClick = onNavigateToInventory
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Chicken") },
                    label = { Text("Chicken") },
                    selected = false,
                    onClick = onNavigateToChickenManagement
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Schedule") },
                    label = { Text("Schedule") },
                    selected = false,
                    onClick = onNavigateToSchedule
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards Grid
                item {
                    val criticalStockCount = inventoryList.count { it.quantity < 3 }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard(
                                modifier = Modifier.weight(1f).clickable(onClick = onNavigateToReports),
                                icon = Icons.Default.Assessment,
                                iconContainerColor = Color(0xFF1DB954).copy(alpha = 0.1f),
                                iconColor = Color(0xFF1DB954),
                                value = "${reports.size}",
                                label = "My Reports"
                            )
                            SummaryCard(
                                modifier = Modifier.weight(1f).clickable(onClick = onNavigateToInventory),
                                icon = Icons.Default.Inventory2,
                                iconContainerColor = Color(0xFF2196F3).copy(alpha = 0.1f),
                                iconColor = Color(0xFF2196F3),
                                value = "${inventoryList.size}",
                                label = "Inventory Items"
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard(
                                modifier = Modifier.weight(1f).clickable(onClick = onNavigateToSchedule),
                                icon = Icons.Default.ElectricBolt,
                                iconContainerColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
                                iconColor = Color(0xFF9C27B0),
                                value = "${schedules.size}",
                                label = "Today Schedules"
                            )
                            SummaryCard(
                                modifier = Modifier.weight(1f).clickable(onClick = onNavigateToRequests),
                                icon = Icons.Default.ChatBubbleOutline,
                                iconContainerColor = Color(0xFFFF5722).copy(alpha = 0.1f),
                                iconColor = Color(0xFFFF5722),
                                value = "${requests.size}",
                                label = "My Request"
                            )
                        }
                    }
                }

                // Today's Schedules Section
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Today's Schedules", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            TextButton(onClick = onNavigateToSchedule) {
                                Text("View All", color = Color(0xFF00897B))
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF00897B))
                                }
                            } else if (schedules.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                                    Text("No schedules today", color = Color.Gray)
                                }
                            } else {
                                Column {
                                    schedules.take(4).forEachIndexed { index, schedule ->
                                        DashboardTaskItem(
                                            title = schedule.taskTitle,
                                            time = formatTimeOnly(schedule.startTime),
                                            priority = schedule.priority,
                                            isCompleted = schedule.status.equals("Completed", ignoreCase = true),
                                            onClick = {
                                                selectedTaskId = schedule.schedId
                                                isDetailLoading = true
                                                showBottomSheet = true
                                                scope.launch {
                                                    try {
                                                        val response = RetrofitClient.instance.getScheduleDetail(schedule.schedId)
                                                        if (response.isSuccessful && response.body()?.data != null) {
                                                            val detail = response.body()!!.data!!
                                                            selectedTaskDetail = ScheduleTask(
                                                                title = detail.taskTitle,
                                                                status = detail.status.replaceFirstChar { it.uppercase() },
                                                                priority = detail.priority.replaceFirstChar { it.uppercase() },
                                                                time = "${formatTimeOnly(detail.startTime)} - ${formatTimeOnly(detail.endTime)}",
                                                                location = "Barn ${detail.barnId}",
                                                                details = detail.description,
                                                                note = detail.note
                                                            )
                                                        }
                                                    } catch (e: Exception) { } finally {
                                                        isDetailLoading = false
                                                    }
                                                }
                                            }
                                        )
                                        if (index < schedules.take(4).size - 1) {
                                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Today Schedule Completed Section
                item {
                    val completedCount = schedules.count { it.status.equals("Completed", ignoreCase = true) }
                    val totalCount = schedules.size
                    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                    val percentage = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0

                    Column {
                        Text("Today Schedule Completed", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Schedules Completed", fontSize = 12.sp, color = Color.Gray)
                                        Text("$completedCount/$totalCount", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(16.dp))
                                        Text("$percentage%", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = Color(0xFF00897B),
                                    trackColor = Color(0xFFE0F2F1),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // Recent Requests Section
                item {
                    val recentRequests = requests.filter { isRecent(it.createdAt) }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Requests", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            TextButton(onClick = onNavigateToRequests) {
                                Text("View All", color = Color(0xFF00897B))
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF00897B))
                                }
                            } else if (recentRequests.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No recent requests", color = Color.Gray)
                                }
                            } else {
                                Column {
                                    recentRequests.take(3).forEachIndexed { index, request ->
                                        DashboardNotificationItem(
                                            title = request.type,
                                            message = request.description,
                                            color = Color.White,
                                            indicatorColor = when (request.status.lowercase()) {
                                                "approved" -> Color(0xFF00C853)
                                                "pending" -> Color(0xFFFFA000)
                                                "rejected" -> Color(0xFFD32F2F)
                                                else -> Color.Gray
                                            },
                                            onClick = {
                                                selectedRequestId = request.requestId
                                                showBottomSheet = true
                                            }
                                        )
                                        if (index < recentRequests.take(3).size - 1) {
                                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Reports Section
                item {
                    val recentReports = reports.filter { isRecent(it.createDate) }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Reports", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            TextButton(onClick = onNavigateToReports) {
                                Text("View All", color = Color(0xFF00897B))
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF00897B))
                                }
                            } else if (recentReports.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No recent reports", color = Color.Gray)
                                }
                            } else {
                                Column {
                                    recentReports.take(3).forEachIndexed { index, report ->
                                        DashboardNotificationItem(
                                            title = report.type,
                                            message = report.description,
                                            color = Color.White,
                                            indicatorColor = when (report.status.lowercase()) {
                                                "reviewed", "completed", "approved" -> Color(0xFF00C853)
                                                "pending" -> Color(0xFFFFA000)
                                                "rejected" -> Color(0xFFD32F2F)
                                                else -> Color.Gray
                                            },
                                            onClick = {
                                                selectedReportId = report.reportId
                                                showBottomSheet = true
                                            }
                                        )
                                        if (index < recentReports.take(3).size - 1) {
                                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheet for Detail View
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false 
                    selectedTaskDetail = null
                    selectedTaskId = -1
                    selectedRequestId = -1
                    selectedReportId = -1
                },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = null
            ) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp)) {
                    if (isDetailLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00A67E))
                    } else if (selectedTaskDetail != null) {
                        ScheduleDetailContent(
                            task = selectedTaskDetail!!,
                            selectedDate = Calendar.getInstance(), // Dashboard always shows today's schedules
                            onStatusUpdate = { newStatus ->
                                showBottomSheet = false
                                if (selectedTaskId != -1) {
                                    updateStatus(selectedTaskId, newStatus)
                                }
                            }
                        )
                    } else if (selectedRequestId != -1) {
                        RequestDetailContent(requestId = selectedRequestId)
                    } else if (selectedReportId != -1) {
                        ReportDetailContent(reportId = selectedReportId)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = iconContainerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DashboardNotificationItem(
    title: String,
    message: String,
    color: Color,
    indicatorColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
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
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = Color(0xFF455A64),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

@Composable
fun DashboardTaskItem(
    title: String,
    time: String,
    priority: String,
    isCompleted: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCompleted) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(20.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White, CircleShape)
                    .padding(1.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {}
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                
                val priorityColor = when (priority.lowercase()) {
                    "high" -> Color(0xFFFFEBEE)
                    "medium" -> Color(0xFFFFF3E0)
                    "low" -> Color(0xFFE8F5E9)
                    else -> Color(0xFFF5F5F5)
                }
                val priorityTextColor = when (priority.lowercase()) {
                    "high" -> Color(0xFFD32F2F)
                    "medium" -> Color(0xFFEF6C00)
                    "low" -> Color(0xFF2E7D32)
                    else -> Color(0xFF616161)
                }
                
                Surface(
                    color = priorityColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = priority,
                        color = priorityTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(time, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}
