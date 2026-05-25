package com.animesh.safeher.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.animesh.safeher.data.models.EmergencyContact
import com.animesh.safeher.data.models.SosAlert
import com.animesh.safeher.data.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object SosManager {

    /**
     * Main SOS trigger — called when user presses the SOS button.
     * Fires simultaneously:
     *  1. SMS to all emergency contacts (device SmsManager)
     *  2. HTTP SMS via Fast2SMS API (backup / when SmsManager not available)
     *  3. FCM push notification to "sos_alerts" topic (all nearby app users)
     *  4. Returns SosAlert object → caller saves it to Firestore
     */
    suspend fun triggerSOS(
        context: Context,
        user: UserProfile,
        contacts: List<EmergencyContact>,
        lat: Double,
        lng: Double
    ): SosAlert = withContext(Dispatchers.IO) {

        val mapsLink = "https://maps.google.com/?q=$lat,$lng"
        val message = """
            🚨 EMERGENCY ALERT from ${user.name}!
            She needs immediate help.
            📍 Live Location: $mapsLink
            📞 Her number: ${user.phone}
            Please contact her or call police 100 immediately.
        """.trimIndent()

        // 1️⃣ Send native SMS to each emergency contact
        sendNativeSms(context, contacts, message)

        // 2️⃣ Send SMS via Fast2SMS API (India-specific, free tier available)
        sendFast2Sms(contacts, message)

        // 3️⃣ Send FCM push to all app users subscribed to sos_alerts topic
        sendFcmTopicAlert(user.name, lat, lng, mapsLink)

        // 4️⃣ Return alert object (caller will persist to Firestore)
        SosAlert(
            userId = user.uid,
            userName = user.name,
            phone = user.phone,
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis(),
            resolved = false,
            mapsLink = mapsLink
        )
    }

    // ── 1. Native SMS (direct from device SIM) ───────────────────────────────
    private fun sendNativeSms(context: Context, contacts: List<EmergencyContact>, message: String) {
        try {
            @Suppress("DEPRECATION")
            val smsManager = SmsManager.getDefault()
            contacts.forEach { contact ->
                try {
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                    Log.d("SOS", "SMS sent to ${contact.name} (${contact.phone})")
                } catch (e: Exception) {
                    Log.e("SOS", "SMS failed to ${contact.phone}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("SOS", "SmsManager error: ${e.message}")
        }
    }

    // ── 2. Fast2SMS API (backup SMS, India) ──────────────────────────────────
    // Sign up free at https://www.fast2sms.com — put your API key in AppConfig
    private fun sendFast2Sms(contacts: List<EmergencyContact>, message: String) {
        if (AppConfig.FAST2SMS_API_KEY == "YOUR_FAST2SMS_API_KEY_HERE") {
            Log.w("SOS", "Fast2SMS API key not configured — skipping")
            return
        }
        try {
            val numbers = contacts.joinToString(",") { it.phone.replace("+91", "") }
            val url = URL("https://www.fast2sms.com/dev/bulkV2")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("authorization", AppConfig.FAST2SMS_API_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val body = JSONObject().apply {
                put("route", "q")
                put("message", message)
                put("language", "english")
                put("flash", 0)
                put("numbers", numbers)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            val code = conn.responseCode
            Log.d("SOS", "Fast2SMS response: $code")
        } catch (e: Exception) {
            Log.e("SOS", "Fast2SMS error: ${e.message}")
        }
    }

    // ── 3. FCM Topic Push (all installed-app users get alert) ────────────────
    // Uses legacy FCM HTTP API — works with Server Key from Firebase Console
    private fun sendFcmTopicAlert(name: String, lat: Double, lng: Double, mapsLink: String) {
        if (AppConfig.FCM_SERVER_KEY == "YOUR_FCM_SERVER_KEY_HERE") {
            Log.w("SOS", "FCM Server Key not configured — skipping topic push")
            return
        }
        try {
            val url = URL("https://fcm.googleapis.com/fcm/send")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "key=${AppConfig.FCM_SERVER_KEY}")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val notification = JSONObject().apply {
                put("title", "🚨 SOS Alert Nearby!")
                put("body", "$name needs help! Tap to see location.")
                put("sound", "default")
            }
            val data = JSONObject().apply {
                put("type", "sos")
                put("name", name)
                put("lat", lat)
                put("lng", lng)
                put("mapsLink", mapsLink)
            }
            val payload = JSONObject().apply {
                put("to", "/topics/sos_alerts")
                put("notification", notification)
                put("data", data)
                put("priority", "high")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            Log.d("SOS", "FCM topic push response: ${conn.responseCode}")
        } catch (e: Exception) {
            Log.e("SOS", "FCM push error: ${e.message}")
        }
    }

    // ── 4. Send FCM directly to contacts who have the app ────────────────────
    // Call this from ViewModel after fetching contacts' FCM tokens from Firestore
    fun sendFcmToTokens(tokens: List<String>, senderName: String, mapsLink: String) {
        if (AppConfig.FCM_SERVER_KEY == "YOUR_FCM_SERVER_KEY_HERE") return
        if (tokens.isEmpty()) return
        try {
            val url = URL("https://fcm.googleapis.com/fcm/send")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "key=${AppConfig.FCM_SERVER_KEY}")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("registration_ids", JSONArray(tokens))
                put("notification", JSONObject().apply {
                    put("title", "🚨 Emergency from $senderName")
                    put("body", "Tap to view live location")
                })
                put("data", JSONObject().apply {
                    put("type", "sos_contact")
                    put("mapsLink", mapsLink)
                })
                put("priority", "high")
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            Log.d("SOS", "FCM direct push response: ${conn.responseCode}")
        } catch (e: Exception) {
            Log.e("SOS", "FCM direct error: ${e.message}")
        }
    }
}