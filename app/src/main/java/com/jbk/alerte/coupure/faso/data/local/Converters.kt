package com.jbk.alerte.coupure.faso.data.local

import androidx.room.TypeConverter
import com.jbk.alerte.coupure.faso.models.TypeSignalement

class Converters {
    @TypeConverter
    fun fromType(type: TypeSignalement): String = type.name

    @TypeConverter
    fun toType(value: String): TypeSignalement {
        return try {
            // .uppercase() transforme "Retour" en "RETOUR" avant de chercher
            TypeSignalement.valueOf(value.uppercase())
        } catch (e: Exception) {
            TypeSignalement.COUPURE // Sécurité si la valeur est vraiment inconnue
        }
    }
}