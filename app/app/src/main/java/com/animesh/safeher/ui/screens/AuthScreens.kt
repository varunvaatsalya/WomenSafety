package com.animesh.safeher.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.animesh.safeher.ui.theme.*
import com.animesh.safeher.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BackgroundDark, Color(0xFF2D0A4E)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Logo / App name
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.radialGradient(listOf(PinkPrimary, PurpleAccent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(50.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text("SafeHer", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface)
            Text("Your safety companion", fontSize = 14.sp, color = PinkLight,
                modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(48.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = authFieldColors(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = authFieldColors(),
                singleLine = true
            )

            // Error message
            AnimatedVisibility(visible = uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = DangerRed, fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Login button
            Button(
                onClick = { viewModel.signIn(email.trim(), password) },
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? ", color = OnSurface.copy(alpha = 0.6f))
                Text("Register", color = PinkPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundDark, Color(0xFF2D0A4E))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Text("Create Account", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface)
            Text("Join SafeHer — Stay Protected", fontSize = 14.sp, color = PinkLight,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))

            val fields = listOf(
                Triple(name, "Full Name", Icons.Default.Person) to { v: String -> name = v },
                Triple(phone, "Phone (+91XXXXXXXXXX)", Icons.Default.Phone) to { v: String -> phone = v },
                Triple(email, "Email Address", Icons.Default.Email) to { v: String -> email = v },
            )

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Full Name") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = authFieldColors(), singleLine = true)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = phone, onValueChange = { phone = it },
                label = { Text("Phone (+91XXXXXXXXXX)") }, leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = authFieldColors(), singleLine = true)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email Address") }, leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = authFieldColors(), singleLine = true)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = authFieldColors(), singleLine = true
            )

            AnimatedVisibility(visible = uiState.error != null) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp)) {
                    Text(uiState.error ?: "", color = DangerRed, fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { viewModel.signUp(email.trim(), password, name.trim(), phone.trim()) },
                enabled = !uiState.isLoading && name.isNotBlank() && phone.isNotBlank()
                        && email.isNotBlank() && password.length >= 6,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) {
                if (uiState.isLoading) CircularProgressIndicator(
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? ", color = OnSurface.copy(alpha = 0.6f))
                Text("Sign In", color = PinkPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PinkPrimary,
    unfocusedBorderColor = OnSurface.copy(alpha = 0.3f),
    focusedLabelColor = PinkPrimary,
    cursorColor = PinkPrimary,
    focusedTextColor = OnSurface,
    unfocusedTextColor = OnSurface,
    focusedLeadingIconColor = PinkPrimary,
    unfocusedLeadingIconColor = OnSurface.copy(alpha = 0.5f),
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark
)