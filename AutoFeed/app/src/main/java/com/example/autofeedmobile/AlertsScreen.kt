package com.example.autofeedmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.autofeedmobile.network.InventoryData
import com.example.autofeedmobile.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    userFullName: String,
    userAvatarUrl: String? = null,
    onLogout: () -> Unit = {},
    onBack: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var inventoryList by remember { mutableStateOf<List<InventoryData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getInventory()
                if (response.isSuccessful) {
                    inventoryList = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val outOfStockItems = inventoryList.filter { it.quantity == 0 }
    val lowStockItems = inventoryList.filter { it.quantity in 1..2 }
    val combinedAlerts = outOfStockItems + lowStockItems

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Alerts", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userAvatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(userAvatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onLoading = {
                                    android.util.Log.d("Coil", "Alerts: Loading avatar from $userAvatarUrl")
                                },
                                onSuccess = {
                                    android.util.Log.d("Coil", "Alerts: Avatar loaded successfully")
                                },
                                onError = { error ->
                                    android.util.Log.e("Coil", "Alerts: Avatar load failed. Error: ${error.result.throwable.message}")
                                }
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
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
                                    text = userFullName,
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center), color = Color(0xFF00897B))
            } else if (combinedAlerts.isEmpty()) {
                Text(
                    "No alerts at the moment",
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(combinedAlerts) { item ->
                        val isOutOfStock = item.quantity == 0
                        AlertItem(
                            title = if (isOutOfStock) "Out of Stock" else "Low Stock",
                            message = if (isOutOfStock) "${item.foodName} is empty" else "${item.foodName} below minimum (${item.quantity} left)",
                            color = Color(0xFFFFEBEE),
                            indicatorColor = Color.Red
                        )
                    }
                }
            }
        }
    }
}
