package com.example.autofeedmobile.ui.chicken

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import com.example.autofeedmobile.network.FlockData
import com.example.autofeedmobile.network.LargeChickenData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.util.formatDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlockDetailView(userId: Int, flockId: Int, onRefresh: () -> Unit = {}) {
    var flock by remember { mutableStateOf<FlockData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchDetail() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getFlockDetail(flockId)
                if (response.isSuccessful) {
                    flock = response.body()?.data
                }
            } catch (e: Exception) {
                // Error handling
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(flockId) {
        fetchDetail()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00897B))
        }
    } else if (flock != null) {
        Column {
            FlockDetailContent(
                flock = flock!!,
                onEditClick = { showEditSheet = true }
            )
        }

        if (showEditSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEditSheet = false },
                containerColor = Color.White
            ) {
                EditFlockView(
                    userId = userId,
                    flock = flock!!,
                    onSuccess = {
                        showEditSheet = false
                        fetchDetail()
                        onRefresh()
                    },
                    onCancel = { showEditSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeChickenDetailView(userId: Int, chickenId: Int, onRefresh: () -> Unit = {}) {
    var chicken by remember { mutableStateOf<LargeChickenData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchDetail() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getLargeChickenDetail(chickenId)
                if (response.isSuccessful) {
                    chicken = response.body()?.data
                }
            } catch (e: Exception) {
                // Error handling
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(chickenId) {
        fetchDetail()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00897B))
        }
    } else if (chicken != null) {
        Column {
            LargeChickenDetailContent(
                chicken = chicken!!,
                onEditClick = { showEditSheet = true }
            )
        }

        if (showEditSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEditSheet = false },
                containerColor = Color.White
            ) {
                EditLargeChickenView(
                    userId = userId,
                    chicken = chicken!!,
                    onSuccess = {
                        showEditSheet = false
                        fetchDetail()
                        onRefresh()
                    },
                    onCancel = { showEditSheet = false }
                )
            }
        }
    }
}

@Composable
fun FlockDetailContent(
    flock: FlockData,
    onEditClick: () -> Unit = {}
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

        Spacer(modifier = Modifier.height(12.dp))

        // Edit Button Row
        if (flock.isActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEditClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00897B))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Details")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title and Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = flock.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Flock ID: ${flock.flockId}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            StatusBadge(flock.healthStatus)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Scale,
                label = "Weight",
                value = "${flock.weight} kg"
            )
            if (flock.isActive) {
                DetailInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Numbers,
                    label = "Quantity",
                    value = "${flock.quantity}"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = if (flock.isActive) Icons.Default.CheckCircle else Icons.Default.MoveToInbox,
                label = "Status",
                value = if (flock.isActive) "Active" else "Transferred",
                iconColor = if (flock.isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            if (flock.isActive && flock.barnId != null) {
                DetailInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Home,
                    label = "Assigned Barn ID",
                    value = "#${flock.barnId}",
                    iconColor = Color(0xFF00897B)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Cake,
                label = "Age",
                value = "${flock.ageInMonths} ${if (flock.ageInMonths == 1) "month" else "months"}"
            )
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarToday,
                label = "Date of Birth",
                value = formatDate(flock.doB ?: flock.dob)
            )
        }

        if (!flock.isActive && flock.transferDate != null) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailInfoCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Event,
                label = "Transfer Date",
                value = formatDate(flock.transferDate)
            )
        }

        if (!flock.note.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = flock.note,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LargeChickenDetailContent(
    chicken: LargeChickenData,
    onEditClick: () -> Unit = {}
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

        Spacer(modifier = Modifier.height(12.dp))

        // Edit Button Row
        if (chicken.isActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEditClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00897B))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Details")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Header with Image
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = RetrofitClient.getFullUrl(chicken.imageUrl),
                contentDescription = chicken.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chicken.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "ID: ${chicken.chickenLid}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(chicken.healthStatus)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.MonitorWeight,
                label = "Weight",
                value = "${chicken.weight} kg"
            )
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = if (chicken.isActive) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                label = "Status",
                value = if (chicken.isActive) "Active" else "Exported",
                iconColor = if (chicken.isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val ageVal = chicken.ageInMonths ?: chicken.age
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Cake,
                label = "Age",
                value = "$ageVal ${if (ageVal == 1) "month" else "months"}"
            )
            DetailInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Grid3x3,
                label = "Flock Association",
                value = chicken.flockName ?: "N/A"
            )
        }

        if (chicken.isActive && chicken.barnId != null) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailInfoCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Home,
                label = "Assigned Barn ID",
                value = "#${chicken.barnId}",
                iconColor = Color(0xFF00897B)
            )
        }

        if (!chicken.isActive && chicken.exportDate != null) {
            Spacer(modifier = Modifier.height(12.dp))
            DetailInfoCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Event,
                label = "Export Date",
                value = formatDate(chicken.exportDate),
                iconColor = Color(0xFFF44336)
            )
        }

        if (!chicken.note.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = chicken.note,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
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
    iconColor: Color = Color.Gray
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
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
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
