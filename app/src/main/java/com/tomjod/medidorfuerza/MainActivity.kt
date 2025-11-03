package com.tomjod.medidorfuerza

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.tomjod.medidorfuerza.data.db.entities.UserProfile
import com.tomjod.medidorfuerza.ui.features.measurent.ForceMeterScreen
import com.tomjod.medidorfuerza.ui.theme.TextColorPrimary
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // --- 1. INYECTAR EL VIEWMODEL ---
    // Esta es la forma moderna de obtener la instancia del ViewModel.
    private val viewModel: ForceMeterViewModel by viewModels()

    // Lanzador de permisos (queda igual)
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* ... lógica de permisos ... */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Pedir permisos de BLE al inicio
        // checkBlePermissions()

        setContent {
            // --- 2. OBSERVAR EL ESTADO DEL VIEWMODEL ---

            // Recolecta el estado de la UI (UiState) del ViewModel.
            // 'collectAsState' hace que Compose se redibuje cuando el estado cambia.
            val uiState by viewModel.uiState.collectAsState()

            // Recolecta el perfil activo.
            val profile by viewModel.currentProfile.collectAsState()

            // Recolecta el promedio calculado.
            val average by viewModel.averageForce.collectAsState()

            // --- 3. PASAR EL ESTADO Y LOS EVENTOS A LA UI ---
            ForceMeterScreen(
                uiState = uiState,
                profile = profile,
                averageForce = average ?: 0.0f, // Maneja el nulo
                onActionButtonClick = {
                    viewModel.onActionButtonClick()
                },
                onSaveMeasurementClick = {
                    viewModel.saveCurrentMeasurement()
                }
            )
        }
    }
}



// --- Componente de Cabecera de Perfil (ACTUALIZADO) ---






