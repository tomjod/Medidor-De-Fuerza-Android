package com.tomjod.medidorfuerza

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.tomjod.medidorfuerza.ui.navigation.AppNavigation
import com.tomjod.medidorfuerza.ui.theme.MedidorfuerzaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MedidorfuerzaTheme {
                // Global permission handling
                val context = androidx.compose.ui.platform.LocalContext.current
                val permissionManager = remember { com.tomjod.medidorfuerza.ui.features.bluetooth.BluetoothPermissionManager(context) }
                
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                ) { 
                    // Permissions result handled, app continues
                }

                LaunchedEffect(Unit) {
                    val missingPermissions = permissionManager.getMissingPermissions()
                    if (missingPermissions.isNotEmpty()) {
                        requestPermissionLauncher.launch(missingPermissions.toTypedArray())
                    }
                }

                Surface(/*...*/) {
                    AppNavigation()
                }
            }
        }
    }
}






