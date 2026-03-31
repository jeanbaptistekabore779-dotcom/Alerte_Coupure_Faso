package com.jbk.faso.models

data class Communique(
    val id: String = "",
    val titre: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val auteur: String = "SONABEL"
)