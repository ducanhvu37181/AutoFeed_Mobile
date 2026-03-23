package com.example.autofeedmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ScheduleTask(
    val title: String,
    val status: String, // "Completed", "Pending", "In Progress"
    val time: String,
    val location: String,
    val details: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailContent(
    task: ScheduleTask,
    onMarkComplete: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Drag Handle (represented in the image as a small line)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.LightGray, RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = task.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Badge
        val statusColor = when (task.status) {
            "Completed" -> Color(0xFFE8F5E9)
            "Pending" -> Color(0xFFE3F2FD)
            else -> Color(0xFFFFF3E0)
        }
        val statusTextColor = when (task.status) {
            "Completed" -> Color(0xFF2E7D32)
            "Pending" -> Color(0xFF1565C0)
            else -> Color(0xFFEF6C00)
        }

        Surface(
            color = statusColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = task.status,
                color = statusTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Time and Location Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AccessTime,
                label = "Time",
                value = task.time
            )
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocationOn,
                label = "Location",
                value = task.location
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Task Details Section
        Text(
            text = "Task Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = task.details,
                fontSize = 14.sp,
                color = Color(0xFF455A64),
                modifier = Modifier.padding(16.dp),
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Button
        if (task.status != "Completed") {
            Button(
                onClick = onMarkComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A67E))
            ) {
                Text(
                    text = "Mark as Complete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleDetailPreview() {
    ScheduleDetailContent(
        task = ScheduleTask(
            title = "Morning Feeding - Barn A",
            status = "Completed",
            time = "06:00 AM",
            location = "Barn A",
            details = "Complete the scheduled feeding task for the assigned location. Ensure all procedures are followed and report any issues."
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ScheduleDetailPendingPreview() {
    ScheduleDetailContent(
        task = ScheduleTask(
            title = "Noon Feeding - Barn A",
            status = "Pending",
            time = "12:00 PM",
            location = "Barn A",
            details = "Complete the scheduled feeding task for the assigned location. Ensure all procedures are followed and report any issues."
        )
    )
}
