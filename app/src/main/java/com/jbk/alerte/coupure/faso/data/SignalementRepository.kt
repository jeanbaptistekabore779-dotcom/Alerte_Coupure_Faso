package com.jbk.alerte.coupure.faso.data

import com.jbk.alerte.coupure.faso.data.local.SignalementDao
import com.jbk.alerte.coupure.faso.models.Signalement
import kotlinx.coroutines.flow.Flow

class SignalementRepository(private val signalementDao: SignalementDao) {

    // Récupère tous les signalements locaux
    val allSignalements: Flow<List<Signalement>> = signalementDao.getAll()

    // Insertion locale
    suspend fun insert(signalement: Signalement) {
        signalementDao.insert(signalement)
    }

    // Ici, on pourra ajouter la logique de synchronisation Firestore
    suspend fun syncWithFirestore() {
        val unsynced = signalementDao.getUnsynced()
        unsynced.forEach { signalement ->
            // TODO: Logique d'envoi vers Firebase
            // Une fois réussi : signalementDao.updateFirestoreId(...)
        }
    }
}