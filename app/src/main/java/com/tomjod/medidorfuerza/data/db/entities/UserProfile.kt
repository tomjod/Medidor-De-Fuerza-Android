package com.tomjod.medidorfuerza.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enumeración para representar el sexo del atleta
 */
enum class Gender(val displayName: String) {
    MASCULINO("Masculino"),
    FEMENINO("Femenino")
}

/**
 * Define la tabla 'user_profiles' en la base de datos.
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val apellido: String,
    val edad: Int,
    val sexo: Gender,
    val fotoUri: String? = null // Room guarda mejor los URIs como String
)