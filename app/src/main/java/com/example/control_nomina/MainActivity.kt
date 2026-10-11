package com.example.control_nomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.control_nomina.data.NominaDatabase
import com.example.control_nomina.ui.NominaScreen
import com.example.control_nomina.ui.HistorialScreen
import com.example.control_nomina.ui.MallaTurnosScreen
import com.example.control_nomina.ui.NominaViewModel
import com.example.control_nomina.ui.theme.Control_nominaTheme

import android.content.Context
import android.os.Build

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        
        // Inicializar base de datos y preferencias
        val database = NominaDatabase.getDatabase(this)
        val dao = database.nominaDao()
        val prefs = getSharedPreferences("control_nomina_prefs", Context.MODE_PRIVATE)
        
        // Factory manual básico para pasar el DAO y SharedPreferences al ViewModel
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NominaViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return NominaViewModel(dao, prefs) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            Control_nominaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val viewModel: NominaViewModel = viewModel(factory = factory)
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "calculadora") {
                        composable("calculadora") {
                            NominaScreen(
                                viewModel = viewModel,
                                onNavigateToHistorial = { navController.navigate("historial") },
                                onNavigateToTurnos = { navController.navigate("turnos") }
                            )
                        }
                        composable("historial") {
                            HistorialScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("turnos") {
                            MallaTurnosScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}