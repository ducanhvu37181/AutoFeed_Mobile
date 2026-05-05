package com.example.autofeedmobile.ui.barn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autofeedmobile.network.FeedingRuleData
import com.example.autofeedmobile.network.FoodRuleDetailData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingRuleScreen(
    barnId: Int,
    onBack: () -> Unit
) {
    var feedingRules by remember { mutableStateOf<List<FeedingRuleData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(barnId) {
        try {
            val response = RetrofitClient.instance.getFeedingRulesByBarn(barnId)
            if (response.isSuccessful) {
                feedingRules = response.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load feeding rules: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barn #$barnId Feeding Rules", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00897B))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF00897B)
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (feedingRules.isEmpty()) {
                Text(
                    text = "No feeding rules found for this barn",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(feedingRules) { rule ->
                        FeedingRuleCard(rule)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedingRuleCard(rule: FeedingRuleData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = rule.description,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                StatusBadge(rule.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (rule.chickenName != null) {
                InfoItem(icon = Icons.Default.Pets, label = "Target Chicken", value = rule.chickenName)
            } else if (rule.flockName != null) {
                InfoItem(icon = Icons.Default.Groups, label = "Target Flock", value = rule.flockName)
            }
            
            InfoItem(icon = Icons.Default.CalendarToday, label = "Duration", value = "${formatDate(rule.startDate)} - ${formatDate(rule.endDate)}")
            InfoItem(icon = Icons.Default.Schedule, label = "Feeding Times", value = "${rule.times} times per day")

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Text(
                text = "Feeding Schedule Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            rule.details.forEach { detail ->
                FeedingDetailRow(detail)
            }
        }
    }
}

@Composable
fun FeedingDetailRow(detail: FoodRuleDetailData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (detail.status) Color(0xFF4CAF50) else Color.Gray, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${String.format("%02d:%02d", detail.feedHour, detail.feedMinute)} - ${detail.foodName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (detail.description != null) {
                Text(text = detail.description, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Text(
            text = "${detail.amount}g",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00897B)
        )
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = if (status.lowercase() == "active") Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val textColor = if (status.lowercase() == "active") Color(0xFF2E7D32) else Color(0xFF616161)
    
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
