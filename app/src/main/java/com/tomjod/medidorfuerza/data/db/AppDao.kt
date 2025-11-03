package com.tomjod.medidorfuerza.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tomjod.medidorfuerza.data.db.entities.Measurement
import com.tomjod.medidorfuerza.data.db.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Consultas de Perfil ---
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun getProfileById(id: Long): Flow<UserProfile?> // Flow para observar cambios

    @Query("SELECT * FROM user_profiles ORDER BY nombre ASC")
    fun getAllProfiles(): Flow<List<UserProfile>>

    // --- Consultas de Medición ---
    @Insert
    suspend fun insertMeasurement(measurement: Measurement)

    @Query("SELECT * FROM measurements WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getMeasurementsForProfile(profileId: Long): Flow<List<Measurement>>

    @Query("SELECT AVG(forceValue) FROM measurements WHERE profileId = :profileId")
    fun getAverageForceForProfile(profileId: Long): Flow<Float?> // El promedio puede ser nulo
}