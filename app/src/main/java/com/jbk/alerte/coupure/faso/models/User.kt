package com.jbk.alerte.coupure.faso.models

import com.google.firebase.firestore.PropertyName

data class User(
    var uid: String = "",
    var nom: String = "",
    var prenom: String = "",
    var email: String = "",
    var role: String = "USER",
    var ville: String = "",
    var photoUrl: String? = null,

    // Utilisation de @get et @set pour correspondre exactement au nom dans Firestore
    @get:PropertyName("estBloque")
    @set:PropertyName("estBloque")
    var estBloque: Boolean = false
) {
    // Le constructeur vide est généré automatiquement grâce aux valeurs par défaut
}