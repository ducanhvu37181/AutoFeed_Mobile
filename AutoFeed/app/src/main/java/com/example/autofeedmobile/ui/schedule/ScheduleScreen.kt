package com.example.autofeedmobile.ui.schedule

import com.example.autofeedmobile.util.formatTimeOnly


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.ScheduleData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "In Progress", "Completed")
    var showMenu by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    var schedules by remember { mutableStateOf<List<ScheduleData>>(emptyList()) }
    var inventoryList by remember { mutableStateOf<List<com.example.autofeedmobile.network.InventoryData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Date state
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Detail state
    var selectedTaskDetail by remember { mutableStateOf<ScheduleTask?>(null) }
    var selectedTaskId by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isDetailLoading by remember { mutableStateOf(false) }

    // Formatters
    val displayDateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val apiDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Function to fetch schedules
    fun fetchSchedules() {
        isLoading = true
        scope.launch {
            try {
                val dateString = apiDateFormatter.format(selectedDate.time)
                val response = RetrofitClient.instance.getSchedulesByDate(userId, dateString)
                if (response.isSuccessful) {
                    schedules = response.body()?.data ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load schedules"
                }

                // Also fetch inventory for notifications
                val invResponse = RetrofitClient.instance.getInventory()
                if (invResponse.isSuccessful) {
                    inventoryList = invResponse.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Function to update status
    fun updateStatus(id: Int, newStatus: String) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.updateScheduleStatus(id, newStatus)
                if (response.isSuccessful) {
                    fetchSchedules() // Refresh list
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // Fetch schedules when user or date changes
    LaunchedEffect(userId, selectedDate) {
        fetchSchedules()
    }

    val filteredSchedules = schedules
        .filter { !it.status.equals("Disabled", ignoreCase = true) }
        .let { list ->
            if (selectedFilter == "All") {
                list.sortedWith(compareBy<ScheduleData> { it.status.equals("Completed", ignoreCase = true) }
                    .thenBy { it.startTime })
            } else {
                list.filter { it.status.equals(selectedFilter, ignoreCase = true) }
                    .sortedBy { it.startTime }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AutoFeed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("My Schedule", color = Color.White, fontSize = 14.sp)
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
                            if (userAvatarUrl != null && userAvatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = userAvatarUrl,
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
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = onNavigateToDashboard
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = false,
                    onClick = onNavigateToInventory
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Schedule") },
                    label = { Text("Schedule") },
                    selected = true,
                    onClick = {}
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val isToday = apiDateFormatter.format(selectedDate.time) == apiDateFormatter.format(Date())
                        Text(if (isToday) "Today's Schedule" else "Schedule for", fontSize = 14.sp, color = Color.Gray)
                        Text(displayDateFormatter.format(selectedDate.time), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                val activeSchedules = schedules.filter { !it.status.equals("Disabled", ignoreCase = true) }
                val completedCount = activeSchedules.count { it.status.equals("Completed", ignoreCase = true) }
                val totalCount = activeSchedules.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("$completedCount/$totalCount", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
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

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00897B),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Schedule List
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00897B))
                } else if (errorMessage != null) {
                   Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                       Text(errorMessage!!, color = Color.Red)
                       Button(onClick = { fetchSchedules() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))) {
                           Text("Retry")
                       }
                   }
                } else if (filteredSchedules.isEmpty()) {
                    Text(
                        "No schedules for this day",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredSchedules) { data ->
                            ScheduleItem(
                                title = data.taskTitle,
                                time = "${formatTimeOnly(data.startTime)} - ${formatTimeOnly(data.endTime)}",
                                location = "Barn ${data.barnId}",
                                priority = data.priority,
                                statusColor = when {
                                    data.status.equals("Completed", ignoreCase = true) -> Color(0xFF00C853)
                                    data.status.equals("In Progress", ignoreCase = true) -> Color(0xFFFF9800)
                                    else -> Color(0xFF2196F3)
                                },
                                isCompleted = data.status.equals("Completed", ignoreCase = true),
                                showAction = !data.status.equals("Completed", ignoreCase = true),
                                actionText = if (data.status.equals("Pending", ignoreCase = true)) "Begin Schedule" else "Mark Complete",
                                onClick = {
                                    selectedTaskId = data.schedId
                                    isDetailLoading = true
                                    showBottomSheet = true
                                    scope.launch {
                                        try {
                                            val response = RetrofitClient.instance.getScheduleDetail(data.schedId)
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
                                        } catch (e: Exception) {
                                            // handle error
                                        } finally {
                                            isDetailLoading = false
                                        }
                                    }
                                },
                                onActionClick = {
                                    val nextStatus = if (data.status.equals("Pending", ignoreCase = true)) "In Progress" else "Completed"
                                    updateStatus(data.schedId, nextStatus)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.timeInMillis
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = Calendar.getInstance().apply {
                                timeInMillis = it
                            }
                            selectedDate = newDate
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = Color(0xFF00897B))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Bottom Sheet for Detail View
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false 
                    selectedTaskDetail = null
                    selectedTaskId = -1
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
                            onStatusUpdate = { newStatus ->
                                showBottomSheet = false
                                if (selectedTaskId != -1) {
                                    updateStatus(selectedTaskId, newStatus)
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
fun ScheduleItem(
    title: String,
    time: String,
    location: String,
    priority: String,
    statusColor: Color,
    isCompleted: Boolean,
    showAction: Boolean = false,
    actionText: String = "Mark Complete",
    onClick: () -> Unit = {},
    onActionClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            // Status Indicator Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color.White, CircleShape)
                                .padding(2.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent
                            ) {}
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text(time, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text(location, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                }

                if (showAction) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onActionClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(actionText, color = Color.White)
                    }
                }
            }
            
            IconButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Details", tint = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    ScheduleScreen(userId = 1, userFullName = "John Farmer")
}
