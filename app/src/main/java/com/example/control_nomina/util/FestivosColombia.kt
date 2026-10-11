package com.example.control_nomina.util

import java.util.Calendar

object FestivosColombia {
    // Días festivos oficiales en Colombia para 2026 (Ley Emiliani)
    private val festivos2026 = setOf(
        "2026-01-01", // Año Nuevo
        "2026-01-12", // Reyes Magos
        "2026-03-23", // San José
        "2026-04-02", // Jueves Santo
        "2026-04-03", // Viernes Santo
        "2026-05-01", // Día del Trabajo
        "2026-05-18", // Ascensión del Señor
        "2026-06-08", // Corpus Christi
        "2026-06-15", // Sagrado Corazón
        "2026-06-29", // San Pedro y San Pablo
        "2026-07-20", // Día de la Independencia
        "2026-08-07", // Batalla de Boyacá
        "2026-08-17", // La Asunción de la Virgen
        "2026-10-12", // Día de la Raza
        "2026-11-02", // Todos los Santos
        "2026-11-16", // Independencia de Cartagena
        "2026-12-08", // Inmaculada Concepción
        "2026-12-25"  // Navidad
    )

    fun esFestivoOficial(fechaStr: String): Boolean {
        return festivos2026.contains(fechaStr)
    }

    fun esDomingoOFestivo(fechaStr: String): Boolean {
        if (festivos2026.contains(fechaStr)) return true
        
        val parts = fechaStr.split("-")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: return false
            val m = parts[1].toIntOrNull() ?: return false
            val d = parts[2].toIntOrNull() ?: return false
            val cal = Calendar.getInstance()
            cal.set(y, m - 1, d)
            return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        }
        return false
    }
}
