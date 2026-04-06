package com.example.autofeedmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.ReportData
import com.example.autofeedmobile.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onBackToProfile: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    var reports by remember { mutableStateOf<List<ReportData>>(emptyList()) }
    var inventoryList by remember { mutableStateOf<List<com.example.autofeedmobile.network.InventoryData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Approved", "Rejected")

    val filteredReports = remember(reports, selectedFilter) {
        if (selectedFilter == "All") reports
        else reports.filter { 
            it.status.equals(selectedFilter, ignoreCase = true) || 
            (selectedFilter == "Approved" && it.status.equals("completed", ignoreCase = true))
        }
    }

    // Detail state
    var selectedReportDetail by remember { mutableStateOf<ReportData?>(null) }
    var showDetailBottomSheet by remember { mutableStateOf(false) }
    var showCreateBottomSheet by remember { mutableStateOf(false) }
    
    val detailSheetState = rememberModalBottomSheetState()
    val createSheetState = rememberModalBottomSheetState()

    fun fetchReports() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getReports(userId)
                if (response.isSuccessful) {
                    reports = response.body()?.data ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load reports"
                }

                // Also fetch inventory for alerts
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

    LaunchedEffect(userId) {
        fetchReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AutoFeed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("My Reports", color = Color.White, fontSize = 14.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToProfile) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Profile", tint = Color.White)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNavigateToAlerts) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
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
                    selected = false,
                    onClick = onNavigateToSchedule
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onBackToProfile
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBottomSheet = true },
                containerColor = Color(0xFF00897B),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Report")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Filter Tabs
            TabRow(
                selectedTabIndex = filters.indexOf(selectedFilter),
                containerColor = Color.White,
                contentColor = Color(0xFF00897B),
                divider = {}
            ) {
                filters.forEach { filter ->
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        text = { 
                            Text(
                                filter, 
                                fontSize = 13.sp,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00897B))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage!!, color = Color.Red)
                        Button(onClick = { fetchReports() }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Retry")
                        }
                    }
                }
            } else if (filteredReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (selectedFilter == "All") "No reports found" else "No $selectedFilter reports found", 
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReports) { report ->
                        ReportItemCard(
                            type = report.type,
                            date = report.createDate.split("T")[0],
                            status = report.status,
                            onClick = {
                                selectedReportDetail = report
                                showDetailBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDetailBottomSheet && selectedReportDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showDetailBottomSheet = false 
                selectedReportDetail = null
            },
            sheetState = detailSheetState,
            containerColor = Color.White,
            dragHandle = null
        ) {
            ReportDetailContent(report = selectedReportDetail!!)
        }
    }

    if (showCreateBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateBottomSheet = false },
            sheetState = createSheetState,
            containerColor = Color.White,
            dragHandle = null
        ) {
            SendReportContent(
                userId = userId,
                onSuccess = {
                    showCreateBottomSheet = false
                    fetchReports()
                },
                onCancel = { showCreateBottomSheet = false }
            )
        }
    }
}

@Composable
fun ReportItemCard(
    type: String,
    date: String,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(type, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(date, fontSize = 12.sp, color = Color.Gray)
                }
                
                val statusColor = when (status.lowercase()) {
                    "pending" -> Color(0xFFFFF8E1)
                    "completed", "approved" -> Color(0xFFE8F5E9)
                    "rejected" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFF5F5F5)
                }
                val statusTextColor = when (status.lowercase()) {
                    "pending" -> Color(0xFFFFA000)
                    "completed", "approved" -> Color(0xFF00C853)
                    "rejected" -> Color(0xFFD32F2F)
                    else -> Color(0xFF757575)
                }

                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val displayStatus = if (status.equals("completed", ignoreCase = true)) "Approved" else status
                    Text(
                        text = displayStatus.replaceFirstChar { it.uppercase() },
                        color = statusTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ReportScreen(userId = 1, userFullName = "John Doe", userAvatarUrl = null)
}
