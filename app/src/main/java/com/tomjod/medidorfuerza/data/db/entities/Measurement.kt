package com.tomjod.medidorfuerza.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tomjod.medidorfuerza.data.db.entities.UserProfile

/**
 * Define la tabla 'measurements'
 * Usa una ForeignKey para relacionar cada medición con un perfil de usuario.
 */
@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.Companion.CASCADE // Si se borra un perfil, se borran sus mediciones
        )
    ],
    indices = [Index(value = ["profileId"])]

)
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long, // La llave foránea
    val forceValue: Float,
    val timestamp: Long = System.currentTimeMillis()
)