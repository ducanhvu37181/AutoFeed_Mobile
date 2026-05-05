package com.example.autofeedmobile.ui.barn

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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.BarnData
import com.example.autofeedmobile.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarnManagementScreen(
    userId: Int,
    userFullName: String,
    userAvatarUrl: String? = null,
    hasNewNotifications: Boolean = false,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChickenManagement: () -> Unit = {},
    onNavigateToBarnImage: (Int) -> Unit = {},
    onNavigateToFeedingRule: (Int) -> Unit = {}
) {
    var barns by remember { mutableStateOf<List<BarnData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedBarnId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    // Real-time polling every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            try {
                android.util.Log.d("BarnManagementScreen", "Fetching barns...")
                val response = RetrofitClient.instance.getBarns()
                if (response.isSuccessful) {
                    val barnList = response.body() ?: emptyList()
                    android.util.Log.d("BarnManagementScreen", "Fetched ${barnList.size} barns")
                    barns = barnList
                } else {
                    android.util.Log.e("BarnManagementScreen", "Error fetching barns: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("BarnManagementScreen", "Exception in polling: ${e.message}", e)
            } finally {
                isLoading = false
            }
            delay(5000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text("AutoFeed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Barn Management", color = Color.White, fontSize = 14.sp)
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
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
        if (isLoading && barns.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00897B))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(barns) { barn ->
                    BarnItem(barn = barn, onClick = { selectedBarnId = barn.barnId })
                }
            }
        }

        if (selectedBarnId != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedBarnId = null },
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                BarnDetailView(
                    barnId = selectedBarnId!!,
                    onViewImages = {
                        val id = selectedBarnId!!
                        selectedBarnId = null
                        onNavigateToBarnImage(id)
                    },
                    onViewFeedingRules = {
                        val id = selectedBarnId!!
                        selectedBarnId = null
                        onNavigateToFeedingRule(id)
                    }
                )
            }
        }
    }
}

@Composable
fun BarnItem(barn: BarnData, onClick: () -> Unit) {
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
                Text("Barn #${barn.barnId}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                StatusBadge(barn.status)
            }
            Text(barn.type, fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RealTimeInfo(icon = Icons.Default.Thermostat, label = "Temp", value = "${barn.temperature}°C", color = Color(0xFFFF5722))
                RealTimeInfo(icon = Icons.Default.WaterDrop, label = "Humid", value = "${barn.humidity}%", color = Color(0xFF2196F3))
                RealTimeInfo(icon = Icons.Default.Restaurant, label = "Food", value = "${barn.foodAmount}g", color = Color(0xFF4CAF50))
                RealTimeInfo(icon = Icons.Default.Opacity, label = "Water", value = "${barn.waterAmount}%", color = Color(0xFF03A9F4))
            }
        }
    }
}

@Composable
fun RealTimeInfo(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = if (status.lowercase() == "used") Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val textColor = if (status.lowercase() == "used") Color(0xFF2E7D32) else Color(0xFF616161)
    
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
