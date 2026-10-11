package com.example.control_nomina.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FestivosColombiaTest {

    @Test
    fun esFestivoOficial_detectaFestivosColombia2026() {
        // Año Nuevo
        assertTrue(FestivosColombia.esFestivoOficial("2026-01-01"))
        // Día del Trabajo
        assertTrue(FestivosColombia.esFestivoOficial("2026-05-01"))
        // Batalla de Boyacá
        assertTrue(FestivosColombia.esFestivoOficial("2026-08-07"))
        // Día de la Independencia
        assertTrue(FestivosColombia.esFestivoOficial("2026-07-20"))
        // Navidad
        assertTrue(FestivosColombia.esFestivoOficial("2026-12-25"))
    }

    @Test
    fun esFestivoOficial_retornaFalseParaDiasOrdinarios() {
        // Un martes normal
        assertFalse(FestivosColombia.esFestivoOficial("2026-02-10"))
        // Un miércoles normal
        assertFalse(FestivosColombia.esFestivoOficial("2026-07-15"))
    }

    @Test
    fun esDomingoOFestivo_detectaDomingosYFestivos() {
        // Domingo 5 de Julio de 2026
        assertTrue(FestivosColombia.esDomingoOFestivo("2026-07-05"))
        // Festivo Oficial (Viernes 1 de Mayo de 2026)
        assertTrue(FestivosColombia.esDomingoOFestivo("2026-05-01"))
        // Lunes ordinario (13 de Julio de 2026)
        assertFalse(FestivosColombia.esDomingoOFestivo("2026-07-13"))
    }
}
