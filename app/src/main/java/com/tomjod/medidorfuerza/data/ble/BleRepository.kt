package com.tomjod.medidorfuerza.data.ble

import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz (el contrato) para el repositorio de Bluetooth.
 * Define qué puede hacer el servicio, abstrayendo la implementación.
 */
interface BleRepository {

    /**
     * Un Flow que emite el estado actual de la conexión.
     */
    val connectionState: StateFlow<BleConnectionState>

    /**
     * Un Flow que emite la última lectura de fuerza. Nulo si no hay datos.
     */
    val forceData: StateFlow<Float?>

    /**
     * Inicia el escaneo de dispositivos BLE.
     */
    fun startScan()

    /**
     * Envía el comando "Tarar" (poner en cero) al dispositivo.
     */
    fun sendTareCommand()

    /**
     * Desconecta del dispositivo BLE actual.
     */
    fun disconnect()

    /**
     * Libera recursos. Llamar cuando el repositorio ya no sea necesario.
     */
    fun release()
}
