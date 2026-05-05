package com.example.autofeedmobile.ui.chicken

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.LargeChickenData
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.UpdateLargeChickenDto
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLargeChickenView(
    userId: Int,
    chicken: LargeChickenData,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(chicken.name) }
    var weight by remember { mutableStateOf(chicken.weight.toString()) }
    var healthStatus by remember { mutableStateOf(chicken.healthStatus) }
    var note by remember { mutableStateOf(chicken.note ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedImageUri = uri }
    )

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
            text = "Edit Large Chicken",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { imagePickerLauncher.launch("image/*") }
        ) {
            AsyncImage(
                model = selectedImageUri ?: RetrofitClient.getFullUrl(chicken.imageUrl),
                contentDescription = "Chicken Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00897B))
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Change Image", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weight
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(8.dp)
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
                            // 1. Update Details
                            val updateDto = UpdateLargeChickenDto(
                                flockId = chicken.flockId,
                                name = name,
                                weight = weight.toDoubleOrNull() ?: chicken.weight,
                                age = chicken.age,
                                healthStatus = healthStatus,
                                note = note
                            )
                            val updateResponse = RetrofitClient.instance.updateLargeChicken(chicken.chickenLid, updateDto)
                            
                            // 2. Update Avatar if selected
                            var avatarSuccess = true
                            selectedImageUri?.let { uri ->
                                val file = File(context.cacheDir, "temp_chicken_avatar.jpg")
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    FileOutputStream(file).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                                val avatarResponse = RetrofitClient.instance.updateLargeChickenAvatar(chicken.chickenLid, body)
                                avatarSuccess = avatarResponse.isSuccessful
                            }

                            if (updateResponse.isSuccessful && avatarSuccess) {
                                // Automatically send report
                                try {
                                    val changes = mutableListOf<String>()
                                    if (name.trim() != chicken.name) changes.add("Name: '${chicken.name}' -> '${name.trim()}'")
                                    if (healthStatus != chicken.healthStatus) changes.add("Health Status: '${chicken.healthStatus}' -> '$healthStatus'")
                                    val newWeight = weight.toDoubleOrNull() ?: chicken.weight
                                    if (newWeight != chicken.weight) changes.add("Weight: ${chicken.weight}kg -> ${newWeight}kg")
                                    if (note.trim() != (chicken.note?.trim() ?: "")) changes.add("Notes updated")
                                    if (selectedImageUri != null) changes.add("Avatar updated")

                                    if (changes.isNotEmpty()) {
                                        val description = "User updated chicken details for '${chicken.name}' (ID: ${chicken.chickenLid}). Changes: ${changes.joinToString("; ")}"
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

                                Toast.makeText(context, "Chicken updated successfully", Toast.LENGTH_SHORT).show()
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
