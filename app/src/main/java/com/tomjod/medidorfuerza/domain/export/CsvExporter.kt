package com.tomjod.medidorfuerza.domain.export

import com.tomjod.medidorfuerza.data.db.entities.Measurement
import com.tomjod.medidorfuerza.data.db.entities.UserProfile
import java.text.SimpleDateFormat
import java.util.*

/**
 * Clase responsable de generar archivos CSV a partir de mediciones.
 */
class CsvExporter {
    
    companion object {
        private const val CSV_HEADER = "Fecha,Hora,Isquios Avg (N),Isquios Max (N)," +
                "Cuads Avg (N),Cuads Max (N),Ratio H/Q,Duración (s),Notas"
    }
    
    /**
     * Genera un archivo CSV con todas las mediciones de un perfil.
     * 
     * @param measurements Lista de mediciones a exportar
     * @param profile Perfil del atleta
     * @return String con el contenido del CSV
     */
    fun generateCsv(
        measurements: List<Measurement>,
        profile: UserProfile
    ): String {
        if (measurements.isEmpty()) {
            return "$CSV_HEADER\n"
        }
        
        val rows = measurements.joinToString("\n") { measurement ->
            formatMeasurementRow(measurement)
        }
        
        return "$CSV_HEADER\n$rows"
    }
    
    /**
     * Formatea una medición individual como fila CSV.
     */
    private fun formatMeasurementRow(measurement: Measurement): String {
        val date = formatDate(measurement.timestamp)
        val time = formatTime(measurement.timestamp)
        val notes = escapeCsvField(measurement.notes ?: "")
        
        return "$date,$time," +
                "${formatNumber(measurement.isquiosAvg)}," +
                "${formatNumber(measurement.isquiosMax)}," +
                "${formatNumber(measurement.cuadsAvg)}," +
                "${formatNumber(measurement.cuadsMax)}," +
                "${formatNumber(measurement.ratio)}," +
                "${measurement.durationSeconds}," +
                "\"$notes\""
    }
    
    /**
     * Formatea la fecha en formato dd/MM/yyyy.
     */
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Formatea la hora en formato HH:mm:ss.
     */
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Formatea un número flotante con 2 decimales.
     */
    private fun formatNumber(value: Float): String {
        return String.format(Locale.US, "%.2f", value)
    }
    
    /**
     * Escapa caracteres especiales en campos CSV.
     * Reemplaza comillas dobles por dos comillas dobles.
     */
    private fun escapeCsvField(field: String): String {
        return field.replace("\"", "\"\"")
    }
    
    /**
     * Genera el nombre del archivo CSV basado en el perfil y timestamp.
     */
    fun generateFileName(profile: UserProfile): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "ForceMetrics_${profile.nombre}_${profile.apellido}_$timestamp.csv"
    }
}
