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

    private val shiftRegexSafe = Regex("""(\d{1,2}:\d{2})\s*-\s*(\d{1,2})(?::(\d{2}))?""")
    private val dayRegex = Regex("""^\b([1-9]|[12][0-9]|3[01])\b$""")

    fun parseCalendarImage(ocrText: Text, yearMonthPrefix: String): List<ParsedShift> {
        val dayElements = mutableListOf<DayElement>()
        val shiftElements = mutableListOf<ShiftElement>()

        try {
            // 1. Recolectar números de día y turnos de forma liviana y 100% segura
            for (block in ocrText.textBlocks) {
                for (line in block.lines) {
                    val text = line.text.trim()
                    val box = line.boundingBox ?: Rect(0, 0, 0, 0)

                    // Verificar número de día
                    val dayMatch = dayRegex.find(text)
                    if (dayMatch != null) {
                        val dayNum = dayMatch.groupValues[1].toIntOrNull()
                        if (dayNum != null && dayNum in 1..31) {
                            dayElements.add(DayElement(dayNum, box))
                            continue
                        }
                    }

                    // Verificar turno (ej: "20:30-14" o "19:30-14:00")
                    val shiftMatch = shiftRegexSafe.find(text)
                    if (shiftMatch != null) {
                        val ent = shiftMatch.groupValues[1]
                        val sHour = shiftMatch.groupValues[2].padStart(2, '0')
                        val sMin = if (shiftMatch.groupValues.size > 3 && shiftMatch.groupValues[3].isNotEmpty()) {
                            shiftMatch.groupValues[3].padStart(2, '0')
                        } else {
                            "00"
                        }
                        val sal = "$sHour:$sMin"
                        shiftElements.add(ShiftElement(ent, sal, box))
                    }
                }
            }

            val result = mutableListOf<ParsedShift>()

            // 2. Emparejamiento simple por cercanía
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

            return result.distinctBy { it.day }.sortedBy { it.day }
        } catch (e: Exception) {
            // En caso de cualquier imprevisto de formato, retornar lista vacía de forma limpia
            return emptyList()
        }
    }

    private fun findClosestDayForShift(shift: ShiftElement, days: List<DayElement>): DayElement? {
        var closest: DayElement? = null
        var minDistance = Double.MAX_VALUE

        for (day in days) {
            val dx = (shift.box.centerX() - day.box.centerX()).toDouble()
            val dy = (shift.box.centerY() - day.box.centerY()).toDouble()
            
            if (dy < -100) continue

            val distance = Math.hypot(dx, dy)
            if (distance < minDistance && distance < 500) {
                minDistance = distance
                closest = day
            }
        }
        return closest
    }

    private data class DayElement(val day: Int, val box: Rect)
    private data class ShiftElement(val entrada: String, val salida: String, val box: Rect)
}
