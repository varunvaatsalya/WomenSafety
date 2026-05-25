package com.animesh.safeher.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.animesh.safeher.data.models.EmergencyContact
import com.animesh.safeher.data.models.SosAlert
import com.animesh.safeher.data.models.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()

    val currentUserId get() = auth.currentUser?.uid
    val isLoggedIn get() = auth.currentUser != null

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user!!.uid)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signUp(email: String, password: String, name: String, phone: String): Result<String> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        val token = try { messaging.token.await() } catch (e: Exception) { "" }
        val profile = UserProfile(uid = uid, name = name, email = email, phone = phone, fcmToken = token)
        db.collection("users").document(uid).set(profile).await()
        // Subscribe to SOS topic
        messaging.subscribeToTopic("sos_alerts").await()
        Result.success(uid)
    } catch (e: Exception) { Result.failure(e) }

    fun signOut() = auth.signOut()

    // ── User Profile ──────────────────────────────────────────────────────────

    suspend fun getUserProfile(uid: String = currentUserId ?: ""): UserProfile? = try {
        db.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
    } catch (e: Exception) { null }

    suspend fun updateProfile(name: String, phone: String): Result<Unit> = try {
        val uid = currentUserId ?: throw Exception("Not logged in")
        db.collection("users").document(uid).update(mapOf("name" to name, "phone" to phone)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateFcmToken(token: String) {
        val uid = currentUserId ?: return
        try { db.collection("users").document(uid).update("fcmToken", token).await() }
        catch (e: Exception) { Log.e("Firebase", "Token update failed", e) }
    }

    // Update user's live location in Firestore
    suspend fun updateLocation(lat: Double, lng: Double) {
        val uid = currentUserId ?: return
        try {
            db.collection("users").document(uid).update(
                mapOf("latitude" to lat, "longitude" to lng, "lastSeen" to System.currentTimeMillis())
            ).await()
        } catch (e: Exception) { Log.e("Firebase", "Location update failed", e) }
    }

    // ── Emergency Contacts ────────────────────────────────────────────────────

    fun getEmergencyContactsFlow(uid: String = currentUserId ?: ""): Flow<List<EmergencyContact>> =
        callbackFlow {
            val listener = db.collection("users").document(uid)
                .collection("contacts")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val contacts = snapshot?.documents?.mapNotNull {
                        it.toObject(EmergencyContact::class.java)
                    } ?: emptyList()
                    trySend(contacts)
                }
            awaitClose { listener.remove() }
        }

    suspend fun addEmergencyContact(contact: EmergencyContact): Result<Unit> = try {
        val uid = currentUserId ?: throw Exception("Not logged in")
        val id = UUID.randomUUID().toString()
        db.collection("users").document(uid).collection("contacts")
            .document(id).set(contact.copy(id = id)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteEmergencyContact(contactId: String): Result<Unit> = try {
        val uid = currentUserId ?: throw Exception("Not logged in")
        db.collection("users").document(uid).collection("contacts")
            .document(contactId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ── SOS Alerts ────────────────────────────────────────────────────────────

    suspend fun triggerSosAlert(alert: SosAlert): Result<String> = try {
        val ref = db.collection("sos_alerts").document()
        ref.set(alert.copy(id = ref.id)).await()
        Result.success(ref.id)
    } catch (e: Exception) { Result.failure(e) }

    fun getActiveSosAlertsFlow(): Flow<List<SosAlert>> = callbackFlow {
        val listener = db.collection("sos_alerts")
            .whereEqualTo("resolved", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val alerts = snapshot?.documents?.mapNotNull {
                    it.toObject(SosAlert::class.java)
                } ?: emptyList()
                trySend(alerts)
            }
        awaitClose { listener.remove() }
    }
}