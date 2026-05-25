package com.animesh.safeher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.animesh.safeher.ui.theme.*
import com.animesh.safeher.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel, onSignOut: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var editMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    val user = uiState.user

    LaunchedEffect(user) {
        editName = user?.name ?: ""
        editPhone = user?.phone ?: ""
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundDark, Color(0xFF1A0030))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header gradient banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(listOf(PinkDark.copy(0.6f), BackgroundDark))
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(3.dp, Brush.linearGradient(listOf(PinkPrimary, PurpleAccent)),
                                CircleShape)
                            .background(
                                Brush.radialGradient(listOf(PinkDark, PurpleAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user?.name?.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(user?.name ?: "User", fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(user?.email ?: "", fontSize = 13.sp,
                        color = PinkLight, modifier = Modifier.padding(bottom = 16.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats row
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Contacts", "${uiState.contacts.size}", Icons.Default.Contacts,
                    Modifier.weight(1f))
                StatCard("SOS Sent", "0", Icons.Default.Emergency, Modifier.weight(1f))
                StatCard("Safe Routes", "3", Icons.Default.Map, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // Profile info / edit
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personal Information", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = OnSurface)
                        IconButton(onClick = {
                            if (editMode) {
                                viewModel.updateProfile(editName, editPhone)
                            }
                            editMode = !editMode
                        }) {
                            Icon(
                                if (editMode) Icons.Default.Check else Icons.Default.Edit,
                                null,
                                tint = if (editMode) SafeGreen else PinkPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (editMode) {
                        OutlinedTextField(
                            value = editName, onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkPrimary,
                                unfocusedBorderColor = OnSurface.copy(0.3f),
                                focusedLabelColor = PinkPrimary,
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                cursorColor = PinkPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            singleLine = true
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editPhone, onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkPrimary,
                                unfocusedBorderColor = OnSurface.copy(0.3f),
                                focusedLabelColor = PinkPrimary,
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                cursorColor = PinkPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            singleLine = true
                        )
                    } else {
                        ProfileInfoRow(Icons.Default.Person, "Name", user?.name ?: "-")
                        ProfileInfoRow(Icons.Default.Phone, "Phone", user?.phone ?: "-")
                        ProfileInfoRow(Icons.Default.Email, "Email", user?.email ?: "-")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Safety settings card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Safety Settings", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = OnSurface)
                    Spacer(Modifier.height(12.dp))
                    SettingToggleRow(
                        icon = Icons.Default.LocationOn,
                        title = "Background Location",
                        subtitle = "Track location for SOS accuracy",
                        checked = true,
                        onCheckedChange = {}
                    )
                    HorizontalDivider(color = OnSurface.copy(0.1f))
                    SettingToggleRow(
                        icon = Icons.Default.NotificationsActive,
                        title = "Nearby Alerts",
                        subtitle = "Get notified of SOS alerts near you",
                        checked = true,
                        onCheckedChange = {}
                    )
                    HorizontalDivider(color = OnSurface.copy(0.1f))
                    SettingToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "SOS Vibration",
                        subtitle = "Vibrate when SOS is triggered",
                        checked = true,
                        onCheckedChange = {}
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Sign out
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed.copy(0.15f),
                    contentColor = DangerRed
                ),
                border = BorderStroke(1.dp, DangerRed.copy(0.4f))
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = PinkPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface)
            Text(label, fontSize = 10.sp, color = OnSurface.copy(0.5f))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, value: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PinkPrimary.copy(0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = OnSurface.copy(0.5f))
            Text(value, fontSize = 14.sp, color = OnSurface)
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, subtitle: String,
    checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PinkPrimary.copy(0.7f), modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = OnSurface)
            Text(subtitle, fontSize = 11.sp, color = OnSurface.copy(0.5f))
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = PinkPrimary,
                checkedTrackColor = PinkPrimary.copy(0.3f))
        )
    }
}