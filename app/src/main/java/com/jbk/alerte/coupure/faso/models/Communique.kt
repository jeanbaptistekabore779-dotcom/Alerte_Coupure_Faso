package com.jbk.alerte.coupure.faso.models

data class Communique(
    var id: String = "",
    val titre: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val auteur: String = "SONABEL"

)