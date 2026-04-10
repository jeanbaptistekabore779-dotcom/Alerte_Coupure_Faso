package com.jbk.alerte.coupure.faso.models

import com.google.firebase.Timestamp

data class Alerte(
    var id: String = "",
    val quartier: String = "",
    val type: String = "",
    val ville: String = "Ouagadougou",
    val description: String = "",
    val status: String = "EN COURS",
    val timestamp: Timestamp? = null,
    val auteurEmail: String = ""
)