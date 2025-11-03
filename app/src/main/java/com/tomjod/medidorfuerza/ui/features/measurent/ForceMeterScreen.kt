package com.tomjod.medidorfuerza.ui.features.measurent

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.rememberAsyncImagePainter
import com.tomjod.medidorfuerza.ui.theme.AppBackgroundColor
import com.tomjod.medidorfuerza.UiState
import com.tomjod.medidorfuerza.data.db.entities.UserProfile
import com.tomjod.medidorfuerza.ui.theme.ButtonColor
import com.tomjod.medidorfuerza.ui.theme.ButtonColorSecondary
import com.tomjod.medidorfuerza.ui.theme.TextColorPrimary
import com.tomjod.medidorfuerza.ui.theme.TextColorSecondary

@Composable
fun ForceMeterScreen(
    uiState: UiState,
    profile: UserProfile?,
    averageForce: Float,
    onActionButtonClick: () -> Unit,
    onSaveMeasurementClick: () -> Unit
) {
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

            // 1. Cabecera de Perfil
            if (profile != null) {
                ProfileHeader(profile = profile, modifier = Modifier.padding(top = 8.dp))
            }

            // 2. Indicador de Estado de Conexión
            Text(
                text = uiState.connectionStatus,
                color = Color(android.graphics.Color.parseColor(uiState.statusColorHex)), // Color desde el state
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
                    text = uiState.forceReading,
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
                    text = "Promedio: ${String.format("%.1f", averageForce)} KG",
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
                Button(
                    onClick = onActionButtonClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = uiState.buttonText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Secundario (Guardar Medición)
                Button(
                    onClick = onSaveMeasurementClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonColorSecondary),
                    enabled = uiState.isConnected, // Habilitado solo si está conectado
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = "Guardar Medición",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(profile: UserProfile, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Foto de Perfil
        Image(
            painter = if (profile.fotoUri != null) {
                rememberAsyncImagePainter(model = Uri.parse(profile.fotoUri)) // Convertir String a Uri
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
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun DefaultPreview() {
    ForceMeterScreen(
        uiState = UiState(),
        profile = UserProfile(
            id = 1,
            nombre = "Andrea",
            apellido = "Zunino",
            edad = 23,
            fotoUri = null
        ),
        averageForce = 0.0f,
        onActionButtonClick = {},
        onSaveMeasurementClick = {}
    )
}
// ... (Preview Conectado)