package com.example.control_nomina.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_nomina.Constants
import com.example.control_nomina.data.NominaDao
import com.example.control_nomina.data.QuincenaRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.Calendar
import android.content.SharedPreferences
import com.example.control_nomina.data.TurnoRecord

class NominaViewModel(
    private val nominaDao: NominaDao,
    private val prefs: SharedPreferences? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(NominaUiState())
    val uiState: StateFlow<NominaUiState> = _uiState.asStateFlow()
    
    // Lista de quincenas guardadas (Historial)
    val historialQuincenas = nominaDao.getAllQuincenas()
    
    private var turnosJob: Job? = null

    init {
        cargarEstadoGuardado()
        loadTurnos()
        recalcularNomina()
    }

    private fun cargarEstadoGuardado() {
        if (prefs == null) return
        val mesAnio = prefs.getString("mesAnio", "Julio de 2026") ?: "Julio de 2026"
        val esPago15 = prefs.getBoolean("esPago15", true)
        val salario = prefs.getString("salarioMensualBase", Constants.SALARIO_MINIMO_2026.toInt().toString()) ?: Constants.SALARIO_MINIMO_2026.toInt().toString()
        val bonos = prefs.getString("bonificaciones", "0") ?: "0"
        val otrasDed = prefs.getString("otrasDeducciones", "0") ?: "0"
        val duracionTurno = prefs.getString("configDuracionTurno", "9.15") ?: "9.15"
        val descansoMin = prefs.getString("configDescansoMinutos", "30") ?: "30"
        val inicioNocturno = prefs.getString("configInicioNocturno", "20") ?: "20"
        
        val heDiurna = prefs.getString("horasExtraDiurna", "") ?: ""
        val heNocturna = prefs.getString("horasExtraNocturna", "") ?: ""
        val heDiurnaFest = prefs.getString("horasExtraDiurnaDomFest", "") ?: ""
        val heNocturnaFest = prefs.getString("horasExtraNocturnaDomFest", "") ?: ""
        val recNocturno = prefs.getString("recargoNocturno", "") ?: ""
        val recFest = prefs.getString("recargoDomFest", "") ?: ""
        val recNoctFest = prefs.getString("recargoNocturnoDomFest", "") ?: ""

        _uiState.update {
            it.copy(
                mesAnio = mesAnio,
                esPago15 = esPago15,
                salarioMensualBase = salario,
                bonificaciones = bonos,
                otrasDeducciones = otrasDed,
                configDuracionTurno = duracionTurno,
                configDescansoMinutos = descansoMin,
                configInicioNocturno = inicioNocturno,
                horasExtraDiurna = heDiurna,
                horasExtraNocturna = heNocturna,
                horasExtraDiurnaDomFest = heDiurnaFest,
                horasExtraNocturnaDomFest = heNocturnaFest,
                recargoNocturno = recNocturno,
                recargoDomFest = recFest,
                recargoNocturnoDomFest = recNoctFest
            )
        }
    }

    private fun guardarEstadoActual() {
        if (prefs == null) return
        val state = _uiState.value
        prefs.edit()
            .putString("mesAnio", state.mesAnio)
            .putBoolean("esPago15", state.esPago15)
            .putString("salarioMensualBase", state.salarioMensualBase)
            .putString("bonificaciones", state.bonificaciones)
            .putString("otrasDeducciones", state.otrasDeducciones)
            .putString("configDuracionTurno", state.configDuracionTurno)
            .putString("configDescansoMinutos", state.configDescansoMinutos)
            .putString("configInicioNocturno", state.configInicioNocturno)
            .putString("horasExtraDiurna", state.horasExtraDiurna)
            .putString("horasExtraNocturna", state.horasExtraNocturna)
            .putString("horasExtraDiurnaDomFest", state.horasExtraDiurnaDomFest)
            .putString("horasExtraNocturnaDomFest", state.horasExtraNocturnaDomFest)
            .putString("recargoNocturno", state.recargoNocturno)
            .putString("recargoDomFest", state.recargoDomFest)
            .putString("recargoNocturnoDomFest", state.recargoNocturnoDomFest)
            .apply()
    }

    private fun loadTurnos() {
        turnosJob?.cancel()
        turnosJob = viewModelScope.launch {
            nominaDao.getTurnosPorQuincena(_uiState.value.mesAnio, _uiState.value.esPago15).collect { turnos ->
                _uiState.update { it.copy(turnosActuales = turnos) }
                recalcularHorasDeTurnos()
                recalcularNomina()
            }
        }
    }

    // --- Funciones para actualizar Inputs ---
    
    fun updateMesAnio(mes: String) {
        _uiState.update { it.copy(mesAnio = mes, isSaved = false) }
        guardarEstadoActual()
        loadTurnos()
    }
    
    fun setQuincena(esPago15: Boolean) {
        _uiState.update { it.copy(esPago15 = esPago15, isSaved = false) }
        guardarEstadoActual()
        loadTurnos()
    }

    fun updateSalarioBase(salario: String) {
        _uiState.update { it.copy(salarioMensualBase = salario, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateBonificaciones(bonos: String) {
        _uiState.update { it.copy(bonificaciones = bonos, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateConfigTurno(turno: String) {
        _uiState.update { it.copy(configDuracionTurno = turno, isSaved = false) }
        guardarEstadoActual()
        recalcularHorasDeTurnos()
        recalcularNomina()
    }

    fun updateConfigDescanso(descanso: String) {
        _uiState.update { it.copy(configDescansoMinutos = descanso, isSaved = false) }
        guardarEstadoActual()
        recalcularHorasDeTurnos()
        recalcularNomina()
    }

    fun updateConfigInicioNocturno(hora: String) {
        _uiState.update { it.copy(configInicioNocturno = hora, isSaved = false) }
        guardarEstadoActual()
        recalcularHorasDeTurnos()
        recalcularNomina()
    }

    fun setSelectedTab(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateOtrasDeducciones(deducciones: String) {
        _uiState.update { it.copy(otrasDeducciones = deducciones, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateHorasExtraDiurna(horas: String) {
        _uiState.update { it.copy(horasExtraDiurna = horas, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateHorasExtraNocturna(horas: String) {
        _uiState.update { it.copy(horasExtraNocturna = horas, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateHorasExtraDiurnaDomFest(horas: String) {
        _uiState.update { it.copy(horasExtraDiurnaDomFest = horas, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateHorasExtraNocturnaDomFest(horas: String) {
        _uiState.update { it.copy(horasExtraNocturnaDomFest = horas, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateRecargoNocturno(horas: String) {
        _uiState.update { it.copy(recargoNocturno = horas, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateRecargoDomFest(horas: String) {
        _uiState.update { it.copy(recargoDomFest = horas, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    fun updateRecargoNocturnoDomFest(horas: String) {
        _uiState.update { it.copy(recargoNocturnoDomFest = horas, isSaved = false) }
        guardarEstadoActual()
        recalcularNomina()
    }

    // --- Lógica de Clasificación de Turnos ---
    private fun recalcularHorasDeTurnos() {
        val state = _uiState.value
        val configTurno = state.configDuracionTurno.toDoubleOrNull() ?: 9.15
        val configDescanso = state.configDescansoMinutos.toIntOrNull() ?: 30
        val configNocturno = state.configInicioNocturno.toIntOrNull() ?: 20

        var heDiurna = 0.0
        var heNocturna = 0.0
        var heDiurnaFest = 0.0
        var heNocturnaFest = 0.0
        var recNocturno = 0.0
        var recFest = 0.0
        var recNoctFest = 0.0

        for (turno in state.turnosActuales) {
            val h = clasificarTurno(turno, configTurno, configDescanso, configNocturno)
            heDiurna += h["he_diurna"] ?: 0.0
            heNocturna += h["he_nocturna"] ?: 0.0
            heDiurnaFest += h["he_diurna_fest"] ?: 0.0
            heNocturnaFest += h["he_nocturna_fest"] ?: 0.0
            recNocturno += h["rec_nocturno"] ?: 0.0
            recFest += h["rec_festivo"] ?: 0.0
            recNoctFest += h["rec_noct_fest"] ?: 0.0
        }

        _uiState.update {
            it.copy(
                calcExtraDiurnaTurnos = heDiurna,
                calcExtraNocturnaTurnos = heNocturna,
                calcExtraDiurnaDomFestTurnos = heDiurnaFest,
                calcExtraNocturnaDomFestTurnos = heNocturnaFest,
                calcRecargoNocturnoTurnos = recNocturno,
                calcRecargoDomFestTurnos = recFest,
                calcRecargoNocturnoDomFestTurnos = recNoctFest
            )
        }
    }

    private fun clasificarTurno(
        turno: TurnoRecord,
        configTurno: Double,
        configDescanso: Int,
        configNocturno: Int
    ): Map<String, Double> {
        val fParts = turno.fecha.split("-")
        if (fParts.size != 3) return emptyMap()
        val y = fParts[0].toIntOrNull() ?: return emptyMap()
        val mo = fParts[1].toIntOrNull() ?: return emptyMap()
        val da = fParts[2].toIntOrNull() ?: return emptyMap()

        val eParts = turno.horaEntrada.split(":")
        val sParts = turno.horaSalida.split(":")
        if (eParts.size != 2 || sParts.size != 2) return emptyMap()

        val a = eParts[0].toInt() * 60 + eParts[1].toInt()
        var b = sParts[0].toInt() * 60 + sParts[1].toInt()
        if (b <= a) b += 1440 // Terminó al día siguiente

        val NS = configNocturno * 60
        val TM = (configTurno * 60).toInt()

        val h = mutableMapOf<String, Double>()
        var n = 0
        var od = 0.0

        val cal = java.util.Calendar.getInstance()

        for (t in a until b) {
            val diaOffset = t / 1440
            val mm = t % 1440

            cal.set(y, mo - 1, da + diaOffset)
            val esDomingo = cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY
            val esFestivo = esDomingo || (diaOffset == 0 && turno.esFestivo)
            val esNoche = mm >= NS || mm < 360
            val esExtra = n >= TM
            n++

            val key = if (esExtra) {
                if (esNoche) {
                    if (esFestivo) "he_nocturna_fest" else "he_nocturna"
                } else {
                    if (esFestivo) "he_diurna_fest" else "he_diurna"
                }
            } else {
                if (esNoche) {
                    if (esFestivo) "rec_noct_fest" else "rec_nocturno"
                } else {
                    if (esFestivo) "rec_festivo" else null
                }
            }

            if (key != null) {
                h[key] = (h[key] ?: 0.0) + (1.0 / 60.0)
            } else {
                od += (1.0 / 60.0)
            }
        }

        var r = configDescanso / 60.0
        val odMin = if (od < r) od else r
        r -= odMin

        val recKeys = listOf("rec_nocturno", "rec_festivo", "rec_noct_fest")
        for (k in recKeys) {
            if (r > 1e-9 && h.containsKey(k)) {
                val z = if ((h[k] ?: 0.0) < r) h[k]!! else r
                h[k] = (h[k] ?: 0.0) - z
                r -= z
            }
        }
        return h
    }

    // --- Lógica de Cálculo Central ---

    private fun recalcularNomina() {
        val state = _uiState.value
        
        val salarioMensual = state.salarioMensualBase.toDoubleOrNull() ?: 0.0
        val salarioQuincenal = salarioMensual / 2
        val valorHoraOrdinaria = salarioMensual / Constants.HORAS_MENSUALES
        val bonificaciones = state.bonificaciones.toDoubleOrNull() ?: 0.0
        
        // 1. Horas Extra (Afectan base cotizable) -> Suma manuales + calculadas de turnos
        var totalExtra = 0.0
        val horasExtraDiurnaTotal = (state.horasExtraDiurna.toDoubleOrNull() ?: 0.0) + state.calcExtraDiurnaTurnos
        val horasExtraNocturnaTotal = (state.horasExtraNocturna.toDoubleOrNull() ?: 0.0) + state.calcExtraNocturnaTurnos
        val horasExtraDiurnaDomFestTotal = (state.horasExtraDiurnaDomFest.toDoubleOrNull() ?: 0.0) + state.calcExtraDiurnaDomFestTurnos
        val horasExtraNocturnaDomFestTotal = (state.horasExtraNocturnaDomFest.toDoubleOrNull() ?: 0.0) + state.calcExtraNocturnaDomFestTurnos

        totalExtra += horasExtraDiurnaTotal * valorHoraOrdinaria * Constants.FACTOR_EXTRA_DIURNA
        totalExtra += horasExtraNocturnaTotal * valorHoraOrdinaria * Constants.FACTOR_EXTRA_NOCTURNA
        totalExtra += horasExtraDiurnaDomFestTotal * valorHoraOrdinaria * Constants.FACTOR_EXTRA_DIURNA_DOM_FEST
        totalExtra += horasExtraNocturnaDomFestTotal * valorHoraOrdinaria * Constants.FACTOR_EXTRA_NOCTURNA_DOM_FEST

        // 2. Recargos (Afectan base cotizable) -> Suma manuales + calculados de turnos
        var totalRecargos = 0.0
        val recargoNocturnoTotal = (state.recargoNocturno.toDoubleOrNull() ?: 0.0) + state.calcRecargoNocturnoTurnos
        val recargoDomFestTotal = (state.recargoDomFest.toDoubleOrNull() ?: 0.0) + state.calcRecargoDomFestTurnos
        val recargoNocturnoDomFestTotal = (state.recargoNocturnoDomFest.toDoubleOrNull() ?: 0.0) + state.calcRecargoNocturnoDomFestTurnos

        totalRecargos += recargoNocturnoTotal * valorHoraOrdinaria * Constants.FACTOR_RECARGO_NOCTURNO
        totalRecargos += recargoDomFestTotal * valorHoraOrdinaria * Constants.FACTOR_RECARGO_DOM_FEST
        totalRecargos += recargoNocturnoDomFestTotal * valorHoraOrdinaria * Constants.FACTOR_RECARGO_NOCTURNO_DOM_FEST

        // 3. Base para Seguridad Social (Solo incluye ingresos constitutivos de salario: Salario base + Extras + Recargos + Bonificaciones)
        val baseCotizacion = salarioQuincenal + totalExtra + totalRecargos + bonificaciones
        
        // 4. Deducciones de Ley (Salud 4%, Pensión 4%, Fondo de Solidaridad Pensional 1% si es >= 4 SMMLV)
        val deduccionSalud = baseCotizacion * Constants.PORCENTAJE_SALUD
        val deduccionPension = baseCotizacion * Constants.PORCENTAJE_PENSION
        val deduccionFsp = if (salarioMensual >= 4 * Constants.SALARIO_MINIMO_2026) baseCotizacion * 0.01 else 0.0
        val deduccionExtraUsuario = state.otrasDeducciones.toDoubleOrNull() ?: 0.0
        val totalDeducciones = deduccionSalud + deduccionPension + deduccionFsp + deduccionExtraUsuario

        // 5. Auxilio de Transporte (No hace parte de la base para aportes a seguridad social)
        // Por ley (Colombia), se recibe si el salario ordinario es <= 2 SMMLV.
        val aplicaAuxilioLegalmente = salarioMensual > 0 && salarioMensual <= (Constants.SALARIO_MINIMO_2026 * 2)
        val auxilioTransporte = if (aplicaAuxilioLegalmente) Constants.AUXILIO_TRANSPORTE_2026 / 2 else 0.0

        // 6. GRAN TOTAL NETO
        val granTotalNeto = baseCotizacion + auxilioTransporte - totalDeducciones

        // 7. Proyección Próximo Pago (Horas de esta quincena antes de deducciones)
        val proximoCobroProyectado = totalExtra + totalRecargos
        val fechaProximoCobro = if (state.esPago15) "30 de ${state.mesAnio}" else "15 del próximo mes"

        _uiState.update {
            it.copy(
                recibeAuxilioTransporte = aplicaAuxilioLegalmente,
                totalSalarioQuincenal = salarioQuincenal,
                totalHorasExtra = totalExtra,
                totalRecargos = totalRecargos,
                totalAuxilioTransporte = auxilioTransporte,
                totalDeducciones = totalDeducciones,
                deduccionSalud = deduccionSalud,
                deduccionPension = deduccionPension,
                deduccionFsp = deduccionFsp,
                granTotalNeto = granTotalNeto,
                proximoCobroProyectado = proximoCobroProyectado,
                fechaProximoCobro = fechaProximoCobro
            )
        }
    }

    // --- Base de Datos ---

    fun guardarQuincena() {
        val state = _uiState.value
        val record = QuincenaRecord(
            mesAnio = state.mesAnio,
            esPago15 = state.esPago15,
            salarioMensualBase = state.salarioMensualBase.toDoubleOrNull() ?: 0.0,
            recibeAuxilioTransporte = state.recibeAuxilioTransporte,
            otrasDeducciones = state.otrasDeducciones.toDoubleOrNull() ?: 0.0,
            
            bonificaciones = state.bonificaciones.toDoubleOrNull() ?: 0.0,
            configDuracionTurno = state.configDuracionTurno.toDoubleOrNull() ?: 9.15,
            configDescansoMinutos = state.configDescansoMinutos.toIntOrNull() ?: 30,
            configInicioNocturno = state.configInicioNocturno.toIntOrNull() ?: 20,
            
            horasExtraDiurna = state.horasExtraDiurna.toIntOrNull() ?: 0,
            horasExtraNocturna = state.horasExtraNocturna.toIntOrNull() ?: 0,
            horasExtraDiurnaDomFest = state.horasExtraDiurnaDomFest.toIntOrNull() ?: 0,
            horasExtraNocturnaDomFest = state.horasExtraNocturnaDomFest.toIntOrNull() ?: 0,
            
            recargoNocturno = state.recargoNocturno.toIntOrNull() ?: 0,
            recargoDomFest = state.recargoDomFest.toIntOrNull() ?: 0,
            recargoNocturnoDomFest = state.recargoNocturnoDomFest.toIntOrNull() ?: 0,
            
            totalSalario = state.totalSalarioQuincenal,
            totalHorasExtra = state.totalHorasExtra,
            totalRecargos = state.totalRecargos,
            totalAuxilioTransporte = state.totalAuxilioTransporte,
            totalDeducciones = state.totalDeducciones,
            granTotalNeto = state.granTotalNeto
        )

        viewModelScope.launch {
            nominaDao.insertQuincena(record)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun guardarTurno(fecha: String, entrada: String, salida: String, festivo: Boolean) {
        val state = _uiState.value
        val turno = TurnoRecord(
            mesAnio = state.mesAnio,
            esPago15 = state.esPago15,
            fecha = fecha,
            horaEntrada = entrada,
            horaSalida = salida,
            esFestivo = festivo
        )
        viewModelScope.launch {
            nominaDao.insertTurno(turno)
            // loadTurnos() is called automatically via flow?
            // Actually, getTurnosPorQuincena returns a Flow so the UI state will auto-update.
        }
    }

    fun eliminarTurno(turno: TurnoRecord) {
        viewModelScope.launch {
            nominaDao.deleteTurno(turno)
        }
    }

    fun guardarMultiplesTurnos(parsedShifts: List<com.example.control_nomina.util.ParsedShift>) {
        val state = _uiState.value
        val yearMonth = when (state.mesAnio) {
            "Enero de 2026" -> "2026-01"
            "Febrero de 2026" -> "2026-02"
            "Marzo de 2026" -> "2026-03"
            "Abril de 2026" -> "2026-04"
            "Mayo de 2026" -> "2026-05"
            "Junio de 2026" -> "2026-06"
            "Julio de 2026" -> "2026-07"
            "Agosto de 2026" -> "2026-08"
            "Septiembre de 2026" -> "2026-09"
            "Octubre de 2026" -> "2026-10"
            "Noviembre de 2026" -> "2026-11"
            "Diciembre de 2026" -> "2026-12"
            else -> "2026-07"
        }

        viewModelScope.launch {
            for (shift in parsedShifts) {
                val isP15 = shift.day <= 15
                if (isP15 == state.esPago15) {
                    val fechaFormatted = String.format(java.util.Locale.getDefault(), "%s-%02d", yearMonth, shift.day)
                    val turno = TurnoRecord(
                        mesAnio = state.mesAnio,
                        esPago15 = state.esPago15,
                        fecha = fechaFormatted,
                        horaEntrada = shift.entrada,
                        horaSalida = shift.salida,
                        esFestivo = shift.esFestivo
                    )
                    nominaDao.insertTurno(turno)
                }
            }
        }
    }
}