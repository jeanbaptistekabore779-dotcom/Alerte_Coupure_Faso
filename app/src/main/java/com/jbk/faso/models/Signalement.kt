package com.jbk.alerte.coupure.faso.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TypeSignalement {
    COUPURE,
    RETOUR
}

@Entity(tableName = "signalements") // Nom de la table en SQL
data class Signalement(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Int = 0, // Clé primaire auto-incrémentée pour le téléphone
    val idFirestore: String = "", // Utile plus tard pour Firebase
    val zone: String = "",
    val type: TypeSignalement = TypeSignalement.COUPURE,
    val timestamp: Long = System.currentTimeMillis(),
)