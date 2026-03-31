package com.jbk.alerte.coupure.faso.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jbk.alerte.coupure.faso.models.Signalement

@Dao
interface SignalementDao {
    @Query("SELECT * FROM signalements ORDER BY timestamp DESC")
    fun getAll(): List<Signalement>

    @Insert
    fun insert(signalement: Signalement)

    @Query("DELETE FROM signalements")
    fun deleteAll()
}