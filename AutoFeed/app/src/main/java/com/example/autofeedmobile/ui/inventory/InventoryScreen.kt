package com.example.autofeedmobile.ui.inventory

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.InventoryData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.ui.report.SendReportContent
import com.example.autofeedmobile.ui.request.SendRequestContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    hasNewNotifications: Boolean = false,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChickenManagement: () -> Unit = {},
    onNavigateToBarnManagement: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    var inventoryList by remember { mutableStateOf<List<InventoryData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Floating Action Button State
    var showQuickActions by remember { mutableStateOf(false) }
    var showRequestSheet by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }

    var selectedItem by remember { mutableStateOf<InventoryData?>(null) }
    var showDetail by remember { mutableStateOf(false) }

    // Function to fetch inventory
    fun fetchInventory() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getInventory()
                if (response.isSuccessful) {
                    inventoryList = response.body()?.data ?: emptyList()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to load inventory"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchInventory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AutoFeed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Inventory Management", color = Color.White, fontSize = 14.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
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
                    selected = true,
                    onClick = {}
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
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showQuickActions) {
                    SmallFloatingActionButton(
                        onClick = { 
                            showQuickActions = false
                            showReportSheet = true 
                        },
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Report, contentDescription = "Send Report")
                    }
                    SmallFloatingActionButton(
                        onClick = { 
                            showQuickActions = false
                            showRequestSheet = true 
                        },
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Send Request")
                    }
                }
                
                FloatingActionButton(
                    onClick = { showQuickActions = !showQuickActions },
                    containerColor = Color(0xFF00897B),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        if (showQuickActions) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Quick Actions"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InventorySummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Inventory,
                    label = "Items",
                    value = inventoryList.size.toString(),
                    color = Color(0xFFE3F2FD)
                )
                InventorySummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning,
                    label = "Low Stock",
                    value = inventoryList.count { it.quantity <= 5 }.toString(),
                    color = Color(0xFFFFF3E0)
                )
            }

            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    placeholder = { Text("Search inventory...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Inventory List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00897B))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage!!, color = Color.Red)
                }
            } else {
                val filteredList = inventoryList.filter { 
                    it.foodName.contains(searchQuery, ignoreCase = true) || 
                    it.foodType.contains(searchQuery, ignoreCase = true)
                }

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items found", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList) { item ->
                            val isLowStock = item.quantity <= 5
                            
                            val (status, statusColor) = try {
                                val expireDate = LocalDate.parse(item.expiredDate.split("T")[0])
                                val today = LocalDate.now()
                                val daysUntil = ChronoUnit.DAYS.between(today, expireDate)
                                
                                when {
                                    expireDate.isBefore(today) -> "Expired" to Color.Red
                                    daysUntil <= 3 -> "Nearly Expired" to Color(0xFFF57C00) // Orange
                                    isLowStock -> "Low Stock" to Color.Red
                                    else -> "Good" to Color(0xFF00897B)
                                }
                            } catch (e: Exception) {
                                (if (isLowStock) "Low Stock" else "In Stock") to (if (isLowStock) Color.Red else Color(0xFF00897B))
                            }

                            InventoryItemCard(
                                name = item.foodName,
                                category = item.foodType,
                                quantity = "${item.quantity} Bags",
                                lastUpdated = "Today", // Ideally from API
                                status = status,
                                statusColor = statusColor,
                                onClick = {
                                    selectedItem = item
                                    showDetail = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sheets
        if (showRequestSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRequestSheet = false },
                containerColor = Color.White
            ) {
                SendRequestContent(
                    userId = userId,
                    initialType = "Inventory",
                    canEditType = false,
                    onSuccess = {
                        showRequestSheet = false
                        fetchInventory()
                    },
                    onCancel = { showRequestSheet = false }
                )
            }
        }

        if (showReportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showReportSheet = false },
                containerColor = Color.White
            ) {
                SendReportContent(
                    userId = userId,
                    initialType = "Inventory",
                    canEditType = false,
                    onSuccess = {
                        showReportSheet = false
                        fetchInventory()
                    },
                    onCancel = { showReportSheet = false }
                )
            }
        }

        if (showDetail && selectedItem != null) {
            ModalBottomSheet(
                onDismissRequest = { showDetail = false },
                containerColor = Color.White
            ) {
                InventoryDetailContent(
                    item = selectedItem!!,
                    userId = userId,
                    onRefresh = {
                        fetchInventory()
                        showDetail = false
                    }
                )
            }
        }
    }
}

@Composable
fun InventorySummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF00897B))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    name: String,
    category: String,
    quantity: String,
    lastUpdated: String,
    status: String,
    statusColor: Color,
    onClick: () -> Unit
) {
    val isWarning = status == "Low Stock" || status == "Expired" || status == "Nearly Expired"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isWarning) Icons.Default.Warning else Icons.Default.Inventory,
                    contentDescription = null,
                    tint = statusColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(category, fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(quantity, fontWeight = FontWeight.Bold, color = if (status == "Low Stock") Color.Red else Color.Black)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    InventoryScreen(userId = 1, userFullName = "John Doe")
}
