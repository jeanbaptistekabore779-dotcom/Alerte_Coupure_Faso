package com.jbk.alerte.coupure.faso.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties // CRITIQUE : Empêche le crash si Firestore a des champs en plus
data class User(
    var uid: String = "",
    var nom: String = "",
    var prenom: String = "",
    var email: String = "",
    var role: String = "USER",
    var ville: String = "",
    var photoUrl: String? = null,

    @get:PropertyName("estBloque")
    @set:PropertyName("estBloque")
    var estBloque: Boolean = false,

    // Ajout du champ timestamp pour correspondre à ta console Firebase
    var timestamp: Timestamp? = null
)