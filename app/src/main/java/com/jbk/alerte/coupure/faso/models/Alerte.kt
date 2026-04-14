package com.jbk.alerte.coupure.faso.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Alerte(
    var id: String = "",
    val quartier: String = "",
    val type: String = "",
    val ville: String = "Ouagadougou",
    val status: String = "EN COURS",
    val timestamp: com.google.firebase.Timestamp? = null, // Bien utiliser le type Timestamp?
    val auteurEmail: String = "",
    val auteurPhotoUrl: String? = null
) {
    // Constructeur vide pour Firebase
    constructor() : this("", "", "", "Ouagadougou", "EN COURS", null, "", null)
}