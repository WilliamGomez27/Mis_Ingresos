package com.example.control_nomina.util

import android.graphics.Rect
import com.google.mlkit.vision.text.Text
import java.util.Locale

data class ParsedShift(
    val day: Int,
    val entrada: String, // "HH:mm"
    val salida: String,  // "HH:mm"
    val esFestivo: Boolean = false
)

object ImageScheduleParser {

    private val shiftRegex = Regex("""(\d{1,2}:\d{2})\s*-\s*(\d{1,2})(?::(\d{2}))?""")
    private val dayRegex = Regex("""^\b([1-9]|[12][0-9]|3[01])\b$""")

    fun parseCalendarImage(ocrText: Text, yearMonthPrefix: String): List<ParsedShift> {
        val dayElements = mutableListOf<DayElement>()
        val shiftElements = mutableListOf<ShiftElement>()

        // 1. Recolectar todos los números de día y turnos con sus coordenadas (BoundingBox)
        for (block in ocrText.textBlocks) {
            for (line in block.lines) {
                val text = line.text.trim()
                val box = line.boundingBox ?: continue

                // Verificar si es un número de día (1 al 31)
                val dayMatch = dayRegex.find(text)
                if (dayMatch != null) {
                    val dayNum = dayMatch.groupValues[1].toInt()
                    dayElements.add(DayElement(dayNum, box))
                    continue
                }

                // Verificar si es un turno (ej: "20:30-14" o "19:30-14:00")
                val shiftMatch = shiftRegex.find(text)
                if (shiftMatch != null) {
                    val ent = shiftMatch.groupValues[1]
                    val sHour = shiftMatch.groupValues[2].padStart(2, '0')
                    val sMin = shiftMatch.groupValues[3].ifEmpty { "00" }
                    val sal = "$sHour:$sMin"
                    shiftElements.add(ShiftElement(ent, sal, box))
                }
            }
        }

        val result = mutableListOf<ParsedShift>()

        // 2. Emparejar cada turno con su número de día más cercano
        for (shift in shiftElements) {
            val closestDay = findClosestDayForShift(shift, dayElements)
            if (closestDay != null) {
                val fechaStr = String.format(Locale.getDefault(), "%s-%02d", yearMonthPrefix, closestDay.day)
                val esFestivo = FestivosColombia.esDomingoOFestivo(fechaStr)
                
                result.add(
                    ParsedShift(
                        day = closestDay.day,
                        entrada = shift.entrada,
                        salida = shift.salida,
                        esFestivo = esFestivo
                    )
                )
            }
        }

        // Si la detección espacial no emparejó suficientes, intentar emparejamiento por secuencia
        if (result.isEmpty() && shiftElements.isNotEmpty()) {
            shiftElements.forEachIndexed { index, shift ->
                val dayNum = index + 1
                if (dayNum <= 31) {
                    val fechaStr = String.format(Locale.getDefault(), "%s-%02d", yearMonthPrefix, dayNum)
                    result.add(
                        ParsedShift(
                            day = dayNum,
                            entrada = shift.entrada,
                            salida = shift.salida,
                            esFestivo = FestivosColombia.esDomingoOFestivo(fechaStr)
                        )
                    )
                }
            }
        }

        return result.distinctBy { it.day }.sortedBy { it.day }
    }

    private fun findClosestDayForShift(shift: ShiftElement, days: List<DayElement>): DayElement? {
        var closest: DayElement? = null
        var minDistance = Double.MAX_VALUE

        for (day in days) {
            // Un número de día en calendario suele estar arriba/izquierda o dentro de la misma celda
            val dx = (shift.box.centerX() - day.box.centerX()).toDouble()
            val dy = (shift.box.centerY() - day.box.centerY()).toDouble()
            
            // Si el número está muy por debajo del turno, no pertenece a ese día
            if (dy < -100) continue

            val distance = Math.hypot(dx, dy)
            if (distance < minDistance && distance < 350) { // Distancia máxima en píxeles de celda
                minDistance = distance
                closest = day
            }
        }
        return closest
    }

    private data class DayElement(val day: Int, val box: Rect)
    private data class ShiftElement(val entrada: String, val salida: String, val box: Rect)
}
