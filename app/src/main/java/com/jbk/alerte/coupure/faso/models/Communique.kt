package com.jbk.alerte.coupure.faso.models

import com.google.firebase.Timestamp

data class Communique(
    var id: String = "",
    val titre: String = "",
    val message: String = "",
    // ✅ Doit s'appeler "date" pour correspondre EXACTEMENT au champ Firestore
    val date: Timestamp? = null,
    val auteur: String = "SONABEL"
)