package com.animesh.safeher.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── SafeHer Brand Colors ──────────────────────────────────────────────────────
val PinkPrimary    = Color(0xFFE91E8C)
val PinkDark       = Color(0xFFAD1457)
val PinkLight      = Color(0xFFF48FB1)
val PurpleAccent   = Color(0xFF7B1FA2)
val DangerRed      = Color(0xFFD32F2F)
val SafeGreen      = Color(0xFF2E7D32)
val WarningAmber   = Color(0xFFF57F17)
val BackgroundDark = Color(0xFF1A0A1E)
val SurfaceDark    = Color(0xFF2D1B38)
val OnSurface      = Color(0xFFF3E5F5)
val CardBg         = Color(0xFF3D2449)

private val DarkColorScheme = darkColorScheme(
    primary         = PinkPrimary,
    onPrimary       = Color.White,
    primaryContainer = PinkDark,
    secondary       = PurpleAccent,
    onSecondary     = Color.White,
    background      = BackgroundDark,
    surface         = SurfaceDark,
    onSurface       = OnSurface,
    error           = DangerRed,
    onError         = Color.White,
)

@Composable
fun SafeHerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}