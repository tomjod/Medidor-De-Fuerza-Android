package com.tomjod.medidorfuerza.data.db

import androidx.room.TypeConverter
import com.tomjod.medidorfuerza.data.db.entities.Gender

/**
 * Converters para Room para manejar tipos de datos personalizados
 */
class Converters {

    @TypeConverter
    fun fromGender(gender: Gender): String {
        return gender.name
    }

    @TypeConverter
    fun toGender(genderString: String): Gender {
        return Gender.valueOf(genderString)
    }
}