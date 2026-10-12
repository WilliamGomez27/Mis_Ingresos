package com.example.control_nomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
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
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Calculadora") },
                                    label = { Text("Calculadora") },
                                    selected = currentDestination?.hierarchy?.any { it.route == "calculadora" } == true,
                                    onClick = {
                                        navController.navigate("calculadora") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Turnos") },
                                    label = { Text("Turnos") },
                                    selected = currentDestination?.hierarchy?.any { it.route == "turnos" } == true,
                                    onClick = {
                                        navController.navigate("turnos") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                                    label = { Text("Historial") },
                                    selected = currentDestination?.hierarchy?.any { it.route == "historial" } == true,
                                    onClick = {
                                        navController.navigate("historial") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                                    label = { Text("Ajustes") },
                                    selected = currentDestination?.hierarchy?.any { it.route == "ajustes" } == true,
                                    onClick = {
                                        navController.navigate("ajustes") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController, 
                            startDestination = "calculadora",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("calculadora") {
                                NominaScreen(viewModel = viewModel)
                            }
                            composable("historial") {
                                HistorialScreen(viewModel = viewModel)
                            }
                            composable("turnos") {
                                MallaTurnosScreen(viewModel = viewModel)
                            }
                            composable("ajustes") {
                                com.example.control_nomina.ui.AjustesScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}