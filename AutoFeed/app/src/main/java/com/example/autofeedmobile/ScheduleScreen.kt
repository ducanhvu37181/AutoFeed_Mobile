package com.example.autofeedmobile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.ScheduleData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    userId: Int,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToRequests: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "In Progress", "Completed")
    var showMenu by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    var schedules by remember { mutableStateOf<List<ScheduleData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Detail state
    var selectedTaskDetail by remember { mutableStateOf<ScheduleTask?>(null) }
    var selectedTaskId by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isDetailLoading by remember { mutableStateOf(false) }

    // Today's Date
    val todayDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    // Function to fetch schedules
    fun fetchSchedules() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getSchedules(userId)
                if (response.isSuccessful) {
                    schedules = response.body()?.data ?: emptyList()
                } else {
                    errorMessage = "Failed to load schedules"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Function to update status
    fun updateStatus(id: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.updateScheduleStatus(id, "Completed")
                if (response.isSuccessful) {
                    fetchSchedules() // Refresh list
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // Fetch schedules on start
    LaunchedEffect(userId) {
        fetchSchedules()
    }

    val filteredSchedules = if (selectedFilter == "All") {
        schedules
    } else {
        schedules.filter { it.status.equals(selectedFilter, ignoreCase = true) }
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
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
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
                                    text = "John Farmer",
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
                    icon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Requests") },
                    label = { Text("Requests") },
                    selected = false,
                    onClick = onNavigateToRequests
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
                        Text("Today's Schedule", fontSize = 14.sp, color = Color.Gray)
                        Text(todayDate, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                val completedCount = schedules.count { it.status.equals("Completed", ignoreCase = true) }
                val totalCount = schedules.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress Today", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                } else if (filteredSchedules.isEmpty()) {
                    Text(
                        "No schedule",
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
                                statusColor = if (data.status.equals("Completed", ignoreCase = true)) Color(0xFF00C853) else Color(0xFF2196F3),
                                isCompleted = data.status.equals("Completed", ignoreCase = true),
                                showAction = !data.status.equals("Completed", ignoreCase = true),
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
                                onMarkComplete = {
                                    updateStatus(data.schedId)
                                }
                            )
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
                            onMarkComplete = {
                                showBottomSheet = false
                                if (selectedTaskId != -1) {
                                    updateStatus(selectedTaskId)
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
    statusColor: Color,
    isCompleted: Boolean,
    showAction: Boolean = false,
    onClick: () -> Unit = {},
    onMarkComplete: () -> Unit = {}
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
                        onClick = { onMarkComplete() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Mark Complete", color = Color.White)
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
