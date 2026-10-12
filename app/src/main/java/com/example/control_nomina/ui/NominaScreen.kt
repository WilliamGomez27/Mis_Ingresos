package com.example.control_nomina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.example.control_nomina.Constants
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NominaScreen(viewModel: NominaViewModel, onNavigateToHistorial: () -> Unit, onNavigateToTurnos: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formatoMoneda.maximumFractionDigits = 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Ingresos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        },
        floatingActionButton = {
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                FloatingActionButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Malla de Turnos") },
                        onClick = {
                            menuExpanded = false
                            onNavigateToTurnos()
                        },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Historial de Pagos") },
                        onClick = {
                            menuExpanded = false
                            onNavigateToHistorial()
                        },
                        leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderSelector(state, viewModel)
            TotalNetoCard(state, viewModel)
            
            // Banners informativo de próximo cobro
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recargo del ${if (state.esPago15) "1 al 15" else "16 al 30"}, se liquida el ${state.fechaProximoCobro} (horas extras de esta quincena): ${formatoMoneda.format(state.proximoCobroProyectado)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }

            DesgloseGrid(state, viewModel)
            InfoDetalleCard(state)
            AjustesForm(state, viewModel)
            HorasTrabajadasForm(state, viewModel)
            
            Spacer(modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun HeaderSelector(state: NominaUiState, viewModel: NominaViewModel) {
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    val meses = remember {
        listOf(
            "Enero de 2026", "Febrero de 2026", "Marzo de 2026", "Abril de 2026",
            "Mayo de 2026", "Junio de 2026", "Julio de 2026", "Agosto de 2026",
            "Septiembre de 2026", "Octubre de 2026", "Noviembre de 2026", "Diciembre de 2026"
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = state.mesAnio,
                onValueChange = {},
                readOnly = true,
                label = { Text("Mes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { monthDropdownExpanded = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar Mes")
                    }
                }
            )
            // Area clickeable sobre la caja
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { monthDropdownExpanded = true }
            )
            DropdownMenu(
                expanded = monthDropdownExpanded,
                onDismissRequest = { monthDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                meses.forEach { mes ->
                    DropdownMenuItem(
                        text = { Text(mes) },
                        onClick = {
                            viewModel.updateMesAnio(mes)
                            monthDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.setQuincena(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.esPago15) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (state.esPago15) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("Pago 15")
        }
        Button(
            onClick = { viewModel.setQuincena(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!state.esPago15) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (!state.esPago15) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("Pago 30")
        }
    }
}

@Composable
fun TotalNetoCard(state: NominaUiState, viewModel: NominaViewModel) {
    val context = LocalContext.current
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formatoMoneda.maximumFractionDigits = 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Neto del pago del ${if (state.esPago15) "15" else "30"} de ${state.mesAnio.split(" ").firstOrNull() ?: "este mes"}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = formatoMoneda.format(state.granTotalNeto),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = { viewModel.guardarQuincena() },
                    enabled = !state.isSaved
                ) {
                    Text(if (state.isSaved) "Guardado" else "Guardar")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = {
                    val textToShare = """
                        Nómina ${if (state.esPago15) "15" else "30"} de ${state.mesAnio}
                        Salario Base: ${formatoMoneda.format(state.totalSalarioQuincenal)}
                        Horas Extra: ${formatoMoneda.format(state.totalHorasExtra)}
                        Recargos: ${formatoMoneda.format(state.totalRecargos)}
                        Aux. Transporte: ${formatoMoneda.format(state.totalAuxilioTransporte)}
                        Deducciones: -${formatoMoneda.format(state.totalDeducciones)}
                        ------------------------
                        Total Neto: ${formatoMoneda.format(state.granTotalNeto)}
                    """.trimIndent()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Tirilla de Pago")
                        putExtra(Intent.EXTRA_TEXT, textToShare)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir nómina"))
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Compartir tirilla", color = Color(0xFFA1E085))
            }
        }
    }
}

@Composable
fun DesgloseGrid(state: NominaUiState, viewModel: NominaViewModel) {
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formatoMoneda.maximumFractionDigits = 0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResumenCard(
                titulo = "Salario quincenal",
                valor = formatoMoneda.format(state.totalSalarioQuincenal),
                isSelected = state.selectedTab == "base",
                onClick = { viewModel.setSelectedTab("base") },
                modifier = Modifier.weight(1f)
            )
            ResumenCard(
                titulo = "Horas extra",
                valor = formatoMoneda.format(state.totalHorasExtra),
                isSelected = state.selectedTab == "extra",
                onClick = { viewModel.setSelectedTab("extra") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResumenCard(
                titulo = "Recargos",
                valor = formatoMoneda.format(state.totalRecargos),
                isSelected = state.selectedTab == "recargo",
                onClick = { viewModel.setSelectedTab("recargo") },
                modifier = Modifier.weight(1f)
            )
            ResumenCard(
                titulo = "Aux. transporte",
                valor = formatoMoneda.format(state.totalAuxilioTransporte),
                isSelected = state.selectedTab == "aux",
                onClick = { viewModel.setSelectedTab("aux") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResumenCard(
                titulo = "Deducciones de Ley",
                valor = "-${formatoMoneda.format(state.totalDeducciones)}",
                isSelected = state.selectedTab == "ded",
                onClick = { viewModel.setSelectedTab("ded") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ResumenCard(
    titulo: String,
    valor: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderStroke = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoDetalleCard(state: NominaUiState) {
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formatoMoneda.maximumFractionDigits = 0
    val salMensual = state.salarioMensualBase.toDoubleOrNull() ?: 0.0
    val divisorHoras = state.divisorHoras.toDoubleOrNull() ?: 210.0
    val valorHora = salMensual / divisorHoras

    val (titulo, descripcion) = when (state.selectedTab) {
        "base" -> Pair(
            "Salario quincenal",
            "Mitad de tu salario mensual (${formatoMoneda.format(salMensual)}), pagada cada 15 y 30.\n\n" +
            "• Valor de tu hora ordinaria: ${formatoMoneda.format(valorHora)} (salario ÷ ${state.divisorHoras} h, jornada de ${if (state.divisorHoras == "210") "42" else "48"} h).\n" +
            "• Salario mínimo 2026: ${formatoMoneda.format(Constants.SALARIO_MINIMO_2026)}" +
            if ((state.bonificaciones.toDoubleOrNull() ?: 0.0) > 0) "\n• Bonificación salarial: ${formatoMoneda.format(state.bonificaciones.toDoubleOrNull() ?: 0.0)}" else ""
        )
        "extra" -> Pair(
            "Horas extra",
            "Quincena Vencida: Hoy se liquidan las horas trabajadas del ${state.quincenaAnteriorNombre}.\n" +
            "• Diurna (+25%): ${formatoMoneda.format(((state.horasExtraDiurna.toDoubleOrNull() ?: 0.0) + state.cobroExtraDiurnaTurnos) * valorHora * Constants.FACTOR_EXTRA_DIURNA)}\n" +
            "• Nocturna (+75%): ${formatoMoneda.format(((state.horasExtraNocturna.toDoubleOrNull() ?: 0.0) + state.cobroExtraNocturnaTurnos) * valorHora * Constants.FACTOR_EXTRA_NOCTURNA)}\n" +
            "• Diurna Dom./Fest. (+100%): ${formatoMoneda.format(((state.horasExtraDiurnaDomFest.toDoubleOrNull() ?: 0.0) + state.cobroExtraDiurnaDomFestTurnos) * valorHora * Constants.FACTOR_EXTRA_DIURNA_DOM_FEST)}\n" +
            "• Nocturna Dom./Fest. (+150%): ${formatoMoneda.format(((state.horasExtraNocturnaDomFest.toDoubleOrNull() ?: 0.0) + state.cobroExtraNocturnaDomFestTurnos) * valorHora * Constants.FACTOR_EXTRA_NOCTURNA_DOM_FEST)}"
        )
        "recargo" -> Pair(
            "Recargos",
            "Quincena Vencida: Hoy se liquidan los recargos del ${state.quincenaAnteriorNombre}.\n" +
            "• Nocturno (+35%): ${formatoMoneda.format(((state.recargoNocturno.toDoubleOrNull() ?: 0.0) + state.cobroRecargoNocturnoTurnos) * valorHora * Constants.FACTOR_RECARGO_NOCTURNO)}\n" +
            "• Dom./Festivo (+75%): ${formatoMoneda.format(((state.recargoDomFest.toDoubleOrNull() ?: 0.0) + state.cobroRecargoDomFestTurnos) * valorHora * Constants.FACTOR_RECARGO_DOM_FEST)}\n" +
            "• Nocturno Dom./Fest. (+110%): ${formatoMoneda.format(((state.recargoNocturnoDomFest.toDoubleOrNull() ?: 0.0) + state.cobroRecargoNocturnoDomFestTurnos) * valorHora * Constants.FACTOR_RECARGO_NOCTURNO_DOM_FEST)}"
        )
        "aux" -> Pair(
            "Auxilio de transporte",
            if (salMensual > 2 * Constants.SALARIO_MINIMO_2026)
                "No aplica: Tu salario supera 2 salarios mínimos legalmente."
            else
                "Se paga la mitad (${formatoMoneda.format(Constants.AUXILIO_TRANSPORTE_2026 / 2)}) en cada quincena. No entra en la base de cotización de salud y pensión."
        )
        else -> Pair(
            "Deducciones de Ley",
            "• Salud (4%): ${formatoMoneda.format(state.deduccionSalud)}\n" +
            "• Pensión (4%): ${formatoMoneda.format(state.deduccionPension)}" +
            (if (state.deduccionFsp > 0) "\n• Fondo Solidaridad Pensional (1%): ${formatoMoneda.format(state.deduccionFsp)}" else "") +
            (if ((state.otrasDeducciones.toDoubleOrNull() ?: 0.0) > 0) "\n• Otras deducciones: ${formatoMoneda.format(state.otrasDeducciones.toDoubleOrNull() ?: 0.0)}" else "")
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AjustesForm(state: NominaUiState, viewModel: NominaViewModel) {
    val focusManager = LocalFocusManager.current
    Column {
        Text("Configuración de Ingresos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = state.salarioMensualBase,
                onValueChange = { viewModel.updateSalarioBase(it) },
                label = { Text("Salario mensual ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.bonificaciones,
                onValueChange = { viewModel.updateBonificaciones(it) },
                label = { Text("Bonificaciones ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = state.otrasDeducciones,
                onValueChange = { viewModel.updateOtrasDeducciones(it) },
                label = { Text("Otras deducciones ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                modifier = Modifier.weight(1f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.recibeAuxilioTransporte,
                onCheckedChange = null, // Calculado automáticamente por ley
                enabled = false
            )
            Text("Recibe auxilio de transporte (Calculado: <= 2 SMMLV)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Ajustes de Jornada", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = state.configDuracionTurno,
                onValueChange = { viewModel.updateConfigTurno(it) },
                label = { Text("Horas x Turno (ej. 9.15)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.configDescansoMinutos,
                onValueChange = { viewModel.updateConfigDescanso(it) },
                label = { Text("Descanso (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = state.configInicioNocturno,
                onValueChange = { viewModel.updateConfigInicioNocturno(it) },
                label = { Text("Hora inicio noche (0-23)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.divisorHoras,
                onValueChange = { viewModel.updateDivisorHoras(it) },
                label = { Text("Divisor (ej. 210)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HorasTrabajadasForm(state: NominaUiState, viewModel: NominaViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Horas trabajadas en esta quincena", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(label = "Extra diurna", value = state.horasExtraDiurna, onValueChange = viewModel::updateHorasExtraDiurna, modifier = Modifier.weight(1f))
            NumberField(label = "Extra nocturna", value = state.horasExtraNocturna, onValueChange = viewModel::updateHorasExtraNocturna, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(label = "Extra diurna dom./fest", value = state.horasExtraDiurnaDomFest, onValueChange = viewModel::updateHorasExtraDiurnaDomFest, modifier = Modifier.weight(1f))
            NumberField(label = "Extra nocturna dom./fest", value = state.horasExtraNocturnaDomFest, onValueChange = viewModel::updateHorasExtraNocturnaDomFest, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(label = "Recargo nocturno", value = state.recargoNocturno, onValueChange = viewModel::updateRecargoNocturno, modifier = Modifier.weight(1f))
            NumberField(label = "Recargo dom./fest", value = state.recargoDomFest, onValueChange = viewModel::updateRecargoDomFest, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(label = "Recargo nocturno dom./fest", value = state.recargoNocturnoDomFest, onValueChange = viewModel::updateRecargoNocturnoDomFest, modifier = Modifier.weight(0.5f))
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
        modifier = modifier
    )
}
