package com.tomjod.medidorfuerza.ui.features.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tomjod.medidorfuerza.data.ble.BleConnectionState
import com.tomjod.medidorfuerza.data.ble.ForceReadings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothConfigScreen(
    navController: NavController,
    viewModel: BluetoothConfigViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val forceData by viewModel.forceData.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Launcher for permissions
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onPermissionsResult(permissions)
    }

    // Launcher for enabling Bluetooth
    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.onBluetoothStateChanged()
    }

    // Auto-request permissions and enable Bluetooth
    LaunchedEffect(Unit) {
        val missingPermissions = viewModel.getMissingPermissions()
        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            // Check Bluetooth status initially
            viewModel.checkBluetoothStatus()
        }
    }

    // React to state changes for auto-enabling Bluetooth
    LaunchedEffect(connectionState) {
        if (connectionState is BleConnectionState.BluetoothDisabled) {
             // Auto-prompt to enable Bluetooth if disabled
             enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        
        if (connectionState is BleConnectionState.Error) {
            snackbarHostState.showSnackbar((connectionState as BleConnectionState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Bluetooth") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Estado de conexión
                ConnectionStatusCard(
                    connectionState = connectionState,
                    onScanClick = viewModel::startScan,
                    onDisconnectClick = viewModel::disconnect,
                    onEnableBluetoothClick = {
                        enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    onRequestPermissionsClick = {
                        val missing = viewModel.getMissingPermissions()
                        if (missing.isNotEmpty()) {
                            requestPermissionLauncher.launch(missing.toTypedArray())
                        }
                    }
                )
            }

            item {
                // Controles del dispositivo ESP32
                if (connectionState is BleConnectionState.Connected) {
                    DeviceControlCard(
                        forceData = forceData,
                        onTareClick = viewModel::sendTareCommand
                    )
                }
            }

            item {
                // Información técnica
                TechnicalInfoCard()
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    connectionState: BleConnectionState,
    onScanClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onEnableBluetoothClick: () -> Unit,
    onRequestPermissionsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Estado de Conexión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            when (connectionState) {
                is BleConnectionState.Disconnected -> {
                    Text(
                        text = "El medidor de fuerza no está conectado. Presiona 'Buscar Dispositivo' para conectarte al ESP32.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onScanClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar Dispositivo ESP32")
                    }
                }

                is BleConnectionState.Scanning, is BleConnectionState.Connecting -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (connectionState is BleConnectionState.Scanning) "Buscando dispositivos..." else "Conectando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Disable button to prevent multiple clicks
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Procesando...")
                    }
                }

                is BleConnectionState.Connected -> {
                    Text(
                        text = "✅ Dispositivo ESP32 conectado correctamente. El medidor está listo para usar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedButton(
                        onClick = onDisconnectClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Desconectar")
                    }
                }

                is BleConnectionState.Error -> {
                    Text(
                        text = "❌ ${connectionState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onScanClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }

                is BleConnectionState.BluetoothDisabled -> {
                    Text(
                        text = "🔵 Bluetooth está deshabilitado. Actívalo para continuar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onEnableBluetoothClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Bluetooth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Activar Bluetooth")
                    }
                }

                is BleConnectionState.PermissionsRequired -> {
                    Text(
                        text = "🔒 Se requieren permisos de Bluetooth. Concédelos para continuar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onRequestPermissionsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Conceder Permisos")
                    }
                }

                is BleConnectionState.BluetoothNotSupported -> {
                    Text(
                        text = "❌ Este dispositivo no soporta Bluetooth LE.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceControlCard(
    forceData: ForceReadings?,
    onTareClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚙️ Controles del Dispositivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (forceData != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Lectura Actual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Ratio: ${String.format(Locale.getDefault(), "%.2f", forceData.ratio)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "I: ${String.format(Locale.getDefault(), "%.1f", forceData.isquios)} / C: ${String.format(Locale.getDefault(), "%.1f", forceData.cuads)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = onTareClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Tarar (Poner en Cero)")
            }

            Text(
                text = "💡 La función 'Tarar' establece la lectura actual como punto cero para mediciones relativas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TechnicalInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔧 Información Técnica",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            val technicalSpecs = listOf(
                "Dispositivo" to "ESP32 con sensor de fuerza",
                "Protocolo" to "Bluetooth Classic (SPP)",
                "Rango" to "0 - 1000 N (configurable)",
                "Precisión" to "±0.1 N",
                "Frecuencia" to "10 Hz de muestreo"
            )

            technicalSpecs.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}