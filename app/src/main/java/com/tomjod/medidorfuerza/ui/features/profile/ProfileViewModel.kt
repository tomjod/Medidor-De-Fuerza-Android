package com.tomjod.medidorfuerza.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomjod.medidorfuerza.data.db.AppDao
import com.tomjod.medidorfuerza.data.db.entities.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appDao: AppDao
) : ViewModel() {

    /**
     * Expone la lista de todos los perfiles de la base de datos.
     */
    val profiles: StateFlow<List<UserProfile>> = appDao.getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Comienza con una lista vacía
        )

    /**
     * Crea un nuevo perfil de prueba.
     */
    fun createTestProfile() {
        viewModelScope.launch {
            val testProfile = UserProfile(
                // Room generará el ID automáticamente
                nombre = "Andrea",
                apellido = "Zunino (Test)",
                edad = 23,
                fotoUri = null // TODO: Implementar selección de foto
            )
            appDao.insertProfile(testProfile)
        }
    }
}
