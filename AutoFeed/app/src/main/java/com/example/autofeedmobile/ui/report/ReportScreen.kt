package com.example.autofeedmobile.ui.report

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
    onNavigateToNotifications: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    var reports by remember { mutableStateOf<List<ReportData>>(emptyList()) }
    var inventoryList by remember { mutableStateOf<List<com.example.autofeedmobile.network.InventoryData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Reviewed", "Rejected")

    var searchQuery by remember { mutableStateOf("") }

    val filteredReports = reports.filter { report ->
        val matchesFilter = if (selectedFilter == "All") true
        else report.status.equals(selectedFilter, ignoreCase = true) ||
                (selectedFilter == "Reviewed" && (report.status.equals("completed", ignoreCase = true) || report.status.equals("approved", ignoreCase = true) || report.status.equals("reviewed", ignoreCase = true)))
        val matchesSearch = report.type.contains(searchQuery, ignoreCase = true) ||
                report.description.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }.sortedByDescending { it.createDate }

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
            // Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportSummaryCard(modifier = Modifier.weight(1f), label = "Total", value = reports.size.toString())
                ReportSummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Pending",
                    value = reports.count { it.status.equals("Pending", ignoreCase = true) }.toString(),
                    valueColor = Color(0xFFFFA000)
                )
                ReportSummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Reviewed",
                    value = reports.count { 
                        it.status.equals("Reviewed", ignoreCase = true) || 
                        it.status.equals("Approved", ignoreCase = true) || 
                        it.status.equals("completed", ignoreCase = true) 
                    }.toString(),
                    valueColor = Color(0xFF43A047)
                )
            }

            // Search and Filter
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search reports...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ScrollableTabRow(
                        selectedTabIndex = filters.indexOf(selectedFilter),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        filters.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }

            // Report List
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
                        ReportItem(
                            report = report,
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
            ReportDetailContent(reportId = selectedReportDetail!!.reportId)
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
fun ReportSummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1A1A1A)
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun ReportItem(
    report: ReportData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getReportColor(report.type).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getReportIcon(report.type),
                    contentDescription = null,
                    tint = getReportColor(report.type)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.type,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = report.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            ReportStatusChip(status = report.status)
        }
    }
}

@Composable
fun ReportStatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "completed", "approved", "reviewed" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "pending" -> Color(0xFFFFF8E1) to Color(0xFFF57C00)
        "rejected" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        val displayStatus = when {
            status.equals("completed", ignoreCase = true) || status.equals("approved", ignoreCase = true) -> "Reviewed"
            else -> status
        }
        Text(
            text = displayStatus.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

fun getReportIcon(type: String) = when (type.lowercase()) {
    "feed" -> Icons.Default.Pets
    "maintenance" -> Icons.Default.Build
    "medical" -> Icons.Default.MedicalServices
    "inventory" -> Icons.Default.Inventory2
    else -> Icons.Default.Description
}

fun getReportColor(type: String) = when (type.lowercase()) {
    "feed" -> Color(0xFF4CAF50)
    "maintenance" -> Color(0xFF2196F3)
    "medical" -> Color(0xFFF44336)
    "inventory" -> Color(0xFFFF9800)
    else -> Color(0xFF9C27B0)
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ReportScreen(userId = 1, userFullName = "John Doe", userAvatarUrl = null)
}
