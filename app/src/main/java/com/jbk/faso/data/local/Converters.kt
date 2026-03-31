package com.jbk.alerte.coupure.faso.data.local

import androidx.room.TypeConverter
import com.jbk.alerte.coupure.faso.models.TypeSignalement

class Converters {
    @TypeConverter
    fun fromType(type: TypeSignalement): String = type.name

    @TypeConverter
    fun toType(value: String): TypeSignalement = TypeSignalement.valueOf(value)
}