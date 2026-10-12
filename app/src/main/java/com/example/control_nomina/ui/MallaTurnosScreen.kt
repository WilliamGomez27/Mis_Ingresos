package com.example.control_nomina.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.control_nomina.util.FestivosColombia
import com.example.control_nomina.util.ImageScheduleParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun calcularSalidaAuto(entradaStr: String, duracionHorasStr: String): String {
    val parts = entradaStr.split(":")
    if (parts.size != 2) return ""
    val h = parts[0].toIntOrNull() ?: return ""
    val m = parts[1].toIntOrNull() ?: return ""
    val dur = duracionHorasStr.toDoubleOrNull() ?: 9.15
    val minTotales = (h * 60 + m + (dur * 60).toInt()) % 1440
    val newH = minTotales / 60
    val newM = minTotales % 60
    return String.format(Locale.getDefault(), "%02d:%02d", newH, newM)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MallaTurnosScreen(viewModel: NominaViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var fecha by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var entrada by remember { mutableStateOf("") }
    var salida by remember { mutableStateOf("") }
    var esFestivo by remember { mutableStateOf(FestivosColombia.esDomingoOFestivo(fecha)) }

    var isProcessingImage by remember { mutableStateOf(false) }
    var ocrMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingImage = true
            ocrMessage = "Escaneando imagen de la malla..."
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        isProcessingImage = false
                        val yearMonthPrefix = when (state.mesAnio) {
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
                        val parsed = ImageScheduleParser.parseCalendarImage(visionText, yearMonthPrefix)
                        if (parsed.isNotEmpty()) {
                            viewModel.guardarMultiplesTurnos(parsed)
                            ocrMessage = "¡Éxito! Se importaron ${parsed.size} turnos automáticamente."
                        } else {
                            ocrMessage = "No se detectaron turnos claros en la imagen."
                        }
                    }
                    .addOnFailureListener { e ->
                        isProcessingImage = false
                        ocrMessage = "Error al escanear: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                isProcessingImage = false
                ocrMessage = "No se pudo cargar la imagen."
            }
        }
    }

    // Mostrar Diálogo Calendario
    val showDatePicker = {
        val parts = fecha.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            fecha = formattedDate
            if (FestivosColombia.esDomingoOFestivo(formattedDate)) {
                esFestivo = true
            }
        }, y, m, d).show()
    }

    // Mostrar Diálogo Reloj
    val showTimePicker = { isEntrada: Boolean ->
        val currentStr = if (isEntrada) entrada else salida
        val parts = currentStr.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val min = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            if (isEntrada) {
                entrada = formattedTime
                if (salida.isBlank()) {
                    salida = calcularSalidaAuto(formattedTime, state.configDuracionTurno)
                }
            } else {
                salida = formattedTime
            }
        }, h, min, true).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Malla de Turnos", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quincena: ${if (state.esPago15) "15" else "30"} de ${state.mesAnio}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Botón de Escanear Malla desde Imagen / Galería
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = !isProcessingImage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isProcessingImage) "Procesando Imagen..." else "Cargar Malla desde Imagen / Galería", fontWeight = FontWeight.Bold)
                    }
                    if (ocrMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ocrMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Formulario de Ingreso
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Registrar Jornada", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Campo Fecha con Calendario
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha de Turno") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Abrir Calendario", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker() }
                        )
                    }

                    // Banner llamativo si es festivo oficial en Colombia
                    if (FestivosColombia.esFestivoOficial(fecha)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE65100)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🎉 ¡Día Festivo Oficial en Colombia!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Campos Entrada y Salida con Reloj
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = entrada,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Entrada (HH:mm)") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { showTimePicker(true) }) {
                                        Icon(Icons.Default.Schedule, contentDescription = "Reloj Entrada", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showTimePicker(true) }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = salida,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Salida (HH:mm)") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { showTimePicker(false) }) {
                                        Icon(Icons.Default.Schedule, contentDescription = "Reloj Salida", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showTimePicker(false) }
                            )
                        }
                    }

                    // Botones Dinámicos "Entrada Ahora" y "Salida Ahora"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val now = sdf.format(Date())
                                entrada = now
                                salida = calcularSalidaAuto(now, state.configDuracionTurno)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entrada Ahora", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                salida = sdf.format(Date())
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salida Ahora", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = esFestivo, onCheckedChange = { esFestivo = it })
                        Text("Día festivo / dominical (Recargo)", style = MaterialTheme.typography.bodyMedium)
                    }

                    Button(
                        onClick = {
                            if (fecha.isNotBlank() && entrada.isNotBlank() && salida.isNotBlank()) {
                                viewModel.guardarTurno(fecha, entrada, salida, esFestivo)
                                esFestivo = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Guardar Jornada", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Jornadas Guardadas", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            
            if (state.turnosActuales.isEmpty()) {
                Text("No has registrado turnos en esta quincena.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.turnosActuales) { turno ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(turno.fecha, fontWeight = FontWeight.Bold)
                                    Text("${turno.horaEntrada} - ${turno.horaSalida}${if (turno.esFestivo) " (Festivo/Dominical)" else ""}", style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { viewModel.eliminarTurno(turno) }) {
                                    Text("X", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
