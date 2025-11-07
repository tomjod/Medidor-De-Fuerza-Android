package com.tomjod.medidorfuerza.ui.features.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat
import com.tomjod.medidorfuerza.data.ble.BleConnectionState

/**
 * Manager para manejar permisos y estado de Bluetooth
 */
class BluetoothPermissionManager(private val context: Context) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    /**
     * Verifica si Bluetooth está soportado en el dispositivo
     */
    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * Verifica si Bluetooth está habilitado
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Obtiene los permisos necesarios según la versión de Android
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // Android < 12
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Verifica si todos los permisos necesarios están concedidos
     */
    fun hasAllPermissions(): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Obtiene el estado actual de Bluetooth y permisos
     */
    fun getCurrentBluetoothState(): BleConnectionState {
        return when {
            !isBluetoothSupported() -> BleConnectionState.BluetoothNotSupported
            !hasAllPermissions() -> BleConnectionState.PermissionsRequired
            !isBluetoothEnabled() -> BleConnectionState.BluetoothDisabled
            else -> BleConnectionState.Disconnected
        }
    }

    /**
     * Crea el Intent para habilitar Bluetooth
     */
    fun createEnableBluetoothIntent(): Intent {
        return Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    }

    /**
     * Obtiene los permisos faltantes
     */
    fun getMissingPermissions(): List<String> {
        return getRequiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Obtiene nombres amigables para los permisos
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.BLUETOOTH_SCAN -> "Escanear dispositivos Bluetooth"
            Manifest.permission.BLUETOOTH_CONNECT -> "Conectar dispositivos Bluetooth"
            Manifest.permission.BLUETOOTH -> "Bluetooth"
            Manifest.permission.BLUETOOTH_ADMIN -> "Administrar Bluetooth"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Ubicación precisa"
            else -> permission
        }
    }
}