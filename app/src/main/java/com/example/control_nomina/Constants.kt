package com.example.control_nomina

object Constants {
    // Valores Legales Colombia (Ley vigente para 2026)
    // SMLV decretado con aumento del 23% para 2026
    const val SALARIO_MINIMO_2026 = 1750905.0
    const val AUXILIO_TRANSPORTE_2026 = 249095.0 // Valor Mensual Oficial 2026
    
    const val HORAS_SEMANALES = 42

    // Multiplicadores Horas Extra y Recargos (Art. 168 CST Colombia)
    const val FACTOR_EXTRA_DIURNA = 1.25
    const val FACTOR_EXTRA_NOCTURNA = 1.75
    const val FACTOR_EXTRA_DIURNA_DOM_FEST = 2.00 // 1.25 + 0.75
    const val FACTOR_EXTRA_NOCTURNA_DOM_FEST = 2.50 // 1.75 + 0.75
    const val FACTOR_RECARGO_NOCTURNO = 0.35
    const val FACTOR_RECARGO_DOM_FEST = 0.75
    const val FACTOR_RECARGO_NOCTURNO_DOM_FEST = 1.10 // 0.35 + 0.75

    // Deducciones de Ley
    const val PORCENTAJE_SALUD = 0.04
    const val PORCENTAJE_PENSION = 0.04
}