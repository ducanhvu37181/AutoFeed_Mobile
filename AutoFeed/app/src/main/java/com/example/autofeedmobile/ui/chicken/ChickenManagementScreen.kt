package com.example.autofeedmobile.ui.chicken

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import java.util.Locale
import com.example.autofeedmobile.network.FlockData
import com.example.autofeedmobile.network.LargeChickenData
import com.example.autofeedmobile.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChickenManagementScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChickenManagement: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Flock", "Large Chicken")
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var statusFilter by remember { mutableStateOf("Active") }
    val flockFilterOptions = listOf("All", "Active", "Transferred")
    val chickenFilterOptions = listOf("All", "Active", "Exported")

    var flocks by remember { mutableStateOf<List<FlockData>>(emptyList()) }
    var largeChickens by remember { mutableStateOf<List<LargeChickenData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedFlock by remember { mutableStateOf<FlockData?>(null) }
    var selectedLargeChicken by remember { mutableStateOf<LargeChickenData?>(null) }
    var showFlockDetail by remember { mutableStateOf(false) }
    var showLargeChickenDetail by remember { mutableStateOf(false) }

    fun fetchData() {
        isLoading = true
        scope.launch {
            try {
                if (selectedTab == 0) {
                    val response = RetrofitClient.instance.getFlocks()
                    if (response.isSuccessful) {
                        flocks = response.body()?.data ?: emptyList()
                    }
                } else {
                    val response = RetrofitClient.instance.getLargeChickens()
                    if (response.isSuccessful) {
                        largeChickens = response.body()?.data ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedTab) {
        fetchData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AutoFeed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Chicken Management", color = Color.White, fontSize = 14.sp)
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
                        if (!userAvatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = userAvatarUrl,
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White).width(200.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(userFullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Farmer", fontSize = 12.sp, color = Color.Gray)
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
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
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Chicken") },
                    label = { Text("Chicken") },
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
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF00897B),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF00897B)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentFilterOptions = if (selectedTab == 0) flockFilterOptions else chickenFilterOptions
                currentFilterOptions.forEach { option ->
                    FilterChip(
                        selected = statusFilter == option,
                        onClick = { statusFilter = option },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00897B).copy(alpha = 0.1f),
                            selectedLabelColor = Color(0xFF00897B)
                        )
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00897B))
                }
            } else {
                val filteredFlocks = when (statusFilter) {
                    "Active" -> flocks.filter { it.isActive }
                    "Transferred" -> flocks.filter { !it.isActive }
                    else -> flocks
                }
                
                val filteredChickens = when (statusFilter) {
                    "Active" -> largeChickens.filter { it.isActive }
                    "Exported" -> largeChickens.filter { !it.isActive }
                    else -> largeChickens
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        items(filteredFlocks) { flock ->
                            FlockItem(flock, onClick = {
                                selectedFlock = flock
                                showFlockDetail = true
                            })
                        }
                    } else {
                        items(filteredChickens) { chicken ->
                            LargeChickenItem(chicken, onClick = {
                                selectedLargeChicken = chicken
                                showLargeChickenDetail = true
                            })
                        }
                    }
                }
            }
        }

        if (showFlockDetail && selectedFlock != null) {
            ModalBottomSheet(
                onDismissRequest = { showFlockDetail = false },
                containerColor = Color.White
            ) {
                FlockDetailView(flockId = selectedFlock!!.flockId)
            }
        }

        if (showLargeChickenDetail && selectedLargeChicken != null) {
            ModalBottomSheet(
                onDismissRequest = { showLargeChickenDetail = false },
                containerColor = Color.White
            ) {
                LargeChickenDetailView(
                    chickenId = selectedLargeChicken!!.chickenLid,
                    onRefresh = { fetchData() }
                )
            }
        }
    }
}

@Composable
fun FlockItem(flock: FlockData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(flock.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                StatusBadge(flock.healthStatus)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoColumn(
                    label = "Weight per Flock",
                    value = "${flock.weight} kg",
                    modifier = Modifier.weight(1f)
                )
                if (flock.isActive && flock.barnId != null) {
                    InfoColumn(
                        label = "Barn ID",
                        value = "#${flock.barnId}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (!flock.note.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Note: ${flock.note}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun LargeChickenItem(chicken: LargeChickenData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = RetrofitClient.getFullUrl(chicken.imageUrl),
                contentDescription = chicken.name,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(chicken.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    StatusBadge(chicken.healthStatus)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Weight: ${chicken.weight} kg", fontSize = 14.sp)
                    if (chicken.isActive && chicken.barnId != null) {
                        Text("Barn ID: #${chicken.barnId}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF00897B))
                    }
                }
                if (!chicken.note.isNullOrEmpty()) {
                    Text("Note: ${chicken.note}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "healthy" -> Color(0xFFE8F5E9)
        "sick" -> Color(0xFFFFEBEE)
        "warning" -> Color(0xFFFFF3E0)
        else -> Color(0xFFF5F5F5)
    }
    val textColor = when (status.lowercase()) {
        "healthy" -> Color(0xFF2E7D32)
        "sick" -> Color(0xFFD32F2F)
        "warning" -> Color(0xFFEF6C00)
        else -> Color(0xFF616161)
    }
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
