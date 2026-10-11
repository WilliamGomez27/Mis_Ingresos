# Informe Completo de Fallas y Correcciones

## 1. Falla de Compilación por Google Services
**Descripción del Error:**
El proyecto fallaba en la tarea de Gradle `processDebugGoogleServices` al intentar compilar, lanzando el error `File google-services.json is missing`. El plugin de servicios de Google requiere de este archivo de configuración de Firebase para poder procesar la aplicación, y su ausencia impedía que el proyecto se construyera o pudiera ser probado.

**Corrección Aplicada:**
Se creó un archivo `google-services.json` temporal (dummy) en la ruta `app/google-services.json` con la configuración básica (`project_info`, `client_info` apuntando al `package_name` actual `com.example.control_nomina`), permitiendo que el compilador termine la tarea con éxito. *Nota: Si vas a usar funciones reales de Firebase, deberás reemplazar este archivo con el original descargado desde la Consola de Firebase.*

## 2. Fallas Críticas en las Pruebas Unitarias (NominaViewModelTest)
Al ejecutar los tests del entorno local, el archivo de pruebas presentaba múltiples errores fatales:

### Falla A: Coroutines Main Dispatcher
**Descripción del Error:**
`java.lang.IllegalStateException: Module with the Main dispatcher had failed to initialize.`
Las pruebas fallaban inmediatamente en el `@Before` porque el `NominaViewModel` instanciaba y llamaba a una corrutina utilizando `viewModelScope.launch` (que depende del `Dispatchers.Main` nativo de Android). En el entorno local de pruebas (JVM puro), dicho dispatcher no existe.

**Corrección Aplicada:**
1. Se agregó la dependencia necesaria `kotlinx-coroutines-test` en los archivos `libs.versions.toml` y `build.gradle.kts`.
2. Se sincronizó Gradle para obtener las herramientas de testing de Kotlin.
3. En el archivo `NominaViewModelTest.kt`, se configuró un `UnconfinedTestDispatcher()` sobreescribiendo el hilo principal antes de inicializar la clase usando `Dispatchers.setMain(testDispatcher)` en el `@Before`, y limpiándolo en el `@After` con `Dispatchers.resetMain()`.

### Falla B: Looper de Android no mockeado
**Descripción del Error:**
`java.lang.RuntimeException: Method getMainLooper in android.os.Looper not mocked.`
Acompañando a las corrutinas, algunos componentes de ViewModel intentaban acceder a APIs exclusivas del SDK de Android, las cuales no están disponibles nativamente durante pruebas locales en JVM.

**Corrección Aplicada:**
Se habilitó la bandera `testOptions { unitTests.isReturnDefaultValues = true }` en el archivo `app/build.gradle.kts`. Esto instruye al entorno de testeo para que retorne valores por defecto (como `null` o `0`) en lugar de arrojar excepciones cuando se tocan componentes nativos sin simular, estabilizando el entorno para enfocarse puramente en la lógica del ViewModel.

## Resultado Final
Tras aplicar las soluciones, el estado del proyecto es:
* **Compilación (Build):** 100% Exitosa sin bloqueos.
* **Pruebas Automatizadas (Unit Tests):** Las 8 pruebas unitarias corren y pasan de manera exitosa, verificando correctamente los cálculos legales para el Auxilio de Transporte, FSP y horas extra de Colombia.

Todos los problemas críticos detectados han sido abordados y resueltos correctamente.