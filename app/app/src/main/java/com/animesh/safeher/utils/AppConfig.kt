package com.animesh.safeher.utils

import com.animesh.safeher.BuildConfig

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║           🔑  SAFEHER — ALL CREDENTIALS HERE            ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  Secrets are stored in local.properties (git-ignored).  ║
 * ║  Copy local.properties.example → local.properties and   ║
 * ║  fill in your own keys before building.                  ║
 * ║                                                          ║
 * ║  1. Firebase:  Download google-services.json from       ║
 * ║     Firebase Console → Project Settings → Android App   ║
 * ║     Place it at:  app/google-services.json              ║
 * ║                                                          ║
 * ║  2. Realtime DB URL → local.properties: FIREBASE_DB_URL ║
 * ║                                                          ║
 * ║  3. Maps API Key   → local.properties: MAPS_API_KEY     ║
 * ║                                                          ║
 * ║  4. Fast2SMS Key   → local.properties: FAST2SMS_API_KEY ║
 * ╚══════════════════════════════════════════════════════════╝
 */
object AppConfig {

    // ── All secrets come from BuildConfig (injected from local.properties) ──
    val MAPS_API_KEY: String     get() = BuildConfig.MAPS_API_KEY
    val FIREBASE_DB_URL: String  get() = BuildConfig.FIREBASE_DB_URL
    val FAST2SMS_API_KEY: String get() = BuildConfig.FAST2SMS_API_KEY
    val FCM_SERVER_KEY: String   get() = BuildConfig.FCM_SERVER_KEY

    // ── App constants ────────────────────────────────────────
    const val SOS_TOPIC = "sos_alerts"      // FCM topic all users subscribe to
    const val NEARBY_RADIUS_KM = 5.0        // radius for nearby SOS alerts
}