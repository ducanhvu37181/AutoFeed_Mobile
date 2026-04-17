package com.example.autofeedmobile.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autofeedmobile.network.InventoryData
import com.example.autofeedmobile.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailContent(
    item: InventoryData
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
                    text = item.foodName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "ID: ${item.inventId}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            val effectiveStatus = when {
                item.quantity == 0 -> "Out of Stock"
                item.quantity < 3 -> "Low Stock"
                else -> "Good"
            }
            val (statusColor, statusTextColor) = when (effectiveStatus) {
                "Good" -> Color(0xFFE8F5E9) to Color(0xFF00C853)
                "Low Stock" -> Color(0xFFFFF8E1) to Color(0xFFFFA000)
                "Out of Stock" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
                else -> Color(0xFFF5F5F5) to Color(0xFF757575)
            }

            Surface(
                color = statusColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = effectiveStatus,
                    color = statusTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Add a warning message if low or out of stock
        if (item.quantity < 3) {
            Spacer(modifier = Modifier.height(16.dp))
            val isOutOfStock = item.quantity == 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOutOfStock) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isOutOfStock) Color(0xFFD32F2F) else Color(0xFFFFA000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isOutOfStock) "This item is currently out of stock!" else "Low stock warning: Please restock soon.",
                        fontSize = 14.sp,
                        color = if (isOutOfStock) Color(0xFFD32F2F) else Color(0xFFFFA000),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid for Main Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InventoryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Category,
                label = "Type",
                value = item.foodType
            )
            InventoryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarToday,
                label = "Expires",
                value = formatDate(item.expiredDate)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InventoryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Inventory,
                label = "Quantity",
                value = "${item.quantity} Bags"
            )
            InventoryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.MonitorWeight,
                label = "Weight/Bag",
                value = "${item.weightPerBag} kg"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Weight Highlight
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Scale, contentDescription = null, tint = Color(0xFF00897B))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Total Weight", fontSize = 12.sp, color = Color(0xFF00897B))
                    Text("${item.totalWeight} kg", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InventoryDetailCard(
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
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryDetailPreview() {
    InventoryDetailContent(
        item = InventoryData(
            inventId = 1,
            foodId = 1,
            foodName = "Corn Mix",
            foodType = "Grain",
            quantity = 20,
            weightPerBag = 50,
            totalWeight = 1000,
            expiredDate = "2027-01-01",
            status = "Good"
        )
    )
}
