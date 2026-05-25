package com.animesh.safeher.ui.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.google.accompanist.permissions.*
import com.animesh.safeher.services.LocationSosService
import com.animesh.safeher.ui.theme.*
import com.animesh.safeher.viewmodel.MainViewModel
import com.animesh.safeher.viewmodel.SosTriggerState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Permission handling
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
    )

    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) {
            viewModel.startLocationUpdates(context)
            LocationSosService.start(context)
        } else {
            permissions.launchMultiplePermissionRequest()
        }
    }

    // SOS state feedback
    val sosState = uiState.sosTriggerState
    LaunchedEffect(sosState) {
        if (sosState == SosTriggerState.SUCCESS || sosState == SosTriggerState.ERROR) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetSosState()
        }
    }

    // Pulse animation for SOS button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f, label = "scale",
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundDark, Color(0xFF1A0030))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Hello, ${uiState.user?.name?.split(" ")?.firstOrNull() ?: "there"} 👋",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnSurface
                    )
                    Text("Stay safe, stay empowered", fontSize = 13.sp, color = PinkLight)
                }
                Icon(Icons.Default.Shield, contentDescription = null,
                    tint = PinkPrimary, modifier = Modifier.size(32.dp))
            }

            Spacer(Modifier.height(28.dp))

            // Location card
            LocationCard(uiState.currentLocation)

            Spacer(Modifier.height(28.dp))

            // Big SOS Button
            Text("Emergency SOS", fontSize = 14.sp, color = OnSurface.copy(0.7f),
                modifier = Modifier.padding(bottom = 16.dp))

            Box(contentAlignment = Alignment.Center) {
                // Outer glow ring
                if (sosState == SosTriggerState.IDLE || sosState == SosTriggerState.SENDING) {
                    Box(
                        modifier = Modifier
                            .size(200.dp * pulseScale)
                            .clip(CircleShape)
                            .background(DangerRed.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(170.dp * pulseScale)
                            .clip(CircleShape)
                            .background(DangerRed.copy(alpha = 0.2f))
                    )
                }

                // SOS Button
                Button(
                    onClick = {
                        if (permissions.allPermissionsGranted) viewModel.triggerSOS(context)
                        else permissions.launchMultiplePermissionRequest()
                    },
                    enabled = sosState != SosTriggerState.SENDING,
                    modifier = Modifier.size(150.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (sosState) {
                            SosTriggerState.SUCCESS -> SafeGreen
                            SosTriggerState.ERROR   -> WarningAmber
                            else                   -> DangerRed
                        }
                    ),
                    elevation = ButtonDefaults.buttonElevation(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (sosState) {
                            SosTriggerState.SENDING -> CircularProgressIndicator(
                                color = Color.White, modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                            SosTriggerState.SUCCESS -> Icon(Icons.Default.CheckCircle, null,
                                modifier = Modifier.size(48.dp), tint = Color.White)
                            SosTriggerState.ERROR   -> Icon(Icons.Default.Warning, null,
                                modifier = Modifier.size(48.dp), tint = Color.White)
                            else -> Icon(Icons.Default.Emergency, null,
                                modifier = Modifier.size(48.dp), tint = Color.White)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when (sosState) {
                                SosTriggerState.SENDING -> "SENDING..."
                                SosTriggerState.SUCCESS -> "SENT!"
                                SosTriggerState.ERROR   -> "RETRY"
                                else -> "SOS"
                            },
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Press & hold to send emergency alert\nto contacts + authorities + nearby users",
                fontSize = 12.sp, color = OnSurface.copy(0.5f), textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(28.dp))

            // What SOS does — info cards
            Text("When SOS is triggered:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = OnSurface.copy(0.8f), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            SosActionChip(Icons.Default.Sms, "SMS to all emergency contacts with live location")
            SosActionChip(Icons.Default.NotificationsActive, "Push alert to all nearby SafeHer users")
            SosActionChip(Icons.Default.LocalPolice, "Alert saved to authorities dashboard")
            SosActionChip(Icons.Default.MyLocation, "Your live location shared continuously")

            Spacer(Modifier.height(28.dp))

            // Active SOS alerts nearby
            if (uiState.activeSosAlerts.isNotEmpty()) {
                NearbyAlertsSection(uiState.activeSosAlerts.take(3))
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(80.dp)) // bottom nav space
        }
    }
}

@Composable
private fun LocationCard(location: android.location.Location?) {
    val context = LocalContext.current

    // Reverse geocode: lat/lng → human-readable address
    var addressText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(location?.latitude, location?.longitude) {
        if (location == null) { addressText = null; return@LaunchedEffect }
        addressText = null  // reset while fetching
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val addr = results?.firstOrNull()
                addressText = when {
                    addr == null -> null
                    !addr.subLocality.isNullOrBlank() && !addr.locality.isNullOrBlank() ->
                        "${addr.subLocality}, ${addr.locality}"
                    !addr.locality.isNullOrBlank() && !addr.adminArea.isNullOrBlank() ->
                        "${addr.locality}, ${addr.adminArea}"
                    !addr.adminArea.isNullOrBlank() ->
                        addr.adminArea
                    else -> addr.getAddressLine(0)?.take(50)
                }
            } catch (_: Exception) {
                addressText = null  // fallback to coords below
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PinkPrimary.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null,
                    tint = PinkPrimary, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Live Location", fontSize = 12.sp, color = OnSurface.copy(0.6f))
                if (location != null) {
                    // Show place name if available, else show coords
                    Text(
                        addressText ?: "%.4f°N, %.4f°E".format(location.latitude, location.longitude),
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface,
                        maxLines = 1
                    )
                    // Show coords as subtitle when address is available
                    if (addressText != null) {
                        Text(
                            "%.4f°N, %.4f°E".format(location.latitude, location.longitude),
                            fontSize = 11.sp, color = OnSurface.copy(0.45f)
                        )
                    }
                    Text(
                        "Updated ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}",
                        fontSize = 11.sp, color = SafeGreen
                    )
                } else {
                    Text("Acquiring location...", fontSize = 14.sp, color = WarningAmber)
                }
            }
            if (location != null) {
                Icon(Icons.Default.CheckCircle, contentDescription = null,
                    tint = SafeGreen, modifier = Modifier.size(20.dp))
            } else {
                CircularProgressIndicator(color = PinkPrimary,
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun SosActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 13.sp, color = OnSurface.copy(0.85f))
    }
}

@Composable
private fun NearbyAlertsSection(alerts: List<com.animesh.safeher.data.models.SosAlert>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DangerRed.copy(0.4f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null,
                    tint = DangerRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nearby Active Alerts", fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, color = DangerRed)
            }
            Spacer(Modifier.height(10.dp))
            alerts.forEach { alert ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FiberManualRecord, null, tint = DangerRed,
                        modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${alert.userName} — ${alert.phone}", fontSize = 13.sp, color = OnSurface)
                }
            }
        }
    }
}