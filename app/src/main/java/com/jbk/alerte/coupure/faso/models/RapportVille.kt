package com.jbk.alerte.coupure.faso.models


data class RapportVille(
    val nomVille: String = "",
    val totalAlertes: Int = 0,
    val enCours: Int = 0,
    val resolues: Int = 0
)