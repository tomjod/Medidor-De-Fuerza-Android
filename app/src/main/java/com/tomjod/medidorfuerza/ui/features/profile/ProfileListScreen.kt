package com.tomjod.medidorfuerza.ui.features.profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import com.tomjod.medidorfuerza.ui.navigation.Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Pantalla (placeholder) para mostrar la lista de perfiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    navController: NavController
    // TODO: Inyectar ProfileViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Seleccionar Perfil") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Aquí irá la lista de perfiles.")

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de simulación
            Button(onClick = {
                // Simulamos que seleccionamos el Perfil con ID 1
                // (El perfil 1 se crea en el init del ViewModel)
                val fakeProfileId = 1L
                navController.navigate(Screen.Measurement.createRoute(fakeProfileId))
            }) {
                Text("Simular: Ir a Medición (Perfil 1)")
            }

            // TODO: Añadir un FloatingActionButton para crear perfiles nuevos
            // fab = { ... navController.navigate(Screen.ProfileCreate.route) ... }
        }
    }
}
