package com.example.control_nomina.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NominaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuincena(record: QuincenaRecord)

    @Delete
    suspend fun deleteQuincena(record: QuincenaRecord)

    @Query("SELECT * FROM historial_quincenas ORDER BY id DESC")
    fun getAllQuincenas(): Flow<List<QuincenaRecord>>

    // --- Turnos ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: TurnoRecord)

    @Delete
    suspend fun deleteTurno(turno: TurnoRecord)

    @Query("SELECT * FROM turnos_quincena WHERE mesAnio = :mesAnio AND esPago15 = :esPago15 ORDER BY fecha ASC")
    fun getTurnosPorQuincena(mesAnio: String, esPago15: Boolean): Flow<List<TurnoRecord>>
}