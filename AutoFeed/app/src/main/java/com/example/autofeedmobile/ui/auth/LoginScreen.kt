package com.example.autofeedmobile.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autofeedmobile.network.RetrofitClient
import com.example.autofeedmobile.network.UserResponse
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (UserResponse, String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7F4)), // Light greenish background
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Surface(
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFF00A67E),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to AutoFeed",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Text(
                    text = "Fighting Cock Farm Management System",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email Address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00A67E),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your password", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00A67E),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign In Button
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val response = RetrofitClient.instance.login(email, password)
                                    val body = response.body()
                                    if (response.isSuccessful && body?.user != null && body.token != null) {
                                        // Restrict access: Only Farmers (roleId 3) with active status (status = true) can log in
                                        if (body.user.roleId != 3) {
                                            Toast.makeText(context, "Access Denied: Only Farmers can log in to this mobile app.", Toast.LENGTH_SHORT).show()
                                        } else if (body.user.status == false) {
                                            Toast.makeText(context, "Login failed: Your account has been disabled.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            onLoginSuccess(body.user!!, body.token!!)
                                        }
                                    } else {
                                        Toast.makeText(context, "Login failed: Please check the email and password", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A67E))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onForgotPassword, enabled = !isLoading) {
                    Text(
                        text = "Forgot password?",
                        color = Color(0xFF00A67E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
