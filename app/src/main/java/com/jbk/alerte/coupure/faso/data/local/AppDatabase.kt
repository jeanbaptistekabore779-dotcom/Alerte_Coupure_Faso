#!/usr/bin/env kotlin

package com.jbk.alerte.coupure.faso.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jbk.alerte.coupure.faso.models.Signalement

@Database(entities = [Signalement::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class) // On ajoute nos convertisseurs ici
abstract class AppDatabase : RoomDatabase() {

    abstract fun signalementDao(): SignalementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alerte_coupure_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}