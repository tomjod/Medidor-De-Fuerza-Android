package com.tomjod.medidorfuerza.data.ble

/**
 * Clase sellada para representar los estados de la conexión BLE.
 */
sealed class BleConnectionState {
    object Disconnected : BleConnectionState()
    object Scanning : BleConnectionState()
    object Connecting : BleConnectionState()
    object Connected : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}
