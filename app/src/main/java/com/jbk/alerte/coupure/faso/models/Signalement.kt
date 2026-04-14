package com.jbk.alerte.coupure.faso.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

enum class TypeSignalement {
    COUPURE,
    RETOUR
}

@IgnoreExtraProperties
@Entity(tableName = "signalements")
data class Signalement(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Int = 0,
    var idFirestore: String = "",
    val zone: String = "",
    val status: String = "EN COURS",
    val type: TypeSignalement = TypeSignalement.COUPURE,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String? = null,
    val userId: String = "",
    val description: String = ""
) {
    // Constructeur vide nécessaire pour Firebase
    constructor() : this(idLocal = 0)
}