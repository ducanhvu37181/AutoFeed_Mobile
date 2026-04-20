package com.example.autofeedmobile.ui.report

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
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.ReportData
import com.example.autofeedmobile.util.formatDate

import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailContent(
    reportId: Int
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var report by remember { mutableStateOf<ReportData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reportId) {
        try {
            val response = RetrofitClient.instance.getReportDetail(reportId)
            if (response.isSuccessful) {
                report = response.body()?.data
            } else {
                errorMessage = "Failed to load report details: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00A67E))
        }
    } else if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = errorMessage!!, color = Color.Red)
        }
    } else if (report != null) {
        val currentReport = report!!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(scrollState)
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
                        text = "Report Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "Ref: #${currentReport.reportId}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                val (statusColor, statusTextColor) = getReportStatusColors(currentReport.status)

                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = getReportDisplayStatus(currentReport.status),
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
                ReportDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Category,
                    label = "Type",
                    value = currentReport.type
                )
                ReportDetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CalendarToday,
                    label = "Date",
                    value = formatDate(currentReport.createDate)
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
                    text = currentReport.description,
                    fontSize = 14.sp,
                    color = Color(0xFF455A64),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 20.sp
                )
            }

            if (!currentReport.fileUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attachment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    val fullUrl = RetrofitClient.getFullUrl(currentReport.fileUrl) ?: ""
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                            context.startActivity(intent)
                        }
                    ) {
                        Text("View file", color = Color(0xFF00897B))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        model = RetrofitClient.getFullUrl(currentReport.fileUrl),
                        contentDescription = "Attachment",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReportDetailCard(
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

private fun getReportStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "pending" -> Pair(Color(0xFFFFF8E1), Color(0xFFFFA000))
        "completed", "approved", "reviewed" -> Pair(Color(0xFFE8F5E9), Color(0xFF00C853))
        "rejected" -> Pair(Color(0xFFFFEBEE), Color(0xFFD32F2F))
        else -> Pair(Color(0xFFF5F5F5), Color(0xFF757575))
    }
}

private fun getReportDisplayStatus(status: String): String {
    return when {
        status.equals("completed", ignoreCase = true) || 
        status.equals("approved", ignoreCase = true) ||
        status.equals("reviewed", ignoreCase = true) -> "Reviewed"
        else -> status.replaceFirstChar { it.uppercase() }
    }
}
