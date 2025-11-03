package com.tomjod.medidorfuerza.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tomjod.medidorfuerza.data.db.entities.Measurement
import com.tomjod.medidorfuerza.data.db.entities.UserProfile

@Database(entities = [UserProfile::class, Measurement::class], version = 1, exportSchema = false)
public abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "force_meter_database"
                )
                    .fallbackToDestructiveMigration() // Ojo: Borra datos si cambias la 'version'
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}