package com.example.control_nomina.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNominaDao : NominaDao {
    private val quincenas = MutableStateFlow<List<QuincenaRecord>>(emptyList())
    private val turnos = MutableStateFlow<List<TurnoRecord>>(emptyList())

    override suspend fun insertQuincena(record: QuincenaRecord) {
        quincenas.value = quincenas.value.filterNot { it.id == record.id } + record
    }

    override suspend fun deleteQuincena(record: QuincenaRecord) {
        quincenas.value = quincenas.value.filterNot { it.id == record.id }
    }

    override fun getAllQuincenas(): Flow<List<QuincenaRecord>> = quincenas

    override suspend fun insertTurno(turno: TurnoRecord) {
        turnos.value = turnos.value + turno
    }

    override suspend fun deleteTurno(turno: TurnoRecord) {
        turnos.value = turnos.value.filterNot { it.id == turno.id }
    }

    override fun getTurnosPorQuincena(mesAnio: String, esPago15: Boolean): Flow<List<TurnoRecord>> {
        return turnos.map { list ->
            list.filter { it.mesAnio == mesAnio && it.esPago15 == esPago15 }
        }
    }
}
