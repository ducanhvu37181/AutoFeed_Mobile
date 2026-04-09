package com.example.autofeedmobile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.UpdateProfileDto
import com.example.autofeedmobile.network.UserResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: Int,
    userFullName: String,
    onLogout: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToRequests: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onProfileUpdated: (UserResponse) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var userData by remember { mutableStateOf<UserResponse?>(null) }
    var inventoryList by remember { mutableStateOf<List<com.example.autofeedmobile.network.InventoryData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }

    fun fetchUserData() {
        isLoading = true
        scope.launch {
            try {
                val profileResponse = RetrofitClient.instance.getUserProfile(userId)
                if (profileResponse.isSuccessful) {
                    val rawData = profileResponse.body()?.data
                    if (rawData != null) {
                        val fullUrl = RetrofitClient.getFullUrl(rawData.avatarUrl)
                        // Append timestamp to force Coil to bypass its cache if the URL hasn't changed but the content has.
                        val updatedUrl = if (fullUrl != null) "$fullUrl?t=${System.currentTimeMillis()}" else null
                        userData = rawData.copy(avatarUrl = updatedUrl)
                        userData?.let { onProfileUpdated(it) }
                    }
                }
                
                val inventoryResponse = RetrofitClient.instance.getInventory()
                if (inventoryResponse.isSuccessful) {
                    inventoryList = inventoryResponse.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        fetchUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AutoFeed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("My Profile", color = Color.White, fontSize = 14.sp)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNavigateToAlerts) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                        if (inventoryList.any { it.quantity < 3 }) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showMenu = true }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = userData?.fullName ?: userFullName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Farmer",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userData?.avatarUrl != null && userData?.avatarUrl!!.isNotEmpty()) {
                                AsyncImage(
                                    model = userData?.avatarUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Menu",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
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
                                    text = userData?.fullName ?: userFullName,
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
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = onNavigateToDashboard
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = false,
                    onClick = onNavigateToInventory
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Schedule") },
                    label = { Text("Schedule") },
                    selected = false,
                    onClick = onNavigateToSchedule
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = true,
                    onClick = {}
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00897B))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2F1))
                                    .clickable { showAvatarSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (userData?.avatarUrl != null && userData?.avatarUrl!!.isNotEmpty()) {
                                    AsyncImage(
                                        model = userData?.avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFF00897B)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00897B))
                                        .padding(4.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(userData?.fullName ?: userFullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Farmer (ID: $userId)", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showEditSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile")
                            }
                        }
                    }
                }

                // Features Section
                item {
                    Text("Features", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.ChatBubbleOutline,
                                title = "My Requests",
                                subtitle = "View and manage your requests",
                                onClick = onNavigateToRequests
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            ProfileMenuItem(
                                icon = Icons.Default.Assessment,
                                title = "My Reports",
                                subtitle = "View and submit daily reports",
                                onClick = onNavigateToReports
                            )
                        }
                    }
                }

                // Account Section
                item {
                    Text("Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Settings,
                                title = "Settings",
                                subtitle = "App preferences and security",
                                onClick = onNavigateToSettings
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = "Logout",
                                subtitle = "Sign out of your account",
                                textColor = Color.Red,
                                iconColor = Color.Red,
                                onClick = onLogout
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditSheet && userData != null) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            containerColor = Color.White
        ) {
            EditProfileContent(
                userId = userId,
                initialFullName = userData!!.fullName,
                initialEmail = userData!!.email,
                initialPhone = userData!!.phone,
                username = userData!!.username,
                roleId = userData!!.roleId,
                onSuccess = {
                    showEditSheet = false
                    fetchUserData()
                },
                onCancel = { showEditSheet = false }
            )
        }
    }

    if (showAvatarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarSheet = false },
            containerColor = Color.White
        ) {
            UpdateAvatarContent(
                userId = userId,
                currentAvatarUrl = userData?.avatarUrl ?: "",
                onSuccess = { showAvatarSheet = false },
                onCancel = { showAvatarSheet = false },
                onAvatarUpdated = { fetchUserData() }
            )
        }
    }
}

@Composable
fun EditProfileContent(
    userId: Int,
    initialFullName: String,
    initialEmail: String,
    initialPhone: String,
    username: String,
    roleId: Int,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var fullName by remember { mutableStateOf(initialFullName) }
    var email by remember { mutableStateOf(initialEmail) }
    var phone by remember { mutableStateOf(initialPhone) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.instance.updateProfile(
                                userId,
                                UpdateProfileDto(
                                    roleId = roleId,
                                    email = email,
                                    fullName = fullName,
                                    phone = phone,
                                    username = username
                                )
                            )
                            if (response.isSuccessful) {
                                onSuccess()
                            } else {
                                android.util.Log.e("ProfileScreen", "Update failed: ${response.code()} ${response.message()} ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ProfileScreen", "Error updating profile", e)
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Save Changes")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun UpdateAvatarContent(
    userId: Int,
    currentAvatarUrl: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    onAvatarUpdated: () -> Unit = {}
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Update Avatar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (currentAvatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = currentAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tap to select a new image",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 14.sp,
            color = Color.Gray
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val uri = selectedImageUri
                    if (uri != null) {
                        isSubmitting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                // Create a temporary file from Uri
                                val file = File(context.cacheDir, "temp_avatar.jpg")
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    FileOutputStream(file).use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                if (file.length() > 5 * 1024 * 1024) { // 5MB limit check
                                    errorMessage = "File is too large (max 5MB)"
                                    isSubmitting = false
                                    return@launch
                                }

                                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                                val response = RetrofitClient.instance.updateAvatar(userId, body)
                                if (response.isSuccessful) {
                                    onAvatarUpdated()
                                    onSuccess()
                                } else {
                                    errorMessage = "Upload failed: ${response.message()}"
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                errorMessage = "Error: ${e.localizedMessage}"
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                enabled = !isSubmitting && selectedImageUri != null
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Update Avatar")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color = Color.Black,
    iconColor: Color = Color(0xFF00897B),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
