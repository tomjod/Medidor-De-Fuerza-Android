package com.tomjod.medidorfuerza.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tomjod.medidorfuerza.data.db.entities.Measurement
import com.tomjod.medidorfuerza.data.db.entities.UserProfile

/**
 * Clase principal de la base de datos Room.
 * Lista todas las entidades (tablas) que contendrá la base de datos.
 */
@Database(
    entities = [UserProfile::class, Measurement::class],
    version = 1, // Si cambias la estructura de las tablas, debes incrementar esta versión
    exportSchema = false // No exportamos el esquema por ahora
)
abstract class AppDatabase : RoomDatabase() {

    // Room implementará esta función abstracta por nosotros.
    abstract fun appDao(): AppDao
}
