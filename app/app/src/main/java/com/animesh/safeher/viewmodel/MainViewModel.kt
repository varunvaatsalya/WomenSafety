package com.animesh.safeher.viewmodel

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.animesh.safeher.data.models.EmergencyContact
import com.animesh.safeher.data.models.SosAlert
import com.animesh.safeher.data.models.UserProfile
import com.animesh.safeher.data.repository.FirebaseRepository
import com.animesh.safeher.utils.SosManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: UserProfile? = null,
    val currentLocation: Location? = null,
    val contacts: List<EmergencyContact> = emptyList(),
    val activeSosAlerts: List<SosAlert> = emptyList(),
    val sosTriggerState: SosTriggerState = SosTriggerState.IDLE,
    val error: String? = null
)

enum class SosTriggerState { IDLE, SENDING, SUCCESS, ERROR }

class MainViewModel : ViewModel() {

    private val repo = FirebaseRepository()
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    init {
        if (repo.isLoggedIn) {
            _uiState.update { it.copy(isLoggedIn = true) }
            loadUserData()
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        repo.signIn(email, password).fold(
            onSuccess = {
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
                loadUserData()
            },
            onFailure = { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        )
    }

    fun signUp(email: String, password: String, name: String, phone: String) =
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.signUp(email, password, name, phone).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
                    loadUserData()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }

    fun signOut() {
        stopLocationUpdates()
        repo.signOut()
        _uiState.update { MainUiState(isLoggedIn = false) }
    }

    // ── Data Loading ──────────────────────────────────────────────────────────

    private fun loadUserData() {
        viewModelScope.launch {
            val profile = repo.getUserProfile()
            _uiState.update { it.copy(user = profile) }
        }
        viewModelScope.launch {
            repo.getEmergencyContactsFlow().collect { contacts ->
                _uiState.update { it.copy(contacts = contacts) }
            }
        }
        viewModelScope.launch {
            repo.getActiveSosAlertsFlow().collect { alerts ->
                _uiState.update { it.copy(activeSosAlerts = alerts) }
            }
        }
    }

    // ── Location ──────────────────────────────────────────────────────────────

    fun startLocationUpdates(context: Context) {
        fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(3_000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                _uiState.update { it.copy(currentLocation = loc) }
                viewModelScope.launch { repo.updateLocation(loc.latitude, loc.longitude) }
            }
        }
        try {
            fusedClient?.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
        } catch (e: SecurityException) { Log.e("VM", "Location permission missing") }
    }

    fun stopLocationUpdates() {
        locationCallback?.let { fusedClient?.removeLocationUpdates(it) }
    }

    // ── Contacts ──────────────────────────────────────────────────────────────

    fun addContact(name: String, phone: String, relation: String) = viewModelScope.launch {
        repo.addEmergencyContact(EmergencyContact(name = name, phone = phone, relation = relation))
    }

    fun deleteContact(id: String) = viewModelScope.launch { repo.deleteEmergencyContact(id) }

    // ── SOS ───────────────────────────────────────────────────────────────────

    fun triggerSOS(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val user = _uiState.value.user ?: return@launch
        val loc = _uiState.value.currentLocation ?: return@launch
        _uiState.update { it.copy(sosTriggerState = SosTriggerState.SENDING) }

        try {
            val alert = SosManager.triggerSOS(
                context = context,
                user = user,
                contacts = _uiState.value.contacts,
                lat = loc.latitude,
                lng = loc.longitude
            )
            repo.triggerSosAlert(alert).fold(
                onSuccess = { _uiState.update { it.copy(sosTriggerState = SosTriggerState.SUCCESS) } },
                onFailure = { _uiState.update { it.copy(sosTriggerState = SosTriggerState.ERROR) } }
            )
        } catch (e: Exception) {
            _uiState.update { it.copy(sosTriggerState = SosTriggerState.ERROR, error = e.message) }
        }
    }

    fun resetSosState() = _uiState.update { it.copy(sosTriggerState = SosTriggerState.IDLE) }

    fun updateProfile(name: String, phone: String) = viewModelScope.launch {
        repo.updateProfile(name, phone)
        val updated = repo.getUserProfile()
        _uiState.update { it.copy(user = updated) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}