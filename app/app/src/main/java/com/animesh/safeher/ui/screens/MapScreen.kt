package com.animesh.safeher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.animesh.safeher.data.models.*
import com.animesh.safeher.ui.theme.*
import com.animesh.safeher.viewmodel.MainViewModel

@Composable
fun MapScreen(viewModel: MainViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    var selectedHotspot by remember { mutableStateOf<HotspotArea?>(null) }

    // IET Lucknow center
    val ietLucknow = LatLng(26.9065, 80.9733)

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ietLucknow, 14f)
    }

    // Move camera to current location
    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let {
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude),
                    14f
                )
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(
                isMyLocationEnabled = uiState.currentLocation != null,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true,
                compassEnabled = true
            )
        ) {

            // Hotspot markers
            HotspotData.hotspots.forEach { hotspot ->

                val markerColor = when (hotspot.riskLevel) {
                    RiskLevel.HIGH -> BitmapDescriptorFactory.HUE_RED
                    RiskLevel.MEDIUM -> BitmapDescriptorFactory.HUE_ORANGE
                    RiskLevel.LOW -> BitmapDescriptorFactory.HUE_YELLOW
                }

                val fillColor = when (hotspot.riskLevel) {
                    RiskLevel.HIGH -> Color(0x28FF0000)
                    RiskLevel.MEDIUM -> Color(0x28FFA500)
                    RiskLevel.LOW -> Color(0x28FFFF00)
                }

                val strokeColor = when (hotspot.riskLevel) {
                    RiskLevel.HIGH -> Color(0xA0FF0000)
                    RiskLevel.MEDIUM -> Color(0xA0FFA500)
                    RiskLevel.LOW -> Color(0xA0C8C800)
                }

                // Risk Area Circle
                Circle(
                    center = hotspot.location,
                    radius = 200.0,
                    fillColor = fillColor,
                    strokeColor = strokeColor,
                    strokeWidth = 2f
                )

                // Marker
                Marker(
                    state = MarkerState(position = hotspot.location),
                    title = hotspot.name,
                    snippet = "${hotspot.incidentCount} incidents — ${hotspot.riskLevel.name} RISK",
                    icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                    onClick = {
                        selectedHotspot = hotspot
                        false
                    }
                )
            }

            // SOS Alerts
            uiState.activeSosAlerts.forEach { alert ->

                Marker(
                    state = MarkerState(
                        position = LatLng(
                            alert.latitude,
                            alert.longitude
                        )
                    ),
                    title = "🚨 SOS: ${alert.userName}",
                    snippet = "Tap for location",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_MAGENTA
                    )
                )
            }
        }

        // Legend Card
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundDark.copy(alpha = 0.92f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                Text(
                    text = "Hotspot Legend",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                RiskLegendRow(
                    color = DangerRed,
                    label = "High Risk"
                )

                RiskLegendRow(
                    color = WarningAmber,
                    label = "Medium Risk"
                )

                RiskLegendRow(
                    color = Color.Yellow,
                    label = "Low Risk"
                )

                RiskLegendRow(
                    color = PinkPrimary,
                    label = "Active SOS"
                )
            }
        }

        // Bottom Section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {

            // Selected Hotspot Card
            AnimatedVisibility(
                visible = selectedHotspot != null
            ) {

                selectedHotspot?.let { hotspot ->

                    HotspotDetailCard(
                        hotspot = hotspot,
                        onDismiss = {
                            selectedHotspot = null
                        }
                    )
                }
            }

            // Hotspot List Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Hotspot Areas near IET Lucknow",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HotspotData.hotspots.forEach { hotspot ->

                        HotspotListItem(
                            hotspot = hotspot,
                            onClick = {
                                selectedHotspot = hotspot
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskLegendRow(
    color: Color,
    label: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = OnSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun HotspotDetailCard(
    hotspot: HotspotArea,
    onDismiss: () -> Unit
) {

    val borderColor = when (hotspot.riskLevel) {
        RiskLevel.HIGH -> DangerRed
        RiskLevel.MEDIUM -> WarningAmber
        RiskLevel.LOW -> Color.Yellow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor.copy(alpha = 0.5f)
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                val riskColor = when (hotspot.riskLevel) {
                    RiskLevel.HIGH -> DangerRed
                    RiskLevel.MEDIUM -> WarningAmber
                    RiskLevel.LOW -> Color.Yellow
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = riskColor.copy(alpha = 0.2f)
                ) {

                    Text(
                        text = "${hotspot.riskLevel.name} RISK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = hotspot.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                Text(
                    text = hotspot.description,
                    fontSize = 12.sp,
                    color = OnSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "${hotspot.incidentCount} incidents reported",
                    fontSize = 11.sp,
                    color = DangerRed,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            IconButton(
                onClick = onDismiss
            ) {

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = OnSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun HotspotListItem(
    hotspot: HotspotArea,
    onClick: () -> Unit
) {

    val riskColor = when (hotspot.riskLevel) {
        RiskLevel.HIGH -> DangerRed
        RiskLevel.MEDIUM -> WarningAmber
        RiskLevel.LOW -> Color.Yellow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(riskColor)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = hotspot.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurface
            )

            Text(
                text = "${hotspot.incidentCount} incidents",
                fontSize = 11.sp,
                color = OnSurface.copy(alpha = 0.5f)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}