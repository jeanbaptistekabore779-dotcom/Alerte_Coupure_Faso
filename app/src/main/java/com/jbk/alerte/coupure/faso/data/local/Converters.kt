package com.jbk.alerte.coupure.faso.data.local

import androidx.room.TypeConverter
import com.jbk.alerte.coupure.faso.models.TypeSignalement

class Converters {
    @TypeConverter
    fun fromType(type: TypeSignalement): String = type.name

    @TypeConverter
    fun toType(value: String): TypeSignalement {
        return try {
            TypeSignalement.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TypeSignalement.COUPURE // Valeur par défaut en cas d'erreur
        }
    }
}