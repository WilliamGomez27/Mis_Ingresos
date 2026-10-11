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

    // Soporta variantes de OCR: "20:30-14", "20.30-14", "20;30-14", "20:30-14:00", "04:00-14", etc.
    private val shiftRegex = Regex("""(\d{1,2})[:.,;](\d{2})\s*[-~—–]?\s*(\d{1,2})(?:[:.,;](\d{2}))?""")
    private val dayRegex = Regex("""\b([1-9]|[12][0-9]|3[01])\b""")

    fun parseCalendarImage(ocrText: Text, yearMonthPrefix: String): List<ParsedShift> {
        val dayElements = mutableListOf<DayElement>()
        val shiftElements = mutableListOf<ShiftElement>()

        // 1. Analizar bloques y líneas
        for (block in ocrText.textBlocks) {
            for (line in block.lines) {
                val text = line.text.trim()
                val box = line.boundingBox ?: Rect(0, 0, 0, 0)

                // Buscar patrón de turno en esta línea
                val shiftMatch = shiftRegex.find(text)
                if (shiftMatch != null) {
                    val hEnt = shiftMatch.groupValues[1].padStart(2, '0')
                    val mEnt = shiftMatch.groupValues[2].padStart(2, '0')
                    val ent = "$hEnt:$mEnt"

                    val sHour = shiftMatch.groupValues[3].padStart(2, '0')
                    val sMin = shiftMatch.groupValues[4].ifEmpty { "00" }.padStart(2, '0')
                    val sal = "$sHour:$sMin"

                    shiftElements.add(ShiftElement(ent, sal, box))
                }

                // Buscar número(s) de día (1 al 31) en la línea
                dayRegex.findAll(text).forEach { dayMatch ->
                    val dayNum = dayMatch.groupValues[1].toIntOrNull()
                    if (dayNum != null && dayNum in 1..31) {
                        // Evitar tomar las horas (20, 30, 14, 04) como días si forman parte del turno
                        val isPartOfShiftTime = shiftMatch != null && shiftMatch.value.contains(dayMatch.value)
                        if (!isPartOfShiftTime) {
                            dayElements.add(DayElement(dayNum, box))
                        }
                    }
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

        // 3. Fallback: Si no se emparejaron por coordenadas, usar secuencia ordenada espacialmente
        if (result.size < shiftElements.size) {
            val sortedShifts = shiftElements.sortedWith(compareBy({ it.box.top / 150 }, { it.box.left }))
            val sortedDays = dayElements.distinctBy { it.day }.sortedBy { it.day }

            if (sortedDays.isNotEmpty()) {
                sortedShifts.forEach { shift ->
                    val closestDay = findClosestDayForShift(shift, sortedDays)
                    if (closestDay != null && result.none { it.day == closestDay.day }) {
                        val fechaStr = String.format(Locale.getDefault(), "%s-%02d", yearMonthPrefix, closestDay.day)
                        result.add(
                            ParsedShift(
                                day = closestDay.day,
                                entrada = shift.entrada,
                                salida = shift.salida,
                                esFestivo = FestivosColombia.esDomingoOFestivo(fechaStr)
                            )
                        )
                    }
                }
            }
        }

        return result.distinctBy { it.day }.sortedBy { it.day }
    }

    private fun findClosestDayForShift(shift: ShiftElement, days: List<DayElement>): DayElement? {
        var closest: DayElement? = null
        var minDistance = Double.MAX_VALUE

        for (day in days) {
            val dx = (shift.box.centerX() - day.box.centerX()).toDouble()
            val dy = (shift.box.centerY() - day.box.centerY()).toDouble()
            
            // Un número de día en el calendario siempre está arriba de la casilla del turno
            if (dy < -50) continue

            // Ponderar la alineación horizontal de la columna
            val weightedDistance = Math.hypot(dx * 1.8, dy)
            if (weightedDistance < minDistance) {
                minDistance = weightedDistance
                closest = day
            }
        }
        return closest
    }

    private data class DayElement(val day: Int, val box: Rect)
    private data class ShiftElement(val entrada: String, val salida: String, val box: Rect)
}
