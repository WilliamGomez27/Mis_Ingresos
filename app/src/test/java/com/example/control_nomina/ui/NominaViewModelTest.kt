package com.example.control_nomina.ui

import com.example.control_nomina.Constants
import com.example.control_nomina.data.FakeNominaDao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NominaViewModelTest {

    private lateinit var fakeDao: FakeNominaDao
    private lateinit var viewModel: NominaViewModel

    @Before
    fun setUp() {
        fakeDao = FakeNominaDao()
        viewModel = NominaViewModel(fakeDao)
    }

    @Test
    fun auxilioTransporte_seAplicaSiSalarioEsMenorOIgual2SMMLV() {
        val salarioMinimo = Constants.SALARIO_MINIMO_2026.toInt().toString()
        viewModel.updateSalarioBase(salarioMinimo)

        val state = viewModel.uiState.value
        assertTrue(state.recibeAuxilioTransporte)
        assertEquals(Constants.AUXILIO_TRANSPORTE_2026 / 2, state.totalAuxilioTransporte, 0.01)
    }

    @Test
    fun auxilioTransporte_noSeAplicaSiSalarioSupera2SMMLV() {
        val salarioAlto = (Constants.SALARIO_MINIMO_2026 * 2.5).toInt().toString()
        viewModel.updateSalarioBase(salarioAlto)

        val state = viewModel.uiState.value
        assertFalse(state.recibeAuxilioTransporte)
        assertEquals(0.0, state.totalAuxilioTransporte, 0.01)
    }

    @Test
    fun fondoSolidaridadPensional_seAplicaSiSalarioEsMayorOIgual4SMMLV() {
        val salario4SMMLV = (Constants.SALARIO_MINIMO_2026 * 4.5).toInt().toString()
        viewModel.updateSalarioBase(salario4SMMLV)

        val state = viewModel.uiState.value
        assertTrue(state.deduccionFsp > 0)
    }

    @Test
    fun calculoHorasExtraYRecargos_aplicaFactoresLegalesCorrectamente() {
        val salarioMinimo = "2100000" // $2.100.000 mensual -> $10.000 / hora con divisor 210h
        viewModel.updateSalarioBase(salarioMinimo)

        // 2 horas extra diurnas (+25% -> $12.500/h * 2 = $25.000)
        viewModel.updateHorasExtraDiurna("2")
        // 1 hora recargo nocturno (+35% -> $3.500/h * 1 = $3.500)
        viewModel.updateRecargoNocturno("1")

        val state = viewModel.uiState.value
        assertEquals(25000.0, state.totalHorasExtra, 1.0)
        assertEquals(35000.0, state.totalRecargos * 10, 1.0) // 3.500
    }
}
