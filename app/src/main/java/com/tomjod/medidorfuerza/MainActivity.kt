package com.tomjod.medidorfuerza

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.tomjod.medidorfuerza.ui.navigation.AppNavigation
import com.tomjod.medidorfuerza.ui.theme.MedidorfuerzaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MedidorfuerzaTheme {
                Surface(/*...*/) {
                    AppNavigation()
                }
            }
        }
    }
}






