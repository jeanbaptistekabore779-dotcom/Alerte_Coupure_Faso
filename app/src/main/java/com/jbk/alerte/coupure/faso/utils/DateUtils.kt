package com.jbk.alerte.coupure.faso.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// On utilise "object" au lieu de "class" ou "data class".
object DateUtils {

    // Cette fonction prend un timestamp (Long) et retourne du texte (String).
    fun formaterDate(timestamp: Long): String {
        // On définit le format qu'on veut afficher
        val formatteur = SimpleDateFormat("dd/MM à HH:mm", Locale.FRANCE)
        // On convertit le timestamp en Date lisible
        return formatteur.format(Date(timestamp))
    }
}