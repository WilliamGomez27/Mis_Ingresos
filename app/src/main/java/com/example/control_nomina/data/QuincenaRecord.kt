package com.example.control_nomina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_quincenas")
data class QuincenaRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mesAnio: String, // ej. "Octubre de 2026"
    val esPago15: Boolean,
    
    val salarioMensualBase: Double,
    val recibeAuxilioTransporte: Boolean,
    val otrasDeducciones: Double,
    
    // Ajustes Avanzados
    val bonificaciones: Double = 0.0,
    val configDuracionTurno: Double = 9.15,
    val configDescansoMinutos: Int = 30,
    val configInicioNocturno: Int = 20,
    
    val horasExtraDiurna: Int,
    val horasExtraNocturna: Int,
    val horasExtraDiurnaDomFest: Int,
    val horasExtraNocturnaDomFest: Int,
    
    val recargoNocturno: Int,
    val recargoDomFest: Int,
    val recargoNocturnoDomFest: Int,
    
    // Resultados calculados guardados para referencia histórica
    val totalSalario: Double,
    val totalHorasExtra: Double,
    val totalRecargos: Double,
    val totalAuxilioTransporte: Double,
    val totalDeducciones: Double,
    val granTotalNeto: Double
)