package com.example.control_nomina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QuincenaRecord::class, TurnoRecord::class], version = 2, exportSchema = false)
abstract class NominaDatabase : RoomDatabase() {
    abstract fun nominaDao(): NominaDao

    companion object {
        @Volatile
        private var Instance: NominaDatabase? = null

        fun getDatabase(context: Context): NominaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    NominaDatabase::class.java,
                    "nomina_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                .also { Instance = it }
            }
        }
    }
}