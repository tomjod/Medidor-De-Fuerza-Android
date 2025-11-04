package com.tomjod.medidorfuerza.ui.features.measurement

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.tomjod.medidorfuerza.data.ble.BleConnectionState
import com.tomjod.medidorfuerza.data.db.entities.UserProfile

// --- Define tus colores en ui/theme/Color.kt ---
private val AppBackgroundColor = Color(0xFF1C1C1E)
private val TextColorPrimary = Color.White
private val TextColorSecondary = Color(0xFFAEAEB2)
private val ButtonColor = Color(0xFF0A84FF)
private val ButtonColorSecondary = Color(0xFF303032)
private val StatusColorConnected = Color(0xFF34C759)
private val StatusColorConnecting = Color(0xFFFF9500)
private val StatusColorDisconnected = Color(0xFFFF3B30)

// --- 1. CONTENEDOR STATEFUL (CON LÓGICA) ---

/**
 * Este es el composable "inteligente" o "contenedor".
 * Se encarga de:
 * 1. Inyectar el ViewModel.
 * 2. Colectar los estados (States).
 * 3. Manejar la lógica de permisos.
 * 4. Pasar los datos simples al composable "tonto" (Stateless).
 */
@Composable
fun ForceMeterRoute(
    navController: NavController,
    viewModel: ForceMeterViewModel = hiltViewModel()
) {
    // --- Colectamos los estados del ViewModel ---
    // Usamos collectAsStateWithLifecycle para seguridad en el ciclo de vida.
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val averageForce by viewModel.averageForce.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val latestForce by viewModel.latestForce.collectAsStateWithLifecycle()

    // --- Lógica de Permisos de Bluetooth ---
    val context = LocalContext.current
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            // Si los permisos fueron concedidos AHORA, iniciamos el escaneo.
            viewModel.onEvent(MeasurementEvent.ConnectClicked)
        } else {
            // TODO: Mostrar un SnackBar o mensaje al usuario indicando que
            // los permisos son necesarios para conectar.
        }
    }

    ForceMeterScreen(
        profile = profile,
        averageForce = averageForce,
        connectionState = connectionState,
        latestForce = latestForce,
        onEvent = viewModel::onEvent, // Pasamos la función de eventos directamente
        onNavigateBack = {
            navController.navigateUp()
        },
        onConnectClick = {
            // Esta lambda especial maneja la lógica de permisos
            val allGranted = requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) {
                // Si ya tenemos permisos, escaneamos.
                viewModel.onEvent(MeasurementEvent.ConnectClicked)
            } else {
                // Si no, pedimos permisos.
                permissionLauncher.launch(requiredPermissions)
            }
        }
    )
}

// --- 2. COMPOSABLE STATELESS (SIN LÓGICA, SOLO UI) ---

/**
 * Este es el composable "tonto" o "de presentación".
 * - No sabe nada sobre el ViewModel.
 * - Recibe todos los datos como parámetros.
 * - Devuelve todos los eventos a través de lambdas (onEvent, onNavigateBack, etc.).
 * - Es 100% previsualizable en el editor de Compose.
 */
@Composable
fun ForceMeterScreen(
    profile: UserProfile?,
    averageForce: Float?,
    connectionState: BleConnectionState,
    latestForce: Float?,
    onEvent: (MeasurementEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onConnectClick: () -> Unit
) {
    // --- Lógica de UI (derivación de estado) ---
    // Derivamos el texto y color del estado de conexión.
    val (statusText, statusColor) = when (connectionState) {
        is BleConnectionState.Connected -> "¡Conectado!" to StatusColorConnected
        is BleConnectionState.Connecting -> "Conectando..." to StatusColorConnecting
        is BleConnectionState.Disconnected -> "Desconectado" to StatusColorDisconnected
        is BleConnectionState.Scanning -> "Buscando..." to StatusColorConnecting
        is BleConnectionState.Error -> connectionState.message to StatusColorDisconnected
    }

    val isConnected = connectionState is BleConnectionState.Connected

    // --- UI ---
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Cabecera (con botón de Atrás)
            ProfileHeader(
                profile = profile,
                onBackClick = onNavigateBack // Evento de navegación
            )

            // 2. Indicador de Estado
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            // 3. Bloque de Lectura de Fuerza (Centrado)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = String.format("%.1f", latestForce ?: 0.0f),
                    color = TextColorPrimary,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "KG",
                    color = TextColorSecondary,
                    fontSize = 34.sp,
                    textAlign = TextAlign.Center
                )

                // 4. Muestra de Promedio
                Text(
                    text = "Promedio: ${String.format("%.1f", averageForce ?: 0.0f)} KG",
                    color = TextColorSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            // 5. Botones de Acción
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón Principal (Conectar / Tarar)
                if (isConnected) {
                    // Si está conectado, el botón es "Tarar"
                    Button(
                        onClick = { onEvent(MeasurementEvent.TareClicked) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text("Poner en Cero (Tarar)", fontSize = 18.sp)
                    }
                } else {
                    // Si está desconectado, el botón es "Conectar"
                    Button(
                        onClick = onConnectClick, // Llama a la lambda con lógica de permisos
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                        enabled = connectionState !is BleConnectionState.Connecting && connectionState !is BleConnectionState.Scanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text("Buscar y Conectar", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Secundario (Guardar / Desconectar)
                if (isConnected) {
                    Button(
                        onClick = { onEvent(MeasurementEvent.SaveClicked) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonColorSecondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text("Guardar Medición", fontSize = 18.sp)
                    }
                } else {
                    // Puedes poner otro botón aquí si quieres, o dejarlo vacío
                }
            }
        }
    }
}

/**
 * Componente de UI separado para la cabecera.
 */
@Composable
private fun ProfileHeader(
    profile: UserProfile?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de "Atrás"
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = TextColorPrimary
            )
        }

        // Foto de Perfil
        Image(
            painter = if (profile?.fotoUri != null) {
                rememberAsyncImagePainter(model = Uri.parse(profile.fotoUri))
            } else {
                rememberVectorPainter(image = Icons.Default.Person)
            },
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .border(2.dp, Color.White, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info de Perfil
        if (profile != null) {
            Column {
                Text(
                    text = "${profile.nombre} ${profile.apellido}",
                    color = TextColorPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${profile.edad} años",
                    color = TextColorSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            // Muestra un placeholder mientras carga el perfil
            Text(text = "Cargando perfil...", color = TextColorSecondary)
        }
    }
}

// --- 3. PREVIEW ---

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun ForceMeterScreenPreview_Connected() {
    ForceMeterScreen(
        profile = UserProfile(id = 1, nombre = "Andrea", apellido = "Zunino", edad = 23, fotoUri = null),
        averageForce = 25.5f,
        connectionState = BleConnectionState.Connected,
        latestForce = 30.1f,
        onEvent = {},
        onNavigateBack = {},
        onConnectClick = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun ForceMeterScreenPreview_Disconnected() {
    ForceMeterScreen(
        profile = UserProfile(id = 1, nombre = "Andrea", apellido = "Zunino", edad = 23, fotoUri = null),
        averageForce = 25.5f,
        connectionState = BleConnectionState.Disconnected,
        latestForce = 0.0f,
        onEvent = {},
        onNavigateBack = {},
        onConnectClick = {}
    )
}