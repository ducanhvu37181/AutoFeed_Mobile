package com.example.autofeedmobile.ui.chicken

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autofeedmobile.network.FlockData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.UpdateFlockDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFlockView(
    userId: Int,
    flock: FlockData,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(flock.name) }
    var healthStatus by remember { mutableStateOf(flock.healthStatus) }
    var weight by remember { mutableStateOf(flock.weight.toString()) }
    var note by remember { mutableStateOf(flock.note ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val healthStatuses = listOf("Healthy", "Sick")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
            text = "Edit Flock",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { },
            readOnly = true,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                disabledBorderColor = Color.LightGray,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Health Status
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = healthStatus,
                onValueChange = {},
                readOnly = true,
                label = { Text("Health Status") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                healthStatuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            healthStatus = status
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight
        OutlinedTextField(
            value = weight,
            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Note
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val updateDto = UpdateFlockDto(
                                flockId = flock.flockId,
                                name = name,
                                healthStatus = healthStatus,
                                weight = weight.toDoubleOrNull() ?: flock.weight,
                                note = note
                            )
                            val updateResponse = RetrofitClient.instance.updateFlock(flock.flockId, updateDto)

                            if (updateResponse.isSuccessful) {
                                // Automatically send report
                                try {
                                    val changes = mutableListOf<String>()
                                    if (name.trim() != flock.name) changes.add("Name: '${flock.name}' -> '${name.trim()}'")
                                    if (healthStatus != flock.healthStatus) changes.add("Health Status: '${flock.healthStatus}' -> '$healthStatus'")
                                    val newWeight = weight.toDoubleOrNull() ?: flock.weight
                                    if (newWeight != flock.weight) changes.add("Weight: ${flock.weight}kg -> ${newWeight}kg")
                                    if (note.trim() != (flock.note?.trim() ?: "")) {
                                        changes.add("Notes updated")
                                    }

                                    if (changes.isNotEmpty()) {
                                        val description = "User updated flock details for '${flock.name}' (ID: ${flock.flockId}). Changes: ${changes.joinToString("; ")}"
                                        val userIdPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                        val typePart = "Flock".toRequestBody("text/plain".toMediaTypeOrNull())
                                        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                                        
                                        RetrofitClient.instance.createReport(
                                            userId = userIdPart,
                                            type = typePart,
                                            description = descriptionPart,
                                            file = null
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                Toast.makeText(context, "Flock updated successfully", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save Changes")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
