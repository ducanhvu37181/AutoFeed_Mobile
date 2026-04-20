package com.example.autofeedmobile.ui.barn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autofeedmobile.network.BarnData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.util.formatDate
import kotlinx.coroutines.delay

@Composable
fun BarnDetailView(barnId: Int) {
    var barn by remember { mutableStateOf<BarnData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Real-time polling for detail view
    LaunchedEffect(barnId) {
        while (true) {
            try {
                val response = RetrofitClient.instance.getBarnDetail(barnId)
                if (response.isSuccessful) {
                    barn = response.body()
                }
            } catch (e: Exception) {
                // Silent error
            } finally {
                isLoading = false
            }
            delay(3000) // Poll faster in detail view
        }
    }

    if (isLoading && barn == null) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00897B))
        }
    } else if (barn != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Barn #${barn!!.barnId} Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = barn!!.type,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Real-time Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RealTimeDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Thermostat,
                    label = "Temperature",
                    value = "${barn!!.temperature}°C",
                    color = Color(0xFFFF5722)
                )
                RealTimeDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WaterDrop,
                    label = "Humidity",
                    value = "${barn!!.humidity}%",
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RealTimeDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Restaurant,
                    label = "Food Level",
                    value = "${barn!!.foodAmount}g",
                    color = Color(0xFF4CAF50)
                )
                RealTimeDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Opacity,
                    label = "Water Level",
                    value = "${barn!!.waterAmount}%",
                    color = Color(0xFF03A9F4)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Food Consumption Stats
            Text("Food Consumption", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RealTimeDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Today,
                    label = "Today",
                    value = "${barn!!.foodToday ?: 0.0}g",
                    color = Color(0xFF4CAF50)
                )
                RealTimeDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DateRange,
                    label = "This Week",
                    value = "${barn!!.foodWeek ?: 0.0}g",
                    color = Color(0xFFFF9800)
                )
                RealTimeDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CalendarMonth,
                    label = "This Month",
                    value = "${barn!!.foodMonth ?: 0.0}g",
                    color = Color(0xFF9C27B0)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Other Information
            Text("General Information", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(label = "Area Size", value = "${barn!!.area} m²")
            InfoRow(label = "Status", value = barn!!.status.replaceFirstChar { it.uppercase() })
            InfoRow(label = "Created Date", value = formatDate(barn!!.createDate))

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RealTimeDetailCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
