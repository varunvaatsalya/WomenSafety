package com.animesh.safeher.data.models

import com.google.android.gms.maps.model.LatLng

// ── User profile stored in Firestore ─────────────────────────────────────────
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val fcmToken: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastSeen: Long = 0L
)

// ── Emergency contact ─────────────────────────────────────────────────────────
data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",       // E.g. "+919876543210"
    val relation: String = ""     // E.g. "Mother", "Friend"
)

// ── SOS Alert written to Firestore when SOS is triggered ─────────────────────
data class SosAlert(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    val resolved: Boolean = false,
    val mapsLink: String = ""
)

// ── Hotspot area (hardcoded near IET Lucknow) ─────────────────────────────────
data class HotspotArea(
    val id: String,
    val name: String,
    val description: String,
    val location: LatLng,
    val incidentCount: Int,
    val riskLevel: RiskLevel
)

enum class RiskLevel { HIGH, MEDIUM, LOW }

// ── Hardcoded hotspots near IET Lucknow ──────────────────────────────────────
object HotspotData {
    val hotspots = listOf(
        HotspotArea(
            id = "hs1",
            name = "Sitapur Road Crossing",
            description = "Isolated stretch after 8 PM. Poor lighting near bus stop.",
            location = LatLng(26.9124, 80.9674),
            incidentCount = 14,
            riskLevel = RiskLevel.HIGH
        ),
        HotspotArea(
            id = "hs2",
            name = "Kursi Road Railway Underpass",
            description = "Underpass with limited visibility. Avoid late evenings.",
            location = LatLng(26.9005, 80.9810),
            incidentCount = 9,
            riskLevel = RiskLevel.HIGH
        ),
        HotspotArea(
            id = "hs3",
            name = "IET Back Gate Area",
            description = "Secluded lane near back gate. Incidents reported at night.",
            location = LatLng(26.9065, 80.9733),
            incidentCount = 6,
            riskLevel = RiskLevel.MEDIUM
        )
    )
}