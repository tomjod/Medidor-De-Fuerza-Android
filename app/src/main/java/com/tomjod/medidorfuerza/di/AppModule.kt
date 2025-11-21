package com.tomjod.medidorfuerza.di

import android.content.Context
import androidx.room.Room
import com.tomjod.medidorfuerza.data.db.AppDao
import com.tomjod.medidorfuerza.data.db.AppDatabase
import com.tomjod.medidorfuerza.domain.export.CsvExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provee la instancia de la base de datos (Room).
     * Hilt inyecta @ApplicationContext automáticamente.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "force_meter_db"
        ).fallbackToDestructiveMigration() // Ojo: esto borra la DB en migraciones
            .build()
    }

    /**
     * Provee el DAO (Data Access Object) para interactuar con la DB.
     * Hilt sabe cómo proveer AppDatabase gracias a la función de arriba.
     */
    @Provides
    @Singleton
    fun provideAppDao(db: AppDatabase): AppDao {
        return db.appDao()
    }

    /**
     * Provee el exportador de CSV.
     */
    @Provides
    @Singleton
    fun provideCsvExporter(): CsvExporter {
        return CsvExporter()
    }
}

