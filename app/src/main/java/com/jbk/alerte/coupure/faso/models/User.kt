package com.jbk.alerte.coupure.faso.models

import com.google.firebase.firestore.PropertyName

data class User(
    var uid: String = "",
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val role: String = "USER",
    val ville: String = "",
    val photoUrl: String? = null,
    @get:PropertyName("estBloque") @set:PropertyName("estBloque")
    var estBloque: Boolean = false
)