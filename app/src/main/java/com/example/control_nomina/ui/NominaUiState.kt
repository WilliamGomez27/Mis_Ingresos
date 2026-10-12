package com.example.control_nomina.ui

import com.example.control_nomina.Constants
import com.example.control_nomina.data.TurnoRecord

data class NominaUiState(
    // Inputs (Configuración)
    val mesAnio: String = "Octubre de 2026",
    val esPago15: Boolean = true,
    val salarioMensualBase: String = Constants.SALARIO_MINIMO_2026.toInt().toString(),
    val recibeAuxilioTransporte: Boolean = true,
    val otrasDeducciones: String = "0",
    
    // Ajustes Avanzados
    val bonificaciones: String = "0",
    val configDuracionTurno: String = "9.15",
    val configDescansoMinutos: String = "30",
    val configInicioNocturno: String = "20",
    val divisorHoras: String = "210", // 210 = 42h, 220 = 44h, 230 = 46h, 240 = 48h

    // Inputs (Horas manuales)
    val horasExtraDiurna: String = "",
    val horasExtraNocturna: String = "",
    val horasExtraDiurnaDomFest: String = "",
    val horasExtraNocturnaDomFest: String = "",
    val recargoNocturno: String = "",
    val recargoDomFest: String = "",
    val recargoNocturnoDomFest: String = "",

    // Outputs de turnos quincena actual (se proyectan para el próximo pago)
    val turnosActuales: List<TurnoRecord> = emptyList(),
    val calcExtraDiurnaTurnos: Double = 0.0,
    val calcExtraNocturnaTurnos: Double = 0.0,
    val calcExtraDiurnaDomFestTurnos: Double = 0.0,
    val calcExtraNocturnaDomFestTurnos: Double = 0.0,
    val calcRecargoNocturnoTurnos: Double = 0.0,
    val calcRecargoDomFestTurnos: Double = 0.0,
    val calcRecargoNocturnoDomFestTurnos: Double = 0.0,

    // Outputs de turnos quincena anterior (se cobran en este pago)
    val turnosQuincenaAnterior: List<TurnoRecord> = emptyList(),
    val quincenaAnteriorNombre: String = "",
    val cobroExtraDiurnaTurnos: Double = 0.0,
    val cobroExtraNocturnaTurnos: Double = 0.0,
    val cobroExtraDiurnaDomFestTurnos: Double = 0.0,
    val cobroExtraNocturnaDomFestTurnos: Double = 0.0,
    val cobroRecargoNocturnoTurnos: Double = 0.0,
    val cobroRecargoDomFestTurnos: Double = 0.0,
    val cobroRecargoNocturnoDomFestTurnos: Double = 0.0,

    // Outputs (Resultados calculados)
    val totalSalarioQuincenal: Double = 0.0,
    val totalHorasExtra: Double = 0.0,
    val totalRecargos: Double = 0.0,
    val totalAuxilioTransporte: Double = 0.0,
    val totalDeducciones: Double = 0.0,
    val deduccionSalud: Double = 0.0,
    val deduccionPension: Double = 0.0,
    val deduccionFsp: Double = 0.0,
    val granTotalNeto: Double = 0.0,
    
    // Proyección próximo cobro
    val proximoCobroProyectado: Double = 0.0,
    val fechaProximoCobro: String = "",
    
    // UI Helpers
    val selectedTab: String = "base", // "base", "extra", "recargo", "aux", "ded"
    val isSaved: Boolean = false
)