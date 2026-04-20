package com.example.autofeedmobile.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

data class ScheduleTask(
    val title: String,
    val status: String, // "Completed", "Pending", "In Progress"
    val priority: String,
    val time: String,
    val location: String,
    val details: String,
    val note: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailContent(
    task: ScheduleTask,
    selectedDate: Calendar = Calendar.getInstance(),
    onStatusUpdate: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.LightGray, RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Title and Priority Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
            
            val priorityColor = when (task.priority.lowercase()) {
                "high" -> Color(0xFFFFEBEE)
                "medium" -> Color(0xFFFFF3E0)
                "low" -> Color(0xFFE8F5E9)
                else -> Color(0xFFF5F5F5)
            }
            val priorityTextColor = when (task.priority.lowercase()) {
                "high" -> Color(0xFFD32F2F)
                "medium" -> Color(0xFFEF6C00)
                "low" -> Color(0xFF2E7D32)
                else -> Color(0xFF616161)
            }

            Surface(
                color = priorityColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = task.priority,
                    color = priorityTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Badge
        val statusColor = when (task.status) {
            "Completed" -> Color(0xFFE8F5E9)
            "Pending" -> Color(0xFFE3F2FD)
            "In Progress" -> Color(0xFFFFF3E0)
            else -> Color(0xFFF5F5F5)
        }
        val statusTextColor = when (task.status) {
            "Completed" -> Color(0xFF2E7D32)
            "Pending" -> Color(0xFF1565C0)
            "In Progress" -> Color(0xFFEF6C00)
            else -> Color(0xFF616161)
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

        // Note Section
        if (!task.note.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Note",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.note,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Button
        if (task.status.equals("Pending", ignoreCase = true) || task.status.equals("In Progress", ignoreCase = true)) {
            val context = LocalContext.current
            val isPending = task.status.equals("Pending", ignoreCase = true)
            val buttonText = if (isPending) "Begin Schedule" else "Mark as Complete"
            val nextStatus = if (isPending) "In Progress" else "Completed"
            
            Button(
                onClick = {
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val selected = Calendar.getInstance().apply {
                        time = selectedDate.time
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (selected.before(today)) {
                        Toast.makeText(context, "Cannot update a past schedule.", Toast.LENGTH_SHORT).show()
                    } else if (isPending) {
                        val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val todayStr = apiDateFormatter.format(Date())
                        val selectedDateStr = apiDateFormatter.format(selectedDate.time)

                        if (selectedDateStr != todayStr && selected.after(today)) {
                            Toast.makeText(context, "Cannot start a future schedule.", Toast.LENGTH_SHORT).show()
                        } else {
                            // On the same day, we allow starting regardless of the time (lowered requirement)
                            onStatusUpdate(nextStatus)
                        }
                    } else {
                        onStatusUpdate(nextStatus)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A67E))
            ) {
                Text(
                    text = buttonText,
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
            priority = "High",
            time = "06:00 AM",
            location = "Barn A",
            details = "Complete the scheduled feeding task for the assigned location. Ensure all procedures are followed and report any issues.",
            note = "All tech farmer-specific functions have been properly relocated and are now only accessible to users with the Tech Farmer role."
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
            priority = "Medium",
            time = "12:00 PM",
            location = "Barn A",
            details = "Complete the scheduled feeding task for the assigned location. Ensure all procedures are followed and report any issues.",
            note = "Relocate Device Management Functions Version 31"
        )
    )
}
