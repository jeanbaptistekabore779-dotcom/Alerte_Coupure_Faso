package com.jbk.alerte.coupure.faso.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.jbk.alerte.coupure.faso.models.Signalement
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalementDao {
    @Query("SELECT * FROM signalements ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Signalement>>

    @Insert
    suspend fun insert(signalement: Signalement)

    // Utile pour récupérer uniquement ce qui n'est pas encore sur le serveur
    @Query("SELECT * FROM signalements WHERE idFirestore = ''")
    suspend fun getUnsynced(): List<Signalement>

    // Pour mettre à jour l'ID une fois la synchro Firebase réussie
    @Query("UPDATE signalements SET idFirestore = :firestoreId WHERE idLocal = :localId")
    suspend fun updateFirestoreId(localId: Int, firestoreId: String)

    @Query("DELETE FROM signalements")
    suspend fun deleteAll()

    // Pour supprimer un signalement précis après synchro (optionnel)
    @Delete
    suspend fun delete(signalement: Signalement)
}