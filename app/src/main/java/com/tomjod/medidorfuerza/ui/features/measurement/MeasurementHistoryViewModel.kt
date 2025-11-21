package com.tomjod.medidorfuerza.ui.features.measurement

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomjod.medidorfuerza.data.db.AppDao
import com.tomjod.medidorfuerza.data.db.entities.Measurement
import com.tomjod.medidorfuerza.data.db.entities.UserProfile
import com.tomjod.medidorfuerza.data.repositories.MeasurementRepository
import com.tomjod.medidorfuerza.domain.export.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel para la pantalla de historial de mediciones.
 * Muestra todas las mediciones guardadas para un perfil específico.
 */
@HiltViewModel
class MeasurementHistoryViewModel @Inject constructor(
    private val measurementRepository: MeasurementRepository,
    private val appDao: AppDao,
    private val csvExporter: CsvExporter,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: Long = checkNotNull(savedStateHandle["profileId"])

    /**
     * Perfil del usuario actual.
     */
    val profile: StateFlow<UserProfile?> = appDao.getProfileById(profileId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Lista de todas las mediciones para este perfil, ordenadas por fecha (más reciente primero).
     */
    val measurements: StateFlow<List<Measurement>> =
        measurementRepository.getMeasurementsForProfile(profileId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Estado de la exportación.
     */
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    /**
     * Elimina una medición de la base de datos.
     */
    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            measurementRepository.deleteMeasurement(id)
        }
    }

    /**
     * Exporta las mediciones a CSV y abre el diálogo de compartir.
     */
    fun exportMeasurements() {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            try {
                // Esperar a que el perfil se cargue (primer valor no nulo)
                val currentProfile = profile.firstOrNull { it != null }
                if (currentProfile == null) {
                    _exportState.value = ExportState.Error("Perfil no encontrado")
                    return@launch
                }

                val measurementsList = measurements.first()
                if (measurementsList.isEmpty()) {
                    _exportState.value = ExportState.Error("No hay mediciones para exportar")
                    return@launch
                }

                // Generar CSV
                val csvContent = csvExporter.generateCsv(measurementsList, currentProfile)
                
                // Guardar en cache
                val file = saveCsvToCache(csvContent, currentProfile)
                
                // Obtener URI para compartir
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                // Compartir archivo
                shareFile(uri)
                
                _exportState.value = ExportState.Success
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Guarda el contenido CSV en el directorio de cache.
     */
    private fun saveCsvToCache(csvContent: String, profile: UserProfile): File {
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }

        val fileName = csvExporter.generateFileName(profile)
        val file = File(exportsDir, fileName)
        file.writeText(csvContent)

        return file
    }

    /**
     * Abre el diálogo de compartir archivo.
     */
    private fun shareFile(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Mediciones ForceMetrics")
            putExtra(Intent.EXTRA_TEXT, "Adjunto archivo con mediciones de fuerza")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir mediciones")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Resetea el estado de exportación.
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}

/**
 * Estados posibles de la exportación.
 */
sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    object Success : ExportState()
    data class Error(val message: String) : ExportState()
}
