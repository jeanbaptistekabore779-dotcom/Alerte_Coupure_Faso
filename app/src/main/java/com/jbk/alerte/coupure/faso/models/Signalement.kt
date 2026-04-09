package com.jbk.alerte.coupure.faso.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TypeSignalement {
    COUPURE,
    RETOUR
}

@Entity(tableName = "signalements")
data class Signalement(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Int = 0,
    var idFirestore: String = "",
    val zone: String = "",
    val type: TypeSignalement = TypeSignalement.COUPURE,
    val timestamp: Long = System.currentTimeMillis(), // Virgule ajoutée ici
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)