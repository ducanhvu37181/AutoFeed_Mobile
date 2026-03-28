package com.example.autofeedmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RequestTask(
    val id: String,
    val title: String,
    val status: String,
    val type: String,
    val createDate: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailContent(
    request: RequestTask
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

        // Title and Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = request.id,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            val (statusColor, statusTextColor) = getStatusColors(request.status)
            Surface(
                color = statusColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = request.status,
                    color = statusTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Type and Date Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Category,
                label = "Type",
                value = request.type
            )
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarToday,
                label = "Date",
                value = request.createDate
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Description Section
        Text(
            text = "Description",
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
                text = request.description,
                fontSize = 14.sp,
                color = Color(0xFF455A64),
                modifier = Modifier.padding(16.dp),
                lineHeight = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailInfoCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1A1A1A)
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
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

private fun getStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "pending" -> Pair(Color(0xFFFFF8E1), Color(0xFFFFA000))
        "approved" -> Pair(Color(0xFFE8F5E9), Color(0xFF00C853))
        "rejected" -> Pair(Color(0xFFFFEBEE), Color(0xFFD32F2F))
        else -> Pair(Color(0xFFF5F5F5), Color(0xFF757575))
    }
}

@Preview(showBackground = true)
@Composable
fun RequestDetailPreview() {
    RequestDetailContent(
        request = RequestTask(
            id = "REQ001",
            title = "Restock Premium Feed",
            status = "Pending",
            type = "Inventory",
            createDate = "2026-02-05",
            description = "Need 500kg of premium feed"
        )
    )
}
