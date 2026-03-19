package com.example.autofeedmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "In Progress", "Completed")

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
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
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
                    selected = true,
                    onClick = {}
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
                        Text("Friday, February 6, 2026", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress Today", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("2/6", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
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

            // Filter Chips - UPDATED LABELS
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    ScheduleItem(
                        title = "Morning Feeding - Barn A",
                        time = "06:00 AM",
                        location = "Barn A",
                        statusColor = Color(0xFF00C853),
                        isCompleted = true
                    )
                }
                item {
                    ScheduleItem(
                        title = "Health Check - All Cages",
                        time = "09:00 AM",
                        location = "All Barns",
                        statusColor = Color(0xFF00C853),
                        isCompleted = true
                    )
                }
                item {
                    ScheduleItem(
                        title = "Noon Feeding - Barn A",
                        time = "12:00 PM",
                        location = "Barn A",
                        statusColor = Color(0xFF2196F3),
                        isCompleted = false,
                        showAction = true
                    )
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
    showAction: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        onClick = { },
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
                onClick = { },
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
    ScheduleScreen()
}
