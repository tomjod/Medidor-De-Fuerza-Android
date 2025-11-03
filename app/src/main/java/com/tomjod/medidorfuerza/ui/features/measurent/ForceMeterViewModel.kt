package com.tomjod.medidorfuerza.ui.features.measurent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomjod.medidorfuerza.data.ble.BleConnectionState // Asegúrate que la ruta es correcta
import com.tomjod.medidorfuerza.data.ble.BleRepository
import com.tomjod.medidorfuerza.data.db.AppDao
import com.tomjod.medidorfuerza.data.db.entities.Measurement
import com.tomjod.medidorfuerza.data.db.entities.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val profile: UserProfile? = null,
    val connectionState: BleConnectionState = BleConnectionState.Disconnected,
    val latestForce: Float? = null,
    val averageForce: Float? = null
)

@HiltViewModel
class ForceMeterViewModel @Inject constructor(
    private val bleRepository: BleRepository,
    private val appDao: AppDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- CORRECCIÓN 1: INICIALIZACIÓN DIRECTA ---
    // Obtenemos el ID (no-nulo) del NavController.
    // La app crasheará si no hay ID, lo cual es correcto para esta pantalla.
    private val profileId: Long = checkNotNull(savedStateHandle["profileId"])

    // Inicializamos el Flow directamente con el ID.
    // Ahora es un Flow<Long>, no un Flow<Long?>.
    private val _activeProfileId = MutableStateFlow(profileId)

    // El bloque 'init' que teníamos para setear esto ya no es necesario.

    // FLOW 1: Obtiene el perfil activo
    // --- CORRECCIÓN 2: SIMPLIFICADO ---
    // Ya no necesitamos el 'if (id == null)' porque _activeProfileId nunca es nulo.
    val activeProfile: StateFlow<UserProfile?> = _activeProfileId.flatMapLatest { id ->
        appDao.getProfileById(id) // Retorna Flow<UserProfile>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    ) // <-- El error 4 (stateIn) desaparece al arreglar el tipo.

    // FLOW 2: Combina todo en un UiState
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> = combine(
        activeProfile, // Tipo claro: Flow<UserProfile?>
        bleRepository.connectionState,
        bleRepository.forceData,

        // --- CORRECCIÓN 3: SIMPLIFICADO Y SIN TYPO ---
        // 1. Usamos _activeProfileId (sin el typo)
        // 2. Quitamos el 'if (id == null)'
        _activeProfileId.flatMapLatest { id ->
            appDao.getAverageForceForProfile(id) // Retorna Flow<Float?>
        }
    ) { profile, connectionState, forceData, avgMeasurement ->
        UiState(
            profile = profile,
            connectionState = connectionState,
            latestForce = forceData,
            averageForce = avgMeasurement
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    ) // <-- El error 4 (stateIn) desaparece al arreglar el tipo.

    // Eventos de la UI
    fun onEvent(event: MeasurementEvent) {
        when (event) {
            MeasurementEvent.Connect -> bleRepository.startScan()
            MeasurementEvent.Disconnect -> bleRepository.disconnect()
            MeasurementEvent.Tare -> bleRepository.sendTareCommand()
            is MeasurementEvent.SaveMeasurement -> {
                val force = event.forceValue
                if (force != null) {
                    viewModelScope.launch {
                        appDao.insertMeasurement(
                            Measurement(
                                profileId  = profileId, // Usamos el ID no-nulo
                                forceValue = force
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleRepository.release()
    }
}

// Eventos que la UI puede enviar al ViewModel
sealed interface MeasurementEvent {
    object Connect : MeasurementEvent
    object Disconnect : MeasurementEvent
    object Tare : MeasurementEvent
    data class SaveMeasurement(val forceValue: Float?) : MeasurementEvent
}