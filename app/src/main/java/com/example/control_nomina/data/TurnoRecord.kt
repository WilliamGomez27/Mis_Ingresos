package com.example.control_nomina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos_quincena")
data class TurnoRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mesAnio: String,
    val esPago15: Boolean,
    val fecha: String, // "YYYY-MM-DD"
    val horaEntrada: String, // "HH:mm"
    val horaSalida: String, // "HH:mm"
    val esFestivo: Boolean
)
