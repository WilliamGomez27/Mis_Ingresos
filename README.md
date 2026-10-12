# 🇨🇴 Mis Ingresos - Control de Nómina y Turnos Colombia

**Mis Ingresos** es una aplicación nativa de Android desarrollada en Jetpack Compose que permite a los trabajadores en Colombia calcular, controlar y proyectar de manera exacta sus liquidaciones quincenales de nómina, horas extras, recargos nocturnos, festivos y deducciones de ley.

Incluye reconocimiento inteligente mediante **Inteligencia Artificial On-Device (Google ML Kit OCR)** para importar la malla de turnos directamente desde una foto o captura de pantalla.

---

## 🚀 Características Principales

* **Cálculo de Nómina Ajustado a la Ley Colombiana:**
  * **Jornada de 42 Horas Semanales (Ley 2101 de 2021):** Divisor exacto de 210 horas mensuales para el cálculo preciso del valor de la hora ordinaria.
  * **Auxilio de Transporte Automático:** Aplicación automática e intocable si el salario es `<= 2 SMMLV` ($124.548 / 2 por quincena).
  * **Fondo de Solidaridad Pensional (FSP):** Deducción automática del 1% para salarios `>= 4 SMMLV`.
  * **Deducciones de Ley:** Salud (4%) y Pensión (4%) calculadas sobre la Base de Cotización.
  * **Quincena Vencida:** Lógica legal colombiana donde las horas extras y recargos trabajados en la quincena se proyectan y liquidan en el pago subsiguiente.
* **Malla de Turnos con IA (OCR):**
  * Importación instantánea de turnos escaneando la imagen o captura del calendario de trabajo mediante **Google ML Kit Text Recognition**.
  * Detección automática de horas de entrada, salida y días libres.
  * Auto-cálculo de hora de salida al marcar hora de entrada según la duración configurada del turno (ej. +9.15h).
  * Botones de marcaje rápido en tiempo real (*"Entrada Ahora"* / *"Salida Ahora"*).
* **Configuración Dinámica:**
  * Bonificaciones ocasionales libres por quincena.
  * Selector dinámico del Divisor de Horas Semanales (210, 220, 230 o 240 horas).
  * Horario personalizable para el inicio de la noche (20:00, 21:00, etc.).
* **Calendario de Festivos de Colombia (Ley Emiliani):**
  * Base de datos integrada con los **18 días festivos oficiales de Colombia para 2026**.
  * Detección automática de festivos nacionales y dominicales con alertas visuales destacadas (*"🎉 ¡Día Festivo Oficial en Colombia!"*).
* **Gestión, Sincronización e Historial (Offline First):**
  * Persistencia local 100% privada en **Room Database** y `SharedPreferences` (los datos nunca salen del teléfono).
  * Aislamiento total de datos por quincena (las quincenas mantienen sus propias horas y novedades separadas de forma segura).
  * Herramienta de **Sincronización JSON (Exportar/Importar)** para respaldar, mover a otro teléfono o integrar con versiones web.
  * Pantalla de Historial de Pagos guardados.
  * Exportación y compartición de Tirilla de Pago en texto plano a través de WhatsApp, Correo o cualquier aplicación.
* **Diseño e Interfaz Moderna:**
  * Interfaz adaptativa **Edge-to-Edge** (Material Design 3).
  * **Barra de Navegación Inferior (Bottom Navigation)** con pestañas aisladas (*Calculadora, Turnos, Historial, Ajustes*).
  * Icono de aplicación profesional nativo en formato Adaptive Icon.
  * Animaciones de desvanecimiento e indicadores reactivos al interactuar con las tarjetas y modificar valores numéricos.
  * Tema corporativo financiero (Verde Esmeralda / Teal) con soporte para modo claro y modo oscuro.

---

## 🛠️ Arquitectura y Tecnologías

* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Arquitectura:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow (StateFlow)
* **Base de Datos:** Room Persistence Library (Versión 2 con soporte de migraciones)
* **Serialización:** GSON (para respaldos e importaciones).
* **Vision / IA:** Google ML Kit Text Recognition (Procesamiento 100% On-Device sin necesidad de internet)
* **Navegación:** Jetpack Navigation Compose
* **Asincronía:** Kotlin Coroutines & Flow

---

## 🧪 Estrategia de Pruebas Automatizadas

El proyecto cuenta con una suite completa de pruebas unitarias e instrumentadas:

* **`FestivosColombiaTest`:** Verifica la detección precisa de los 18 festivos de la Ley Emiliani y domingos en Colombia.
* **`NominaViewModelTest`:** Comprueba la exactitud matemática de los cálculos:
  * Umbral de Auxilio de Transporte (`<= 2 SMMLV`).
  * Umbral de FSP (`>= 4 SMMLV`).
  * Multiplicadores legales de horas extras (Diurna 1.25, Nocturna 1.75, Festiva 2.0, Nocturna Festiva 2.5) y recargos (Nocturno 0.35, Festivo 0.75, Nocturno Festivo 1.10).
  * Divisor de jornada de 42h (210h/mes).
* **`FakeNominaDao`:** Inyección de DAO simulado para pruebas unitarias ultra rápidas sin sqlite.

Para ejecutar las pruebas desde Android Studio o terminal:
```bash
./gradlew test
```