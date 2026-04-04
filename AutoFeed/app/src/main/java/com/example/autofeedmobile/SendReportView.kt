package com.example.autofeedmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autofeedmobile.network.CreateReportDto
import com.example.autofeedmobile.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendReportContent(
    userId: Int,
    onSuccess: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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

        Text(
            text = "Create New Report",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Report Type
        Text(
            text = "Report Type",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF455A64),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Daily Activity, Incident", color = Color.Gray) },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF00897B)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = "Description",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF455A64),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("Enter the details of your report...", color = Color.Gray) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF00897B)
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    if (type.isNotBlank() && description.isNotBlank()) {
                        isSubmitting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val response = RetrofitClient.instance.createReport(
                                    CreateReportDto(userId, type, description)
                                )
                                if (response.isSuccessful) {
                                    onSuccess()
                                } else {
                                    errorMessage = "Failed to submit report"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network error: ${e.localizedMessage}"
                            } finally {
                                isSubmitting = false
                            }
                        }
                    } else {
                        errorMessage = "Please fill in all fields"
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A67E)),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
