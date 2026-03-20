package com.example.autofeedmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                        // Red dot for notification
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                        )
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
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Schedule") },
                    label = { Text("Schedule") },
                    selected = false,
                    onClick = onNavigateToSchedule
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Requests") },
                    label = { Text("Requests") },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Pets,
                            iconContainerColor = Color(0xFF1DB954).copy(alpha = 0.1f),
                            iconColor = Color(0xFF1DB954),
                            value = "32",
                            label = "My Chickens"
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Inventory2,
                            iconContainerColor = Color(0xFF2196F3).copy(alpha = 0.1f),
                            iconColor = Color(0xFF2196F3),
                            value = "8",
                            label = "Inventory Items"
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ElectricBolt,
                            iconContainerColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
                            iconColor = Color(0xFF9C27B0),
                            value = "5",
                            label = "Today Schedules"
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Warning,
                            iconContainerColor = Color(0xFFFF5722).copy(alpha = 0.1f),
                            iconColor = Color(0xFFFF5722),
                            value = "2",
                            label = "Low Stock"
                        )
                    }
                }
            }

            // Alerts Section
            item {
                Column {
                    Text("Alerts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    AlertItem(
                        title = "Low Stock",
                        message = "Premium Chicken Feed below minimum",
                        color = Color(0xFFFFEBEE),
                        indicatorColor = Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AlertItem(
                        title = "Temperature",
                        message = "Cage B-02 temperature above normal",
                        color = Color(0xFFFFF8E1),
                        indicatorColor = Color(0xFFFFA000) // Orange
                    )
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
                        Column {
                            DashboardTaskItem(
                                title = "Morning Feeding - Barn A",
                                time = "06:00 AM",
                                isCompleted = true
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            DashboardTaskItem(
                                title = "Health Check - Cage A-03",
                                time = "09:00 AM",
                                isCompleted = false
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            DashboardTaskItem(
                                title = "Noon Feeding - Barn A",
                                time = "12:00 PM",
                                isCompleted = false
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            DashboardTaskItem(
                                title = "Clean Water Dispensers",
                                time = "02:00 PM",
                                isCompleted = false
                            )
                        }
                    }
                }
            }

            // Today Schedule Completed Section
            item {
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
                                    Text("2/6", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(16.dp))
                                    Text("33.3%", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { 2f / 6f },
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
fun AlertItem(
    title: String,
    message: String,
    color: Color,
    indicatorColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Text(message, fontSize = 12.sp, color = Color(0xFF455A64))
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
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(time, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
