@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)

package com.example.moveperu

/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  MovePerú — App de transporte privado (Lima)                             ║
 * ║  Arny Ruben Saravia Chavez                                               ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 *
 * PATRÓN: MVVM con flujo de datos unidireccional (UDF).
 *   model      → Viaje, Conductor, Pasajero, UbicacionTexto, TipoServicio,
 *                Calificacion, EstadoViaje
 *   repository → ViajeRepository, ConductorRepository, CalificacionRepository,
 *                AuthRepository (necesario para RF01/RF02, no estaba en la
 *                lista original pero el login lo requiere)
 *   viewmodel  → AuthViewModel, SolicitudViajeViewModel, HistorialViajeViewModel,
 *                ConductorViewModel (+ Inicio, Seguimiento, Calificacion,
 *                DetalleViaje y Perfil, que necesitan su propio estado)
 *   screens    → Splash, Login (ingreso y registro en una sola pantalla,
 *                porque solo pediste un AuthViewModel), Inicio, SolicitarViaje,
 *                Seguimiento, Historial, Calificacion, DetalleViaje, Perfil,
 *                ConductorDashboard, ConductorDetalle
 *
 * COBERTURA DE REQUERIMIENTOS
 *   RF01 Splash + Login ............ SplashScreen / LoginScreen
 *   RF02 Pasajero o conductor ...... Rol + selector en LoginScreen
 *   RF03 Mínimo 30 viajes .......... DatosSimulados.viajesIniciales() → 33
 *   RF04 Filtros ................... FiltrarHistorialUseCase + HistorialScreen
 *   RF05 Solicitar viaje ........... SolicitarViajeScreen
 *   RF06 Tarifa estimada ........... EstimarTarifaUseCase
 *   RF07 Conductor + seguimiento ... ConductorRepository.asignarConductor + SeguimientoScreen
 *   RF08 Calificación .............. CalificacionRepository + CalificacionScreen
 *   RF09 Viajes asignados .......... ConductorDashboardScreen
 *   RF10 Cambio de estado .......... ConductorViewModel.onAvanzarEstado
 *
 * Archivo único para pegar en MainActivity.kt. Cambia la línea `package`.
 */

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

// ══════════════════════════════════════════════════════════════════════════════
//  ACTIVITY
// ══════════════════════════════════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovePeruTheme {
                MovePeruApp()
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  TEMA Y MARCA
// ══════════════════════════════════════════════════════════════════════════════

private val Petroleo = Color(0xFF0B3B44)      // acantilado de la Costa Verde
private val PetroleoOsc = Color(0xFF062A31)
private val Turquesa = Color(0xFF1E7F8C)
private val AguaClara = Color(0xFFCFE5E7)
private val Senal = Color(0xFFF2B705)         // amarillo de señalización vial
private val SenalSuave = Color(0xFFFCEFC4)
private val Tinta = Color(0xFF12201F)
private val Neblina = Color(0xFFEDF1F0)       // la garúa limeña
private val Borde = Color(0xFFC3CFCE)

private val MoveColors = lightColorScheme(
    primary = Petroleo,
    onPrimary = Color.White,
    primaryContainer = AguaClara,
    onPrimaryContainer = PetroleoOsc,
    secondary = Senal,
    onSecondary = Tinta,
    secondaryContainer = SenalSuave,
    onSecondaryContainer = Color(0xFF473400),
    tertiary = Turquesa,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5EEF1),
    onTertiaryContainer = Color(0xFF07343A),
    background = Neblina,
    onBackground = Tinta,
    surface = Color.White,
    onSurface = Tinta,
    surfaceVariant = Color(0xFFE3EAE9),
    onSurfaceVariant = Color(0xFF475856),
    outline = Borde,
    outlineVariant = Color(0xFFDDE5E4),
    error = Color(0xFFB3261E)
)

@Composable
fun MovePeruTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MoveColors, content = content)
}

/**
 * La misma marca del icono de la app, dibujada con Canvas: punto de origen,
 * trazo de la ruta y pin de destino. Coordenadas sobre un lienzo de 108x108.
 */
@Composable
private fun MarcaMovePeru(tamano: Dp = 96.dp, colorHueco: Color = Petroleo) {
    Canvas(modifier = Modifier.size(tamano)) {
        val lado = size.minDimension
        fun p(x: Float, y: Float) = Offset(x / 108f * lado, y / 108f * lado)
        fun l(v: Float) = v / 108f * lado

        drawLine(
            color = Senal,
            start = p(38f, 76f),
            end = p(61f, 58f),
            strokeWidth = l(7f),
            cap = StrokeCap.Round
        )
        val punta = Path().apply {
            moveTo(p(55.6f, 43.9f).x, p(55.6f, 43.9f).y)
            lineTo(p(72.4f, 43.9f).x, p(72.4f, 43.9f).y)
            lineTo(p(64f, 62f).x, p(64f, 62f).y)
            close()
        }
        drawPath(punta, Senal)
        drawCircle(color = Senal, radius = l(13f), center = p(64f, 34f))
        drawCircle(color = colorHueco, radius = l(5.4f), center = p(64f, 34f))
        drawCircle(color = Color.White, radius = l(8f), center = p(38f, 76f))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MODEL
// ══════════════════════════════════════════════════════════════════════════════

enum class Rol(val etiqueta: String) {
    PASAJERO("Pasajero"),
    CONDUCTOR("Conductor")
}

enum class TipoServicio(
    val etiqueta: String,
    val tarifaBase: Double,
    val porKm: Double,
    val porMin: Double,
    val capacidad: String
) {
    ECONOMICO("Económico", 4.50, 1.10, 0.25, "4 pasajeros"),
    CONFORT("Confort", 6.50, 1.65, 0.35, "4 pasajeros"),
    VAN("Van", 9.00, 2.30, 0.45, "8 pasajeros")
}

enum class MetodoPago(val etiqueta: String) {
    EFECTIVO("Efectivo"),
    YAPE("Yape"),
    TARJETA("Tarjeta")
}

/** RF07 y RF10: los estados por los que pasa un viaje. */
enum class EstadoViaje(val etiqueta: String, val detalle: String) {
    SOLICITADO("Solicitado", "Buscando un conductor disponible"),
    ACEPTADO("Aceptado", "El conductor confirmó el viaje"),
    EN_CAMINO("En camino", "El conductor va al punto de recojo"),
    INICIADO("Iniciado", "Viaje en curso hacia el destino"),
    FINALIZADO("Finalizado", "Llegaste a tu destino"),
    CANCELADO("Cancelado", "El viaje fue cancelado");

    /** Siguiente estado del flujo, o null si el viaje ya terminó. */
    val siguiente: EstadoViaje?
        get() = when (this) {
            SOLICITADO -> ACEPTADO
            ACEPTADO -> EN_CAMINO
            EN_CAMINO -> INICIADO
            INICIADO -> FINALIZADO
            FINALIZADO, CANCELADO -> null
        }

    val activo: Boolean get() = this != FINALIZADO && this != CANCELADO

    companion object {
        val flujo = listOf(SOLICITADO, ACEPTADO, EN_CAMINO, INICIADO, FINALIZADO)
    }
}

enum class TipoLugar { CASA, TRABAJO, OTRO }

data class UbicacionTexto(val direccion: String, val distrito: String = "") {
    val completa: String get() = if (distrito.isBlank()) direccion else "$direccion, $distrito"
}

data class LugarGuardado(
    val id: String,
    val etiqueta: String,
    val ubicacion: UbicacionTexto,
    val tipo: TipoLugar = TipoLugar.OTRO
)

data class Pasajero(
    val id: String,
    val nombre: String,
    val correo: String,
    val celular: String,
    val miembroDesde: String
)

data class Conductor(
    val id: String,
    val nombre: String,
    val celular: String,
    val vehiculo: String,
    val placa: String,
    val calificacion: Double
)

data class Calificacion(
    val estrellas: Int,
    val comentario: String = "",
    val propina: Double = 0.0
)

data class Viaje(
    val id: String,
    val fechaMillis: Long,
    val pasajeroNombre: String,
    val origen: UbicacionTexto,
    val destino: UbicacionTexto,
    val conductor: Conductor?,
    val tipoServicio: TipoServicio,
    val metodoPago: MetodoPago,
    val distanciaKm: Double,
    val duracionMin: Int,
    val tarifa: Double,
    val descuento: Double = 0.0,
    val estado: EstadoViaje = EstadoViaje.FINALIZADO,
    val calificacion: Calificacion? = null
) {
    val total: Double get() = tarifa + (calificacion?.propina ?: 0.0)
    val sinCalificar: Boolean get() = estado == EstadoViaje.FINALIZADO && calificacion == null
}

data class TarifaEstimada(
    val distanciaKm: Double,
    val duracionMin: Int,
    val recargo: Double,
    val subtotal: Double,
    val descuento: Double,
    val total: Double
)

data class ResumenViajes(
    val viajes: Int = 0,
    val gastoTotal: Double = 0.0,
    val gastoDelMes: Double = 0.0,
    val calificacionPromedio: Double = 0.0,
    val servicioFavorito: TipoServicio? = null
)

/** Quién está usando la app en este momento. */
data class Sesion(
    val rol: Rol,
    val pasajero: Pasajero? = null,
    val conductor: Conductor? = null
) {
    val nombre: String get() = pasajero?.nombre ?: conductor?.nombre ?: "Invitado"
    val correo: String get() = pasajero?.correo ?: "conductor@moveperu.pe"
    val iniciales: String get() = inicialesDe(nombre)
}

sealed interface ResultadoAuth {
    data class Exito(val sesion: Sesion) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

// ══════════════════════════════════════════════════════════════════════════════
//  REPOSITORY — contratos
// ══════════════════════════════════════════════════════════════════════════════

interface ViajeRepository {
    fun observarViajes(): Flow<List<Viaje>>
    fun observarViaje(id: String): Flow<Viaje?>
    suspend fun crearViaje(viaje: Viaje)
    suspend fun actualizarEstado(id: String, estado: EstadoViaje)
}

interface ConductorRepository {
    fun observarConductores(): Flow<List<Conductor>>
    suspend fun asignarConductor(tipo: TipoServicio): Conductor
    fun conductorDemo(): Conductor
}

interface CalificacionRepository {
    suspend fun calificar(viajeId: String, calificacion: Calificacion)
}

interface AuthRepository {
    val sesion: StateFlow<Sesion?>
    val lugares: StateFlow<List<LugarGuardado>>
    val pagoPreferido: StateFlow<MetodoPago>
    suspend fun ingresar(correo: String, clave: String, rol: Rol): ResultadoAuth
    suspend fun registrar(
        nombre: String,
        correo: String,
        celular: String,
        clave: String,
        rol: Rol
    ): ResultadoAuth
    fun cerrarSesion()
    fun agregarLugar(lugar: LugarGuardado)
    fun quitarLugar(id: String)
    fun cambiarPagoPreferido(metodo: MetodoPago)
}

// ══════════════════════════════════════════════════════════════════════════════
//  CASOS DE USO
// ══════════════════════════════════════════════════════════════════════════════

/** Cupones simulados. */
object Cupones {
    private val codigos = mapOf("MOVE10" to 0.10, "LIMA20" to 0.20, "BIENVENIDO" to 0.15)
    fun descuentoDe(codigo: String): Double? = codigos[codigo.trim().uppercase()]
    const val VISIBLE = "MOVE10"
}

/** RF04: filtra el historial por tipo de servicio y por rango de fechas. */
class FiltrarHistorialUseCase(private val repository: ViajeRepository) {
    operator fun invoke(
        tipo: TipoServicio?,
        desdeMillis: Long?,
        hastaMillis: Long?
    ): Flow<List<Viaje>> = repository.observarViajes().map { viajes ->
        viajes.filter { viaje ->
            (tipo == null || viaje.tipoServicio == tipo) &&
                    (desdeMillis == null || viaje.fechaMillis >= desdeMillis) &&
                    (hastaMillis == null || viaje.fechaMillis <= hastaMillis)
        }
    }
}

/**
 * RF06: tarifa estimada simulada, sin mapas ni GPS. La distancia se deriva del
 * hash del par origen/destino: el mismo par siempre da el mismo resultado,
 * así el caso de uso es determinista y se puede testear sin mocks.
 */
class EstimarTarifaUseCase {
    operator fun invoke(
        origen: String,
        destino: String,
        tipo: TipoServicio,
        cupon: String = ""
    ): TarifaEstimada {
        val clave = origen.trim().lowercase() + ">" + destino.trim().lowercase()
        val semilla = abs(clave.hashCode())
        val distanciaKm = ((2.5 + (semilla % 215) / 10.0) * 10).roundToInt() / 10.0
        val duracionMin = (6 + distanciaKm * 2.4).roundToInt()
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val recargo = if (hora in 7..9 || hora in 18..21) 1.25 else 1.0
        val subtotal =
            (tipo.tarifaBase + distanciaKm * tipo.porKm + duracionMin * tipo.porMin) * recargo
        val tasa = Cupones.descuentoDe(cupon) ?: 0.0
        val descuento = subtotal * tasa
        return TarifaEstimada(
            distanciaKm = distanciaKm,
            duracionMin = duracionMin,
            recargo = recargo,
            subtotal = subtotal.redondear(),
            descuento = descuento.redondear(),
            total = (subtotal - descuento).redondear()
        )
    }
}

class ObtenerResumenUseCase(private val repository: ViajeRepository) {
    operator fun invoke(): Flow<ResumenViajes> = repository.observarViajes().map { viajes ->
        val terminados = viajes.filter { it.estado == EstadoViaje.FINALIZADO }
        val calificados = terminados.mapNotNull { it.calificacion }
        val inicioDelMes = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        ResumenViajes(
            viajes = terminados.size,
            gastoTotal = terminados.sumOf { it.total },
            gastoDelMes = terminados.filter { it.fechaMillis >= inicioDelMes }.sumOf { it.total },
            calificacionPromedio = if (calificados.isEmpty()) 0.0
            else calificados.sumOf { it.estrellas } / calificados.size.toDouble(),
            servicioFavorito = terminados.groupBy { it.tipoServicio }
                .maxByOrNull { entrada -> entrada.value.size }?.key
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  DATA — datos simulados
// ══════════════════════════════════════════════════════════════════════════════

private object DatosSimulados {

    val conductores = listOf(
        Conductor("C-01", "Carlos Mamani", "+51 987 654 321", "Toyota Yaris", "A2B-431", 4.9),
        Conductor("C-02", "Rosa Quispe", "+51 954 118 220", "Kia Rio", "B7K-902", 4.8),
        Conductor("C-03", "Luis Ccahuana", "+51 921 447 508", "Hyundai Accent", "C4M-118", 4.7),
        Conductor("C-04", "Miriam Huamán", "+51 967 330 914", "Toyota Corolla", "D9P-556", 4.9),
        Conductor("C-05", "Jorge Ayala", "+51 998 201 776", "Chevrolet Sail", "F1T-370", 4.6),
        Conductor("C-06", "Nancy Paredes", "+51 943 662 185", "Hyundai H1", "G6V-284", 5.0),
        Conductor("C-07", "Elmer Chávez", "+51 912 875 043", "Nissan Versa", "H3R-745", 4.5),
        Conductor("C-08", "Sofía Rojas", "+51 976 504 338", "Toyota Hiace", "J8L-619", 4.8)
    )

    val conductorDemo = conductores.first()

    val origenes = listOf(
        UbicacionTexto("Av. Larco 1301", "Miraflores"),
        UbicacionTexto("Calle Berlín 480", "Miraflores"),
        UbicacionTexto("Av. Camino Real 1075", "San Isidro"),
        UbicacionTexto("Av. Javier Prado Este 2050", "San Isidro"),
        UbicacionTexto("Jr. de la Unión 700", "Cercado de Lima"),
        UbicacionTexto("Av. Primavera 1352", "Santiago de Surco"),
        UbicacionTexto("Av. Benavides 3866", "Surco"),
        UbicacionTexto("Av. La Marina 2355", "San Miguel"),
        UbicacionTexto("Av. Angamos Este 1805", "Surquillo"),
        UbicacionTexto("Av. Salaverry 3100", "Magdalena")
    )

    val destinos = listOf(
        UbicacionTexto("Aeropuerto Jorge Chávez", "Callao"),
        UbicacionTexto("Jockey Plaza", "Santiago de Surco"),
        UbicacionTexto("Parque Kennedy", "Miraflores"),
        UbicacionTexto("Estación Gamarra", "La Victoria"),
        UbicacionTexto("Plaza San Miguel", "San Miguel"),
        UbicacionTexto("Centro Cívico", "Cercado de Lima"),
        UbicacionTexto("Real Plaza Salaverry", "Jesús María"),
        UbicacionTexto("Mercado de Surquillo", "Surquillo"),
        UbicacionTexto("Larcomar", "Miraflores"),
        UbicacionTexto("Universidad de Lima", "Surco")
    )

    private val pasajeros = listOf(
        "Arny Saravia", "Diana Flores", "Kevin Ríos", "Patricia Vega",
        "Renzo Alarcón", "Lucía Bravo", "Marco Tello", "Gabriela Nieto"
    )

    private val comentarios = listOf(
        "Puntual y muy amable.",
        "Buena ruta, evitó el tráfico de Javier Prado.",
        "El auto estaba impecable.",
        "Llegó unos minutos tarde, pero todo bien.",
        ""
    )

    val lugaresIniciales = listOf(
        LugarGuardado("casa", "Casa", UbicacionTexto("Av. Primavera 1352", "Santiago de Surco"), TipoLugar.CASA),
        LugarGuardado("trabajo", "Trabajo", UbicacionTexto("Av. Javier Prado Este 2050", "San Isidro"), TipoLugar.TRABAJO),
        LugarGuardado("aeropuerto", "Aeropuerto", UbicacionTexto("Aeropuerto Jorge Chávez", "Callao"))
    )

    /**
     * RF03: 30 viajes finalizados para el historial, más 3 viajes activos
     * asignados al conductor de demostración para que RF09 y RF10 tengan
     * contenido desde el primer arranque. Total: 33.
     */
    fun viajesIniciales(): List<Viaje> {
        val estimador = EstimarTarifaUseCase()

        val finalizados = List(30) { i ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -(i * 2 + 1))
                set(Calendar.HOUR_OF_DAY, 7 + (i % 13))
                set(Calendar.MINUTE, (i * 7) % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val tipo = TipoServicio.entries[i % TipoServicio.entries.size]
            val origen = origenes[i % origenes.size]
            val destino = destinos[(i * 3) % destinos.size]
            val conductor = conductores[i % conductores.size]
            val estimada = estimador(origen.completa, destino.completa, tipo)
            Viaje(
                id = "MP-%04d".format(1000 + i),
                fechaMillis = cal.timeInMillis,
                pasajeroNombre = pasajeros[i % pasajeros.size],
                origen = origen,
                destino = destino,
                conductor = conductor,
                tipoServicio = tipo,
                metodoPago = MetodoPago.entries[i % MetodoPago.entries.size],
                distanciaKm = estimada.distanciaKm,
                duracionMin = estimada.duracionMin,
                tarifa = estimada.total,
                estado = EstadoViaje.FINALIZADO,
                calificacion = if (i % 9 == 0) null else Calificacion(
                    estrellas = 3 + (i % 3),
                    comentario = comentarios[i % comentarios.size]
                )
            )
        }

        val estadosActivos = listOf(
            EstadoViaje.SOLICITADO,
            EstadoViaje.ACEPTADO,
            EstadoViaje.EN_CAMINO
        )
        val activos = estadosActivos.mapIndexed { i, estado ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.MINUTE, -(12 * (i + 1)))
            }
            val tipo = TipoServicio.entries[i % TipoServicio.entries.size]
            val origen = origenes[(i + 4) % origenes.size]
            val destino = destinos[(i + 2) % destinos.size]
            val estimada = estimador(origen.completa, destino.completa, tipo)
            Viaje(
                id = "MP-90%02d".format(i + 1),
                fechaMillis = cal.timeInMillis,
                pasajeroNombre = pasajeros[(i + 1) % pasajeros.size],
                origen = origen,
                destino = destino,
                conductor = conductorDemo,
                tipoServicio = tipo,
                metodoPago = MetodoPago.entries[(i + 1) % MetodoPago.entries.size],
                distanciaKm = estimada.distanciaKm,
                duracionMin = estimada.duracionMin,
                tarifa = estimada.total,
                estado = estado
            )
        }

        return activos + finalizados
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  DATA — implementaciones en memoria
// ══════════════════════════════════════════════════════════════════════════════

/** Única fuente de verdad de los viajes. La comparten viajes y calificaciones. */
class AlmacenViajes {
    val viajes = MutableStateFlow(DatosSimulados.viajesIniciales())

    fun modificar(id: String, transformacion: (Viaje) -> Viaje) {
        viajes.update { lista -> lista.map { if (it.id == id) transformacion(it) else it } }
    }
}

class ViajeRepositoryImpl(private val almacen: AlmacenViajes) : ViajeRepository {

    override fun observarViajes(): Flow<List<Viaje>> =
        almacen.viajes.map { lista -> lista.sortedByDescending { it.fechaMillis } }

    override fun observarViaje(id: String): Flow<Viaje?> =
        almacen.viajes.map { lista -> lista.firstOrNull { it.id == id } }

    override suspend fun crearViaje(viaje: Viaje) {
        almacen.viajes.update { listOf(viaje) + it }
    }

    /** RF10: el cambio de estado pasa siempre por aquí, lo pida quien lo pida. */
    override suspend fun actualizarEstado(id: String, estado: EstadoViaje) {
        almacen.modificar(id) { viaje ->
            if (estado == EstadoViaje.CANCELADO) viaje.copy(estado = estado, tarifa = 0.0)
            else viaje.copy(estado = estado)
        }
    }
}

class ConductorRepositoryImpl : ConductorRepository {

    override fun observarConductores(): Flow<List<Conductor>> =
        MutableStateFlow(DatosSimulados.conductores)

    /** RF07: asigna un conductor ficticio según el tipo de servicio pedido. */
    override suspend fun asignarConductor(tipo: TipoServicio): Conductor {
        delay(400)
        val candidatos = when (tipo) {
            TipoServicio.VAN -> DatosSimulados.conductores.filter {
                it.vehiculo.contains("H1") || it.vehiculo.contains("Hiace")
            }
            else -> DatosSimulados.conductores.filterNot {
                it.vehiculo.contains("H1") || it.vehiculo.contains("Hiace")
            }
        }
        return candidatos.randomOrNull() ?: DatosSimulados.conductores.random()
    }

    override fun conductorDemo(): Conductor = DatosSimulados.conductorDemo
}

class CalificacionRepositoryImpl(private val almacen: AlmacenViajes) : CalificacionRepository {
    override suspend fun calificar(viajeId: String, calificacion: Calificacion) {
        almacen.modificar(viajeId) { it.copy(calificacion = calificacion) }
    }
}

class AuthRepositoryImpl(private val conductores: ConductorRepository) : AuthRepository {

    private val _sesion = MutableStateFlow<Sesion?>(null)
    override val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    private val _lugares = MutableStateFlow(DatosSimulados.lugaresIniciales)
    override val lugares: StateFlow<List<LugarGuardado>> = _lugares.asStateFlow()

    private val _pagoPreferido = MutableStateFlow(MetodoPago.YAPE)
    override val pagoPreferido: StateFlow<MetodoPago> = _pagoPreferido.asStateFlow()

    override suspend fun ingresar(correo: String, clave: String, rol: Rol): ResultadoAuth {
        delay(900)  // simula la llamada de red
        validar(correo, clave)?.let { return ResultadoAuth.Error(it) }
        val sesionNueva = when (rol) {
            Rol.PASAJERO -> Sesion(
                rol = rol,
                pasajero = Pasajero(
                    id = "P-" + abs(correo.hashCode()),
                    nombre = nombreDesdeCorreo(correo),
                    correo = correo.trim().lowercase(),
                    celular = "+51 999 888 777",
                    miembroDesde = "Marzo 2026"
                )
            )
            Rol.CONDUCTOR -> Sesion(rol = rol, conductor = conductores.conductorDemo())
        }
        _sesion.value = sesionNueva
        return ResultadoAuth.Exito(sesionNueva)
    }

    override suspend fun registrar(
        nombre: String,
        correo: String,
        celular: String,
        clave: String,
        rol: Rol
    ): ResultadoAuth {
        delay(1100)
        if (nombre.trim().length < 3) return ResultadoAuth.Error("Escribe tu nombre completo")
        if (celular.filter { it.isDigit() }.length < 9) {
            return ResultadoAuth.Error("El celular debe tener 9 dígitos")
        }
        validar(correo, clave)?.let { return ResultadoAuth.Error(it) }
        val sesionNueva = when (rol) {
            Rol.PASAJERO -> Sesion(
                rol = rol,
                pasajero = Pasajero(
                    id = "P-" + abs(correo.hashCode()),
                    nombre = nombre.trim(),
                    correo = correo.trim().lowercase(),
                    celular = celular.trim(),
                    miembroDesde = mesActual()
                )
            )
            Rol.CONDUCTOR -> Sesion(
                rol = rol,
                conductor = conductores.conductorDemo().copy(
                    nombre = nombre.trim(),
                    celular = celular.trim()
                )
            )
        }
        _sesion.value = sesionNueva
        return ResultadoAuth.Exito(sesionNueva)
    }

    override fun cerrarSesion() {
        _sesion.value = null
    }

    override fun agregarLugar(lugar: LugarGuardado) = _lugares.update { it + lugar }

    override fun quitarLugar(id: String) =
        _lugares.update { lista -> lista.filterNot { it.id == id } }

    override fun cambiarPagoPreferido(metodo: MetodoPago) {
        _pagoPreferido.value = metodo
    }

    private fun validar(correo: String, clave: String): String? = when {
        !correo.contains("@") || !correo.contains(".") -> "Ingresa un correo válido"
        clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    private fun nombreDesdeCorreo(correo: String): String {
        val local = correo.substringBefore("@").replace(".", " ").replace("_", " ")
        return local.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { parte -> parte.replaceFirstChar { it.uppercaseChar() } }
            .ifBlank { "Pasajero MovePerú" }
    }
}

/**
 * Inyección manual de dependencias. Al migrar a Hilt, este objeto desaparece
 * y lo reemplazan los @Module con @Provides / @Binds.
 */
object ServiceLocator {
    private val almacen by lazy { AlmacenViajes() }
    val viajeRepository: ViajeRepository by lazy { ViajeRepositoryImpl(almacen) }
    val calificacionRepository: CalificacionRepository by lazy { CalificacionRepositoryImpl(almacen) }
    val conductorRepository: ConductorRepository by lazy { ConductorRepositoryImpl() }
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(conductorRepository) }
    val filtrarHistorial by lazy { FiltrarHistorialUseCase(viajeRepository) }
    val obtenerResumen by lazy { ObtenerResumenUseCase(viajeRepository) }
    val estimarTarifa by lazy { EstimarTarifaUseCase() }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — autenticación (RF01, RF02)
// ══════════════════════════════════════════════════════════════════════════════

enum class ModoAuth { INGRESO, REGISTRO }

data class AuthUiState(
    val modo: ModoAuth = ModoAuth.INGRESO,
    val rol: Rol = Rol.PASAJERO,
    val nombre: String = "",
    val correo: String = "",
    val celular: String = "",
    val clave: String = "",
    val confirmacion: String = "",
    val verClave: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null
) {
    val puedeEnviar: Boolean
        get() = !cargando && correo.isNotBlank() && clave.isNotBlank() &&
                (modo == ModoAuth.INGRESO || (nombre.isNotBlank() && celular.isNotBlank()))
}

class AuthViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    /** Evento de una sola vez: el rol con el que se entró, para decidir el destino. */
    private val _sesionIniciada = Channel<Rol>(Channel.BUFFERED)
    val sesionIniciada: Flow<Rol> = _sesionIniciada.receiveAsFlow()

    fun onModoChange(modo: ModoAuth) = _state.update { it.copy(modo = modo, error = null) }
    fun onRolChange(rol: Rol) = _state.update { it.copy(rol = rol, error = null) }
    fun onNombreChange(v: String) = _state.update { it.copy(nombre = v, error = null) }
    fun onCorreoChange(v: String) = _state.update { it.copy(correo = v, error = null) }
    fun onCelularChange(v: String) = _state.update { it.copy(celular = v, error = null) }
    fun onClaveChange(v: String) = _state.update { it.copy(clave = v, error = null) }
    fun onConfirmacionChange(v: String) = _state.update { it.copy(confirmacion = v, error = null) }
    fun onVerClave() = _state.update { it.copy(verClave = !it.verClave) }

    fun onUsarDemo() = _state.update {
        it.copy(
            correo = if (it.rol == Rol.PASAJERO) "arny.saravia@moveperu.pe"
            else "carlos.mamani@moveperu.pe",
            clave = "moveperu2026",
            error = null
        )
    }

    fun onEnviar() {
        val actual = _state.value
        if (!actual.puedeEnviar) return
        if (actual.modo == ModoAuth.REGISTRO && actual.clave != actual.confirmacion) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            val resultado = if (actual.modo == ModoAuth.INGRESO) {
                auth.ingresar(actual.correo, actual.clave, actual.rol)
            } else {
                auth.registrar(actual.nombre, actual.correo, actual.celular, actual.clave, actual.rol)
            }
            when (resultado) {
                is ResultadoAuth.Exito -> {
                    _state.update { it.copy(cargando = false) }
                    _sesionIniciada.send(resultado.sesion.rol)
                }
                is ResultadoAuth.Error ->
                    _state.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AuthViewModel(ServiceLocator.authRepository) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — inicio del pasajero
// ══════════════════════════════════════════════════════════════════════════════

data class InicioUiState(
    val sesion: Sesion? = null,
    val lugares: List<LugarGuardado> = emptyList(),
    val resumen: ResumenViajes = ResumenViajes(),
    val ultimoViaje: Viaje? = null,
    val viajeActivo: Viaje? = null,
    val sinCalificar: Viaje? = null
)

class InicioViewModel(
    auth: AuthRepository,
    viajes: ViajeRepository,
    obtenerResumen: ObtenerResumenUseCase
) : ViewModel() {

    val state: StateFlow<InicioUiState> = combine(
        auth.sesion,
        auth.lugares,
        obtenerResumen(),
        viajes.observarViajes()
    ) { sesion, lugares, resumen, lista ->
        InicioUiState(
            sesion = sesion,
            lugares = lugares,
            resumen = resumen,
            ultimoViaje = lista.firstOrNull { it.estado == EstadoViaje.FINALIZADO },
            viajeActivo = lista.firstOrNull { it.estado.activo },
            sinCalificar = lista.firstOrNull { it.sinCalificar }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUiState())

    companion object {
        val Factory = viewModelFactory {
            initializer {
                InicioViewModel(
                    ServiceLocator.authRepository,
                    ServiceLocator.viajeRepository,
                    ServiceLocator.obtenerResumen
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — historial (RF03, RF04)
// ══════════════════════════════════════════════════════════════════════════════

data class FiltroHistorial(
    val tipoServicio: TipoServicio? = null,
    val desdeMillis: Long? = null,
    val hastaMillis: Long? = null
) {
    val tieneRango: Boolean get() = desdeMillis != null || hastaMillis != null
    val vacio: Boolean get() = tipoServicio == null && !tieneRango
}

sealed interface HistorialUiState {
    data object Cargando : HistorialUiState
    data class Exito(
        val viajes: List<Viaje>,
        val filtro: FiltroHistorial,
        val gastoTotal: Double
    ) : HistorialUiState
    data class Error(val mensaje: String) : HistorialUiState
}

class HistorialViajeViewModel(
    private val filtrarHistorial: FiltrarHistorialUseCase
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroHistorial())

    val uiState: StateFlow<HistorialUiState> = filtro
        .flatMapLatest<FiltroHistorial, HistorialUiState> { actual ->
            filtrarHistorial(actual.tipoServicio, actual.desdeMillis, actual.hastaMillis)
                .map { lista ->
                    HistorialUiState.Exito(
                        viajes = lista,
                        filtro = actual,
                        gastoTotal = lista.filter { it.estado == EstadoViaje.FINALIZADO }
                            .sumOf { it.total }
                    )
                }
        }
        .catch { error ->
            emit(HistorialUiState.Error(error.message ?: "No se pudo cargar el historial"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState.Cargando)

    fun onTipoSeleccionado(tipo: TipoServicio?) = filtro.update { it.copy(tipoServicio = tipo) }

    fun onRangoSeleccionado(desde: Long?, hasta: Long?) =
        filtro.update { it.copy(desdeMillis = desde, hastaMillis = hasta) }

    fun onLimpiarFiltros() {
        filtro.value = FiltroHistorial()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HistorialViajeViewModel(ServiceLocator.filtrarHistorial) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — solicitar viaje (RF05, RF06, RF07)
// ══════════════════════════════════════════════════════════════════════════════

data class SolicitudUiState(
    val origen: String = "",
    val destino: String = "",
    val tipoServicio: TipoServicio = TipoServicio.ECONOMICO,
    val metodoPago: MetodoPago = MetodoPago.YAPE,
    val cupon: String = "",
    val cuponAplicado: Boolean = false,
    val lugares: List<LugarGuardado> = emptyList(),
    val estimada: TarifaEstimada? = null,
    val error: String? = null,
    val enviando: Boolean = false
) {
    val puedeEstimar: Boolean get() = origen.isNotBlank() && destino.isNotBlank()
}

class SolicitudViajeViewModel(
    private val estimarTarifa: EstimarTarifaUseCase,
    private val viajes: ViajeRepository,
    private val conductores: ConductorRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        SolicitudUiState(
            lugares = auth.lugares.value,
            metodoPago = auth.pagoPreferido.value
        )
    )
    val state: StateFlow<SolicitudUiState> = _state.asStateFlow()

    private val _viajeCreado = Channel<String>(Channel.BUFFERED)
    val viajeCreado: Flow<String> = _viajeCreado.receiveAsFlow()

    private var precargado = false

    fun precargar(origen: String, destino: String) {
        if (precargado) return
        precargado = true
        if (origen.isBlank() && destino.isBlank()) return
        _state.update { it.copy(origen = origen, destino = destino) }
        recalcular()
    }

    fun onOrigenChange(v: String) =
        _state.update { it.copy(origen = v, estimada = null, error = null) }

    fun onDestinoChange(v: String) =
        _state.update { it.copy(destino = v, estimada = null, error = null) }

    fun onTipoChange(tipo: TipoServicio) {
        _state.update { it.copy(tipoServicio = tipo) }
        if (_state.value.estimada != null) recalcular()
    }

    fun onPagoChange(metodo: MetodoPago) = _state.update { it.copy(metodoPago = metodo) }

    fun onCuponChange(v: String) =
        _state.update { it.copy(cupon = v, cuponAplicado = false, error = null) }

    fun onAplicarCupon() {
        if (Cupones.descuentoDe(_state.value.cupon) == null) {
            _state.update { it.copy(cuponAplicado = false, error = "Ese cupón no existe o ya venció") }
            return
        }
        _state.update { it.copy(cuponAplicado = true, error = null) }
        if (_state.value.estimada != null) recalcular()
    }

    fun onEstimar() {
        if (!_state.value.puedeEstimar) {
            _state.update { it.copy(error = "Escribe el origen y el destino") }
            return
        }
        recalcular()
    }

    private fun recalcular() {
        val actual = _state.value
        if (!actual.puedeEstimar) return
        val estimada = estimarTarifa(
            origen = actual.origen,
            destino = actual.destino,
            tipo = actual.tipoServicio,
            cupon = if (actual.cuponAplicado) actual.cupon else ""
        )
        _state.update { it.copy(estimada = estimada, error = null) }
    }

    /** RF05 y RF07: crea el viaje y le asigna un conductor ficticio. */
    fun onConfirmar() {
        val actual = _state.value
        val estimada = actual.estimada ?: return
        viewModelScope.launch {
            _state.update { it.copy(enviando = true) }
            val conductor = conductores.asignarConductor(actual.tipoServicio)
            val viaje = Viaje(
                id = "MP-" + System.currentTimeMillis().toString().takeLast(5),
                fechaMillis = System.currentTimeMillis(),
                pasajeroNombre = auth.sesion.value?.nombre ?: "Pasajero",
                origen = UbicacionTexto(actual.origen.trim()),
                destino = UbicacionTexto(actual.destino.trim()),
                conductor = conductor,
                tipoServicio = actual.tipoServicio,
                metodoPago = actual.metodoPago,
                distanciaKm = estimada.distanciaKm,
                duracionMin = estimada.duracionMin,
                tarifa = estimada.total,
                descuento = estimada.descuento,
                estado = EstadoViaje.SOLICITADO
            )
            viajes.crearViaje(viaje)
            _state.update { it.copy(enviando = false) }
            _viajeCreado.send(viaje.id)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SolicitudViajeViewModel(
                    ServiceLocator.estimarTarifa,
                    ServiceLocator.viajeRepository,
                    ServiceLocator.conductorRepository,
                    ServiceLocator.authRepository
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — seguimiento (RF07)
// ══════════════════════════════════════════════════════════════════════════════

data class SeguimientoUiState(
    val viaje: Viaje? = null,
    val minutosRestantes: Int = 0
) {
    val estado: EstadoViaje get() = viaje?.estado ?: EstadoViaje.SOLICITADO
    val finalizado: Boolean get() = estado == EstadoViaje.FINALIZADO
    val cancelado: Boolean get() = estado == EstadoViaje.CANCELADO
    val puedeCancelar: Boolean
        get() = estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO ||
                estado == EstadoViaje.EN_CAMINO
    val progreso: Float
        get() {
            val indice = EstadoViaje.flujo.indexOf(estado)
            return if (indice < 0) 0f else indice / (EstadoViaje.flujo.size - 1).toFloat()
        }
}

class SeguimientoViewModel(private val viajes: ViajeRepository) : ViewModel() {

    private val _state = MutableStateFlow(SeguimientoUiState())
    val state: StateFlow<SeguimientoUiState> = _state.asStateFlow()

    private var iniciado = false
    private var viajeId: String = ""

    fun iniciar(id: String) {
        if (iniciado) return
        iniciado = true
        viajeId = id
        viewModelScope.launch {
            viajes.observarViaje(id).collect { viaje ->
                _state.update {
                    it.copy(
                        viaje = viaje,
                        minutosRestantes = minutosPara(viaje?.estado, viaje?.duracionMin ?: 0)
                    )
                }
            }
        }
        viewModelScope.launch { simular(id) }
    }

    /**
     * Avanza el estado automáticamente para la demo. Lee siempre el estado
     * actual del repositorio, así que si el conductor lo mueve desde su panel,
     * la simulación continúa desde donde él lo dejó.
     */
    private suspend fun simular(id: String) {
        while (true) {
            delay(3_500)
            val actual = viajes.observarViaje(id).first() ?: return
            val siguiente = actual.estado.siguiente ?: return
            viajes.actualizarEstado(id, siguiente)
        }
    }

    fun onCancelar() {
        if (!_state.value.puedeCancelar) return
        viewModelScope.launch { viajes.actualizarEstado(viajeId, EstadoViaje.CANCELADO) }
    }

    private fun minutosPara(estado: EstadoViaje?, duracion: Int): Int = when (estado) {
        EstadoViaje.SOLICITADO -> 4
        EstadoViaje.ACEPTADO -> 3
        EstadoViaje.EN_CAMINO -> 2
        EstadoViaje.INICIADO -> duracion
        else -> 0
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SeguimientoViewModel(ServiceLocator.viajeRepository) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — calificación (RF08)
// ══════════════════════════════════════════════════════════════════════════════

data class CalificacionUiState(
    val viaje: Viaje? = null,
    val estrellas: Int = 5,
    val comentario: String = "",
    val propina: Double = 0.0,
    val guardada: Boolean = false
)

class CalificacionViewModel(
    private val viajes: ViajeRepository,
    private val calificaciones: CalificacionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalificacionUiState())
    val state: StateFlow<CalificacionUiState> = _state.asStateFlow()

    private var cargado = false

    fun cargar(viajeId: String) {
        if (cargado) return
        cargado = true
        viewModelScope.launch {
            viajes.observarViaje(viajeId).collect { viaje -> _state.update { it.copy(viaje = viaje) } }
        }
    }

    fun onEstrellasChange(valor: Int) = _state.update { it.copy(estrellas = valor) }
    fun onComentarioChange(valor: String) = _state.update { it.copy(comentario = valor) }
    fun onPropinaChange(monto: Double) = _state.update { it.copy(propina = monto) }

    fun onEnviar() {
        val actual = _state.value
        val viaje = actual.viaje ?: return
        viewModelScope.launch {
            calificaciones.calificar(
                viaje.id,
                Calificacion(actual.estrellas, actual.comentario.trim(), actual.propina)
            )
            _state.update { it.copy(guardada = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CalificacionViewModel(
                    ServiceLocator.viajeRepository,
                    ServiceLocator.calificacionRepository
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — detalle del viaje
// ══════════════════════════════════════════════════════════════════════════════

class DetalleViajeViewModel(private val viajes: ViajeRepository) : ViewModel() {

    private val _viaje = MutableStateFlow<Viaje?>(null)
    val viaje: StateFlow<Viaje?> = _viaje.asStateFlow()

    private var cargado = false

    fun cargar(viajeId: String) {
        if (cargado) return
        cargado = true
        viewModelScope.launch {
            viajes.observarViaje(viajeId).collect { valor -> _viaje.value = valor }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { DetalleViajeViewModel(ServiceLocator.viajeRepository) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — conductor (RF09, RF10)
// ══════════════════════════════════════════════════════════════════════════════

data class ConductorUiState(
    val conductor: Conductor? = null,
    val disponible: Boolean = true,
    val asignados: List<Viaje> = emptyList(),
    val terminados: List<Viaje> = emptyList(),
    val gananciaDelDia: Double = 0.0,
    val viajesDelDia: Int = 0
)

class ConductorViewModel(
    auth: AuthRepository,
    private val viajes: ViajeRepository
) : ViewModel() {

    private val disponible = MutableStateFlow(true)

    val state: StateFlow<ConductorUiState> = combine(
        auth.sesion,
        viajes.observarViajes(),
        disponible
    ) { sesion, lista, libre ->
        val conductor = sesion?.conductor
        val propios = lista.filter { it.conductor?.id == conductor?.id }
        val inicioDelDia = inicioDeHoy()
        val terminadosHoy = propios.filter {
            it.estado == EstadoViaje.FINALIZADO && it.fechaMillis >= inicioDelDia
        }
        ConductorUiState(
            conductor = conductor,
            disponible = libre,
            asignados = propios.filter { it.estado.activo },
            terminados = propios.filter { !it.estado.activo }.take(10),
            gananciaDelDia = terminadosHoy.sumOf { it.total },
            viajesDelDia = terminadosHoy.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConductorUiState())

    fun onDisponibilidadChange(valor: Boolean) {
        disponible.value = valor
    }

    /** RF10: aceptado → en camino → iniciado → finalizado. */
    fun onAvanzarEstado(viaje: Viaje) {
        val siguiente = viaje.estado.siguiente ?: return
        viewModelScope.launch { viajes.actualizarEstado(viaje.id, siguiente) }
    }

    fun onCancelar(viaje: Viaje) {
        viewModelScope.launch { viajes.actualizarEstado(viaje.id, EstadoViaje.CANCELADO) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ConductorViewModel(ServiceLocator.authRepository, ServiceLocator.viajeRepository)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEWMODEL — perfil
// ══════════════════════════════════════════════════════════════════════════════

data class PerfilUiState(
    val sesion: Sesion? = null,
    val lugares: List<LugarGuardado> = emptyList(),
    val pago: MetodoPago = MetodoPago.YAPE,
    val resumen: ResumenViajes = ResumenViajes()
)

class PerfilViewModel(
    private val auth: AuthRepository,
    obtenerResumen: ObtenerResumenUseCase
) : ViewModel() {

    val state: StateFlow<PerfilUiState> = combine(
        auth.sesion,
        auth.lugares,
        auth.pagoPreferido,
        obtenerResumen()
    ) { sesion, lugares, pago, resumen ->
        PerfilUiState(sesion, lugares, pago, resumen)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilUiState())

    fun onAgregarLugar(etiqueta: String, direccion: String) {
        if (etiqueta.isBlank() || direccion.isBlank()) return
        auth.agregarLugar(
            LugarGuardado(
                id = "lugar-" + System.currentTimeMillis(),
                etiqueta = etiqueta.trim(),
                ubicacion = UbicacionTexto(direccion.trim())
            )
        )
    }

    fun onQuitarLugar(id: String) = auth.quitarLugar(id)
    fun onPagoChange(metodo: MetodoPago) = auth.cambiarPagoPreferido(metodo)
    fun onCerrarSesion() = auth.cerrarSesion()

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PerfilViewModel(ServiceLocator.authRepository, ServiceLocator.obtenerResumen)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  NAVEGACIÓN
// ══════════════════════════════════════════════════════════════════════════════

object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val HISTORIAL = "historial"
    const val PERFIL = "perfil"
    const val SOLICITAR = "solicitar?origen={origen}&destino={destino}"
    const val SEGUIMIENTO = "seguimiento/{viajeId}"
    const val CALIFICACION = "calificacion/{viajeId}"
    const val DETALLE = "detalle/{viajeId}"
    const val CONDUCTOR_PANEL = "conductor"
    const val CONDUCTOR_DETALLE = "conductor/detalle/{viajeId}"

    fun solicitar(origen: String = "", destino: String = "") =
        "solicitar?origen=${Uri.encode(origen)}&destino=${Uri.encode(destino)}"

    fun seguimiento(viajeId: String) = "seguimiento/$viajeId"
    fun calificacion(viajeId: String) = "calificacion/$viajeId"
    fun detalle(viajeId: String) = "detalle/$viajeId"
    fun conductorDetalle(viajeId: String) = "conductor/detalle/$viajeId"
}

@Composable
fun MovePeruApp() {
    val navController = rememberNavController()

    // Pestañas del pasajero: conservan el estado de cada una.
    val irAPestana: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(Rutas.INICIO) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    // Pestañas del conductor.
    val irAPestanaConductor: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(Rutas.CONDUCTOR_PANEL) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    // Cerrar sesión deja la pila en Login, que nunca se quita del backstack.
    val volverALogin: () -> Unit = { navController.popBackStack(Rutas.LOGIN, inclusive = false) }

    NavHost(navController = navController, startDestination = Rutas.SPLASH) {

        composable(Rutas.SPLASH) {
            SplashScreen(
                onTerminado = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.LOGIN) {
            LoginScreen(
                onIngreso = { rol ->
                    val destino = if (rol == Rol.PASAJERO) Rutas.INICIO else Rutas.CONDUCTOR_PANEL
                    navController.navigate(destino) { launchSingleTop = true }
                }
            )
        }

        // ── Pasajero ──────────────────────────────────────────────────────────
        composable(Rutas.INICIO) {
            InicioScreen(
                onSolicitar = { destino -> navController.navigate(Rutas.solicitar(destino = destino)) },
                onRepetir = { viaje ->
                    navController.navigate(Rutas.solicitar(viaje.origen.completa, viaje.destino.completa))
                },
                onVerViaje = { viaje -> navController.navigate(Rutas.detalle(viaje.id)) },
                onSeguir = { viaje -> navController.navigate(Rutas.seguimiento(viaje.id)) },
                onCalificar = { viaje -> navController.navigate(Rutas.calificacion(viaje.id)) },
                onPestana = irAPestana
            )
        }

        composable(Rutas.HISTORIAL) {
            HistorialScreen(
                onSolicitar = { navController.navigate(Rutas.solicitar()) },
                onVerViaje = { viaje -> navController.navigate(Rutas.detalle(viaje.id)) },
                onPestana = irAPestana
            )
        }

        composable(
            route = Rutas.SOLICITAR,
            arguments = listOf(
                navArgument("origen") { type = NavType.StringType; defaultValue = "" },
                navArgument("destino") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entrada ->
            SolicitarViajeScreen(
                origenInicial = entrada.arguments?.getString("origen").orEmpty(),
                destinoInicial = entrada.arguments?.getString("destino").orEmpty(),
                onAtras = { navController.popBackStack() },
                onViajeCreado = { viajeId ->
                    navController.navigate(Rutas.seguimiento(viajeId)) { popUpTo(Rutas.INICIO) }
                }
            )
        }

        composable(
            route = Rutas.SEGUIMIENTO,
            arguments = listOf(navArgument("viajeId") { type = NavType.StringType })
        ) { entrada ->
            val viajeId = entrada.arguments?.getString("viajeId").orEmpty()
            SeguimientoScreen(
                viajeId = viajeId,
                onFinalizado = {
                    navController.navigate(Rutas.calificacion(viajeId)) { popUpTo(Rutas.INICIO) }
                },
                onCancelado = { navController.popBackStack(Rutas.INICIO, inclusive = false) }
            )
        }

        composable(
            route = Rutas.CALIFICACION,
            arguments = listOf(navArgument("viajeId") { type = NavType.StringType })
        ) { entrada ->
            CalificacionScreen(
                viajeId = entrada.arguments?.getString("viajeId").orEmpty(),
                onListo = { navController.popBackStack(Rutas.INICIO, inclusive = false) }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("viajeId") { type = NavType.StringType })
        ) { entrada ->
            val viajeId = entrada.arguments?.getString("viajeId").orEmpty()
            DetalleViajeScreen(
                viajeId = viajeId,
                onAtras = { navController.popBackStack() },
                onRepetir = { viaje ->
                    navController.navigate(Rutas.solicitar(viaje.origen.completa, viaje.destino.completa))
                },
                onCalificar = { navController.navigate(Rutas.calificacion(viajeId)) }
            )
        }

        // ── Conductor ─────────────────────────────────────────────────────────
        composable(Rutas.CONDUCTOR_PANEL) {
            ConductorDashboardScreen(
                onVerViaje = { viaje -> navController.navigate(Rutas.conductorDetalle(viaje.id)) },
                onPestana = irAPestanaConductor
            )
        }

        composable(
            route = Rutas.CONDUCTOR_DETALLE,
            arguments = listOf(navArgument("viajeId") { type = NavType.StringType })
        ) { entrada ->
            ConductorDetalleScreen(
                viajeId = entrada.arguments?.getString("viajeId").orEmpty(),
                onAtras = { navController.popBackStack() }
            )
        }

        // ── Compartido ────────────────────────────────────────────────────────
        composable(Rutas.PERFIL) {
            PerfilScreen(
                onSesionCerrada = volverALogin,
                onPestanaPasajero = irAPestana,
                onPestanaConductor = irAPestanaConductor
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Splash (RF01)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SplashScreen(onTerminado: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1_800)
        onTerminado()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MarcaMovePeru(tamano = 132.dp)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "MovePerú",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Transporte privado en Lima",
                style = MaterialTheme.typography.bodyMedium,
                color = AguaClara
            )
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = Senal,
                strokeWidth = 3.dp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Login (RF01, RF02)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(
    onIngreso: (Rol) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.sesionIniciada.collect { rol -> onIngreso(rol) } }

    Surface(color = MaterialTheme.colorScheme.primary) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Row(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarcaMovePeru(tamano = 64.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "MovePerú",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pide tu viaje en Lima en menos de un minuto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AguaClara
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(Modifier.padding(24.dp)) {

                    // RF02: elegir con qué rol se entra
                    SubtituloSeccion("¿Cómo vas a ingresar?")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Rol.entries.forEach { rol ->
                            TarjetaSeleccion(
                                modifier = Modifier.weight(1f),
                                titulo = rol.etiqueta,
                                subtitulo = if (rol == Rol.PASAJERO) "Pido viajes"
                                else "Atiendo viajes",
                                seleccionada = state.rol == rol,
                                onClick = { viewModel.onRolChange(rol) }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipMove(
                            texto = "Ingresar",
                            seleccionado = state.modo == ModoAuth.INGRESO,
                            onClick = { viewModel.onModoChange(ModoAuth.INGRESO) }
                        )
                        ChipMove(
                            texto = "Crear cuenta",
                            seleccionado = state.modo == ModoAuth.REGISTRO,
                            onClick = { viewModel.onModoChange(ModoAuth.REGISTRO) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (state.modo == ModoAuth.REGISTRO) {
                        OutlinedTextField(
                            value = state.nombre,
                            onValueChange = viewModel::onNombreChange,
                            label = { Text("Nombre completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = state.correo,
                        onValueChange = viewModel::onCorreoChange,
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        isError = state.error != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.modo == ModoAuth.REGISTRO) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.celular,
                            onValueChange = viewModel::onCelularChange,
                            label = { Text("Celular") },
                            placeholder = { Text("987 654 321") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.clave,
                        onValueChange = viewModel::onClaveChange,
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            TextButton(onClick = viewModel::onVerClave) {
                                Text(if (state.verClave) "Ocultar" else "Ver")
                            }
                        },
                        visualTransformation = if (state.verClave) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = state.error != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.modo == ModoAuth.REGISTRO) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.confirmacion,
                            onValueChange = viewModel::onConfirmacionChange,
                            label = { Text("Repite la contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    state.error?.let { mensaje ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::onEnviar,
                        enabled = state.puedeEnviar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (state.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (state.modo == ModoAuth.INGRESO)
                                    "Entrar como ${state.rol.etiqueta.lowercase()}"
                                else "Crear cuenta",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = viewModel::onUsarDemo,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Usar cuenta de demostración") }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "La autenticación es simulada: funciona cualquier correo válido " +
                                "con una contraseña de 6 caracteres.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Inicio del pasajero
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun InicioScreen(
    onSolicitar: (String) -> Unit,
    onRepetir: (Viaje) -> Unit,
    onVerViaje: (Viaje) -> Unit,
    onSeguir: (Viaje) -> Unit,
    onCalificar: (Viaje) -> Unit,
    onPestana: (String) -> Unit,
    viewModel: InicioViewModel = viewModel(factory = InicioViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalirDeLaApp()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BarraPasajero(actual = Rutas.INICIO, onSelect = onPestana) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(saludo(), style = MaterialTheme.typography.bodyMedium, color = AguaClara)
                    Text(
                        text = state.sesion?.nombre ?: "Pasajero",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Avatar(
                    iniciales = state.sesion?.iniciales ?: "MP",
                    onClick = { onPestana(Rutas.PERFIL) }
                )
            }

            Column(Modifier.padding(16.dp)) {

                // Viaje activo: acceso directo al seguimiento
                state.viajeActivo?.let { viaje ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSeguir(viaje) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Viaje en curso",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = viaje.estado.detalle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSolicitar("") },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "¿A dónde vamos?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Escribe tu destino y calcula la tarifa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }

                state.sinCalificar?.let { viaje ->
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Tienes un viaje sin calificar",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Con ${viaje.conductor?.nombre ?: "tu conductor"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            TextButton(onClick = { onCalificar(viaje) }) { Text("Calificar") }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                SubtituloSeccion("Lugares guardados")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.lugares.forEach { lugar ->
                        AccesoLugar(
                            lugar = lugar,
                            onClick = { onSolicitar(lugar.ubicacion.completa) }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                SubtituloSeccion("Elige tu servicio")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TipoServicio.entries.forEach { tipo ->
                        Card(
                            modifier = Modifier.weight(1f).clickable { onSolicitar("") },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tipo.etiqueta,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "desde ${tipo.tarifaBase.soles()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador(
                        modifier = Modifier.weight(1f),
                        valor = state.resumen.viajes.toString(),
                        etiqueta = "Viajes realizados"
                    )
                    Indicador(
                        modifier = Modifier.weight(1f),
                        valor = state.resumen.gastoDelMes.soles(),
                        etiqueta = "Gasto de este mes"
                    )
                }

                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Cupón ${Cupones.VISIBLE}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Descuenta 10% en tu próximo viaje. Aplícalo antes de confirmar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                state.ultimoViaje?.let { viaje ->
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SubtituloSeccion("Tu último viaje")
                        TextButton(onClick = { onPestana(Rutas.HISTORIAL) }) { Text("Ver todos") }
                    }
                    Spacer(Modifier.height(4.dp))
                    TarjetaViaje(viaje = viaje, onClick = { onVerViaje(viaje) })
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onRepetir(viaje) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Repetir este viaje")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AccesoLugar(lugar: LugarGuardado, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(152.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(
                imageVector = iconoDeLugar(lugar.tipo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(lugar.etiqueta, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                text = lugar.ubicacion.completa,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Historial (RF03, RF04)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun HistorialScreen(
    onSolicitar: () -> Unit,
    onVerViaje: (Viaje) -> Unit,
    onPestana: (String) -> Unit,
    viewModel: HistorialViajeViewModel = viewModel(factory = HistorialViajeViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarRango by remember { mutableStateOf(false) }
    val rangoState = rememberDateRangePickerState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Viajes realizados", fontWeight = FontWeight.Bold)
                        Text(
                            "Filtra por servicio o por fecha",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { BarraPasajero(actual = Rutas.HISTORIAL, onSelect = onPestana) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSolicitar,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nuevo viaje", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val ui = state) {
                HistorialUiState.Cargando -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is HistorialUiState.Error -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) { Text(ui.mensaje, style = MaterialTheme.typography.bodyLarge) }

                is HistorialUiState.Exito -> Column(Modifier.fillMaxSize()) {
                    SeccionFiltros(
                        filtro = ui.filtro,
                        onTipoSeleccionado = viewModel::onTipoSeleccionado,
                        onAbrirRango = { mostrarRango = true },
                        onLimpiar = viewModel::onLimpiarFiltros
                    )
                    ResumenResultados(cantidad = ui.viajes.size, total = ui.gastoTotal)
                    if (ui.viajes.isEmpty()) {
                        SinResultados(onLimpiar = viewModel::onLimpiarFiltros)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(ui.viajes, key = { it.id }) { viaje ->
                                TarjetaViaje(viaje = viaje, onClick = { onVerViaje(viaje) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarRango) {
        DatePickerDialog(
            onDismissRequest = { mostrarRango = false },
            confirmButton = {
                TextButton(onClick = {
                    val desde = rangoState.selectedStartDateMillis?.let(::inicioDelDiaLocal)
                    val hasta = rangoState.selectedEndDateMillis
                        ?.let(::inicioDelDiaLocal)
                        ?.plus(DIA_EN_MILLIS - 1)
                    viewModel.onRangoSeleccionado(desde, hasta)
                    mostrarRango = false
                }) { Text("Aplicar rango") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRango = false }) { Text("Cancelar") }
            }
        ) {
            DateRangePicker(
                state = rangoState,
                modifier = Modifier.weight(1f),
                title = {
                    Text(
                        "Elige las fechas del viaje",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SeccionFiltros(
    filtro: FiltroHistorial,
    onTipoSeleccionado: (TipoServicio?) -> Unit,
    onAbrirRango: () -> Unit,
    onLimpiar: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipMove(
                texto = "Todos",
                seleccionado = filtro.tipoServicio == null,
                onClick = { onTipoSeleccionado(null) }
            )
            TipoServicio.entries.forEach { tipo ->
                ChipMove(
                    texto = tipo.etiqueta,
                    seleccionado = filtro.tipoServicio == tipo,
                    onClick = { onTipoSeleccionado(tipo) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onAbrirRango) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (filtro.tieneRango) {
                        "${filtro.desdeMillis.fechaCorta()} a ${filtro.hastaMillis.fechaCorta()}"
                    } else {
                        "Rango de fechas"
                    }
                )
            }
            if (!filtro.vacio) {
                TextButton(onClick = onLimpiar) { Text("Quitar filtros") }
            }
        }
    }
}

@Composable
private fun ResumenResultados(cantidad: Int, total: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (cantidad == 1) "1 viaje" else "$cantidad viajes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Total gastado ${total.soles()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SinResultados(onLimpiar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ningún viaje coincide con estos filtros", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Prueba con otro tipo de servicio o amplía el rango de fechas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLimpiar) { Text("Quitar filtros") }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Solicitar viaje (RF05, RF06)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SolicitarViajeScreen(
    origenInicial: String,
    destinoInicial: String,
    onAtras: () -> Unit,
    onViajeCreado: (String) -> Unit,
    viewModel: SolicitudViajeViewModel = viewModel(factory = SolicitudViajeViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.precargar(origenInicial, destinoInicial) }
    LaunchedEffect(Unit) { viewModel.viajeCreado.collect { id -> onViajeCreado(id) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Solicitar viaje", onAtras = onAtras) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.origen,
                onValueChange = viewModel::onOrigenChange,
                label = { Text("Punto de recojo") },
                placeholder = { Text("Av. Larco 1301, Miraflores") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ChipsDeLugares(lugares = state.lugares, onElegir = viewModel::onOrigenChange)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.destino,
                onValueChange = viewModel::onDestinoChange,
                label = { Text("Destino") },
                placeholder = { Text("Aeropuerto Jorge Chávez, Callao") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ChipsDeLugares(lugares = state.lugares, onElegir = viewModel::onDestinoChange)

            Spacer(Modifier.height(20.dp))
            SubtituloSeccion("Tipo de servicio")
            Spacer(Modifier.height(8.dp))
            SelectorDeServicio(seleccionado = state.tipoServicio, onSelect = viewModel::onTipoChange)

            Spacer(Modifier.height(20.dp))
            SubtituloSeccion("Método de pago")
            Spacer(Modifier.height(8.dp))
            SelectorDePago(seleccionado = state.metodoPago, onSelect = viewModel::onPagoChange)

            Spacer(Modifier.height(20.dp))
            SubtituloSeccion("Cupón de descuento")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.cupon,
                    onValueChange = viewModel::onCuponChange,
                    label = { Text("Código") },
                    placeholder = { Text(Cupones.VISIBLE) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = viewModel::onAplicarCupon,
                    enabled = state.cupon.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) { Text(if (state.cuponAplicado) "Aplicado" else "Aplicar") }
            }

            state.error?.let { mensaje ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = viewModel::onEstimar,
                enabled = state.puedeEstimar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Calcular tarifa") }

            state.estimada?.let { estimada ->
                Spacer(Modifier.height(20.dp))
                TarjetaEstimacion(estimada = estimada, tipo = state.tipoServicio)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = viewModel::onConfirmar,
                    enabled = !state.enviando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = if (state.enviando) "Buscando conductor" else "Confirmar viaje",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChipsDeLugares(lugares: List<LugarGuardado>, onElegir: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lugares.forEach { lugar ->
            ChipMove(
                texto = lugar.etiqueta,
                seleccionado = false,
                onClick = { onElegir(lugar.ubicacion.completa) }
            )
        }
    }
}

@Composable
private fun SelectorDeServicio(seleccionado: TipoServicio, onSelect: (TipoServicio) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipoServicio.entries.forEach { tipo ->
            TarjetaSeleccion(
                modifier = Modifier.weight(1f),
                titulo = tipo.etiqueta,
                subtitulo = tipo.capacidad,
                seleccionada = tipo == seleccionado,
                onClick = { onSelect(tipo) }
            )
        }
    }
}

@Composable
private fun SelectorDePago(seleccionado: MetodoPago, onSelect: (MetodoPago) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetodoPago.entries.forEach { metodo ->
            ChipMove(
                texto = metodo.etiqueta,
                seleccionado = metodo == seleccionado,
                onClick = { onSelect(metodo) }
            )
        }
    }
}

@Composable
private fun TarjetaEstimacion(estimada: TarifaEstimada, tipo: TipoServicio) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = estimada.total.soles(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tarifa estimada en ${tipo.etiqueta}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))
            FilaDato("Distancia", "${estimada.distanciaKm} km")
            FilaDato("Duración estimada", "${estimada.duracionMin} min")
            FilaDato("Subtotal", estimada.subtotal.soles())
            if (estimada.recargo > 1.0) FilaDato("Recargo por hora punta", "x${estimada.recargo}")
            if (estimada.descuento > 0) FilaDato("Descuento del cupón", "- ${estimada.descuento.soles()}")
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Seguimiento (RF07)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SeguimientoScreen(
    viajeId: String,
    onFinalizado: () -> Unit,
    onCancelado: () -> Unit,
    viewModel: SeguimientoViewModel = viewModel(factory = SeguimientoViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmarCancelar by remember { mutableStateOf(false) }
    var verContacto by remember { mutableStateOf(false) }

    LaunchedEffect(viajeId) { viewModel.iniciar(viajeId) }
    LaunchedEffect(state.finalizado) {
        if (state.finalizado) {
            delay(900)
            onFinalizado()
        }
    }
    LaunchedEffect(state.cancelado) { if (state.cancelado) onCancelado() }

    BackHandler(enabled = state.puedeCancelar) { confirmarCancelar = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = state.estado.etiqueta,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (state.minutosRestantes > 0)
                    "${state.estado.detalle}. Faltan unos ${state.minutosRestantes} min"
                else state.estado.detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { state.progreso },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            state.viaje?.let { viaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(iniciales = inicialesDe(viaje.conductor?.nombre ?: "MP"))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = viaje.conductor?.nombre ?: "Asignando conductor",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = viaje.conductor?.let { "${it.vehiculo}, placa ${it.placa}" }
                                        ?: "Buscando unidad cercana",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            InsigniaServicio(viaje.tipoServicio)
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { verContacto = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Llamar")
                            }
                            if (state.puedeCancelar) {
                                OutlinedButton(
                                    onClick = { confirmarCancelar = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("Cancelar") }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        LineaDeRuta(origen = viaje.origen.completa, destino = viaje.destino.completa)
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${viaje.distanciaKm} km, pago con ${viaje.metodoPago.etiqueta}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(viaje.tarifa.soles(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            LineaDeEstados(actual = state.estado)
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmarCancelar) {
        AlertDialog(
            onDismissRequest = { confirmarCancelar = false },
            title = { Text("¿Cancelar el viaje?") },
            text = { Text("El conductor ya fue notificado. Cancelar seguido puede afectar tu cuenta.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCancelar = false
                    viewModel.onCancelar()
                }) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCancelar = false }) { Text("Seguir esperando") }
            }
        )
    }

    if (verContacto) {
        AlertDialog(
            onDismissRequest = { verContacto = false },
            title = { Text("Contacto del conductor") },
            text = { Text(state.viaje?.conductor?.celular ?: "Sin número disponible") },
            confirmButton = {
                TextButton(onClick = { verContacto = false }) { Text("Entendido") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Calificación (RF08)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun CalificacionScreen(
    viajeId: String,
    onListo: () -> Unit,
    viewModel: CalificacionViewModel = viewModel(factory = CalificacionViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viajeId) { viewModel.cargar(viajeId) }
    LaunchedEffect(state.guardada) { if (state.guardada) onListo() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Califica tu viaje", onAtras = onListo) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val viaje = state.viaje
            Text(
                text = "¿Cómo estuvo el viaje con ${viaje?.conductor?.nombre ?: "tu conductor"}?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            viaje?.let {
                Text(
                    text = "Destino ${it.destino.completa}, ${it.tarifa.soles()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(28.dp))
            Estrellas(
                calificacion = state.estrellas,
                tamano = 44.dp,
                onCalificar = viewModel::onEstrellasChange
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = etiquetaDeEstrellas(state.estrellas),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            SubtituloSeccion("Deja propina", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.0, 2.0, 5.0, 10.0).forEach { monto ->
                    ChipMove(
                        texto = if (monto == 0.0) "Sin propina" else monto.soles(),
                        seleccionado = state.propina == monto,
                        onClick = { viewModel.onPropinaChange(monto) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = state.comentario,
                onValueChange = viewModel::onComentarioChange,
                label = { Text("Comentario (opcional)") },
                placeholder = { Text("Cuéntanos qué tal estuvo el servicio") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::onEnviar,
                enabled = viaje != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Enviar calificación", fontWeight = FontWeight.Bold) }

            TextButton(onClick = onListo) { Text("Ahora no") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Detalle del viaje
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun DetalleViajeScreen(
    viajeId: String,
    onAtras: () -> Unit,
    onRepetir: (Viaje) -> Unit,
    onCalificar: () -> Unit,
    viewModel: DetalleViajeViewModel = viewModel(factory = DetalleViajeViewModel.Factory)
) {
    val viaje by viewModel.viaje.collectAsStateWithLifecycle()

    LaunchedEffect(viajeId) { viewModel.cargar(viajeId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Detalle del viaje", onAtras = onAtras) }
    ) { padding ->
        val actual = viaje
        if (actual == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(actual.fechaMillis.fechaLarga(), style = MaterialTheme.typography.labelLarge)
                        InsigniaServicio(actual.tipoServicio)
                    }
                    Spacer(Modifier.height(16.dp))
                    LineaDeRuta(origen = actual.origen.completa, destino = actual.destino.completa)
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    FilaDato("Código", actual.id)
                    FilaDato("Estado", actual.estado.etiqueta)
                    FilaDato("Conductor", actual.conductor?.nombre ?: "Sin asignar")
                    actual.conductor?.let {
                        FilaDato("Vehículo", "${it.vehiculo}, placa ${it.placa}")
                    }
                    FilaDato("Distancia", "${actual.distanciaKm} km")
                    FilaDato("Duración", "${actual.duracionMin} min")
                    FilaDato("Pago", actual.metodoPago.etiqueta)
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Boleta", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    FilaDato("Tarifa del viaje", actual.tarifa.soles())
                    if (actual.descuento > 0) FilaDato("Descuento aplicado", "- ${actual.descuento.soles()}")
                    actual.calificacion?.propina?.takeIf { it > 0 }?.let {
                        FilaDato("Propina", it.soles())
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text(
                            text = actual.total.soles(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Tu calificación", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    val calificacion = actual.calificacion
                    if (calificacion != null) {
                        Estrellas(calificacion = calificacion.estrellas, tamano = 22.dp)
                        if (calificacion.comentario.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(calificacion.comentario, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Text(
                            "Todavía no calificaste este viaje.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (actual.estado == EstadoViaje.FINALIZADO) {
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = onCalificar, shape = RoundedCornerShape(12.dp)) {
                                Text("Calificar ahora")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onRepetir(actual) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Repetir este viaje", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Panel del conductor (RF09)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ConductorDashboardScreen(
    onVerViaje: (Viaje) -> Unit,
    onPestana: (String) -> Unit,
    viewModel: ConductorViewModel = viewModel(factory = ConductorViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SalirDeLaApp()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BarraConductor(actual = Rutas.CONDUCTOR_PANEL, onSelect = onPestana) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(iniciales = inicialesDe(state.conductor?.nombre ?: "MP"))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(saludo(), style = MaterialTheme.typography.bodySmall, color = AguaClara)
                        Text(
                            text = state.conductor?.nombre ?: "Conductor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        state.conductor?.let {
                            Text(
                                text = "${it.vehiculo}, placa ${it.placa}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AguaClara
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (state.disponible) "Disponible" else "Desconectado",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (state.disponible) "Recibiendo solicitudes"
                                else "No te llegarán nuevos viajes",
                                style = MaterialTheme.typography.labelMedium,
                                color = AguaClara
                            )
                        }
                        Switch(
                            checked = state.disponible,
                            onCheckedChange = viewModel::onDisponibilidadChange
                        )
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador(
                        modifier = Modifier.weight(1f),
                        valor = state.asignados.size.toString(),
                        etiqueta = "Viajes activos"
                    )
                    Indicador(
                        modifier = Modifier.weight(1f),
                        valor = state.gananciaDelDia.soles(),
                        etiqueta = "Ganancia de hoy"
                    )
                    Indicador(
                        modifier = Modifier.weight(1f),
                        valor = String.format(Locale.US, "%.1f", state.conductor?.calificacion ?: 0.0),
                        etiqueta = "Calificación"
                    )
                }

                Spacer(Modifier.height(24.dp))
                SubtituloSeccion("Viajes asignados")
                Spacer(Modifier.height(8.dp))

                if (state.asignados.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("No tienes viajes activos", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Cuando un pasajero confirme un viaje, aparecerá aquí.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    state.asignados.forEach { viaje ->
                        TarjetaViajeConductor(
                            viaje = viaje,
                            onVerDetalle = { onVerViaje(viaje) },
                            onAvanzar = { viewModel.onAvanzarEstado(viaje) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))
                SubtituloSeccion("Últimos viajes atendidos")
                Spacer(Modifier.height(8.dp))
                if (state.terminados.isEmpty()) {
                    Text(
                        "Todavía no has cerrado ningún viaje.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.terminados.forEach { viaje ->
                        TarjetaViaje(viaje = viaje, onClick = { onVerViaje(viaje) })
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TarjetaViajeConductor(
    viaje: Viaje,
    onVerDetalle: () -> Unit,
    onAvanzar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onVerDetalle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(viaje.pasajeroNombre, fontWeight = FontWeight.SemiBold)
                InsigniaEstado(viaje.estado)
            }
            Spacer(Modifier.height(12.dp))
            LineaDeRuta(origen = viaje.origen.completa, destino = viaje.destino.completa)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${viaje.tipoServicio.etiqueta}, ${viaje.distanciaKm} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(viaje.tarifa.soles(), fontWeight = FontWeight.Bold)
                }
                viaje.estado.siguiente?.let { siguiente ->
                    Button(onClick = onAvanzar, shape = RoundedCornerShape(12.dp)) {
                        Text(accionParaEstado(siguiente))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Detalle del conductor (RF10)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ConductorDetalleScreen(
    viajeId: String,
    onAtras: () -> Unit,
    detalleViewModel: DetalleViajeViewModel = viewModel(factory = DetalleViajeViewModel.Factory),
    conductorViewModel: ConductorViewModel = viewModel(factory = ConductorViewModel.Factory)
) {
    val viaje by detalleViewModel.viaje.collectAsStateWithLifecycle()
    var confirmarCancelar by remember { mutableStateOf(false) }

    LaunchedEffect(viajeId) { detalleViewModel.cargar(viajeId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Viaje asignado", onAtras = onAtras) }
    ) { padding ->
        val actual = viaje
        if (actual == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(iniciales = inicialesDe(actual.pasajeroNombre))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(actual.pasajeroNombre, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = actual.fechaMillis.fechaLarga(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        InsigniaEstado(actual.estado)
                    }
                    Spacer(Modifier.height(16.dp))
                    LineaDeRuta(origen = actual.origen.completa, destino = actual.destino.completa)
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    FilaDato("Código", actual.id)
                    FilaDato("Servicio", actual.tipoServicio.etiqueta)
                    FilaDato("Distancia", "${actual.distanciaKm} km")
                    FilaDato("Duración", "${actual.duracionMin} min")
                    FilaDato("Cobro", "${actual.tarifa.soles()} en ${actual.metodoPago.etiqueta}")
                }
            }

            Spacer(Modifier.height(20.dp))
            SubtituloSeccion("Estado del viaje")
            Spacer(Modifier.height(10.dp))
            LineaDeEstados(actual = actual.estado)

            Spacer(Modifier.height(20.dp))

            // RF10: el conductor mueve el viaje al siguiente estado
            val siguiente = actual.estado.siguiente
            if (siguiente != null) {
                Button(
                    onClick = { conductorViewModel.onAvanzarEstado(actual) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) { Text(accionParaEstado(siguiente), fontWeight = FontWeight.Bold) }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { confirmarCancelar = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Cancelar viaje") }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Viaje ${actual.estado.etiqueta.lowercase()}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Ya no hay más cambios de estado pendientes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmarCancelar) {
        val actual = viaje
        AlertDialog(
            onDismissRequest = { confirmarCancelar = false },
            title = { Text("¿Cancelar este viaje?") },
            text = { Text("El pasajero será notificado de inmediato.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCancelar = false
                    actual?.let { conductorViewModel.onCancelar(it) }
                    onAtras()
                }) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCancelar = false }) { Text("Volver") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — Perfil
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun PerfilScreen(
    onSesionCerrada: () -> Unit,
    onPestanaPasajero: (String) -> Unit,
    onPestanaConductor: (String) -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var agregarLugar by remember { mutableStateOf(false) }
    var confirmarSalida by remember { mutableStateOf(false) }
    val esConductor = state.sesion?.rol == Rol.CONDUCTOR

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (esConductor) BarraConductor(actual = Rutas.PERFIL, onSelect = onPestanaConductor)
            else BarraPasajero(actual = Rutas.PERFIL, onSelect = onPestanaPasajero)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(iniciales = state.sesion?.iniciales ?: "MP", tamano = 72.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.sesion?.nombre ?: "Invitado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = state.sesion?.correo ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AguaClara
                )
                Text(
                    text = state.sesion?.rol?.etiqueta ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = AguaClara
                )
            }

            Column(Modifier.padding(16.dp)) {

                if (esConductor) {
                    val conductor = state.sesion?.conductor
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Tu unidad", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            FilaDato("Vehículo", conductor?.vehiculo ?: "Sin registrar")
                            FilaDato("Placa", conductor?.placa ?: "Sin registrar")
                            FilaDato("Celular", conductor?.celular ?: "Sin registrar")
                            FilaDato(
                                "Calificación",
                                String.format(Locale.US, "%.1f", conductor?.calificacion ?: 0.0)
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Indicador(
                            modifier = Modifier.weight(1f),
                            valor = state.resumen.viajes.toString(),
                            etiqueta = "Viajes"
                        )
                        Indicador(
                            modifier = Modifier.weight(1f),
                            valor = state.resumen.gastoTotal.soles(),
                            etiqueta = "Gastado"
                        )
                        Indicador(
                            modifier = Modifier.weight(1f),
                            valor = String.format(Locale.US, "%.1f", state.resumen.calificacionPromedio),
                            etiqueta = "Promedio"
                        )
                    }

                    state.resumen.servicioFavorito?.let { favorito ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Tu servicio más usado es ${favorito.etiqueta}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SubtituloSeccion("Lugares guardados")
                        TextButton(onClick = { agregarLugar = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }

                    state.lugares.forEach { lugar ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = iconoDeLugar(lugar.tipo),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(lugar.etiqueta, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = lugar.ubicacion.completa,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.onQuitarLugar(lugar.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar ${lugar.etiqueta}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SubtituloSeccion("Método de pago preferido")
                    Spacer(Modifier.height(8.dp))
                    SelectorDePago(seleccionado = state.pago, onSelect = viewModel::onPagoChange)
                }

                Spacer(Modifier.height(28.dp))
                OutlinedButton(
                    onClick = { confirmarSalida = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (agregarLugar) {
        var etiqueta by remember { mutableStateOf("") }
        var direccion by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { agregarLugar = false },
            title = { Text("Nuevo lugar guardado") },
            text = {
                Column {
                    OutlinedTextField(
                        value = etiqueta,
                        onValueChange = { etiqueta = it },
                        label = { Text("Nombre") },
                        placeholder = { Text("Universidad") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección") },
                        placeholder = { Text("Av. Javier Prado Este 4600, Surco") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onAgregarLugar(etiqueta, direccion)
                    agregarLugar = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { agregarLugar = false }) { Text("Cancelar") }
            }
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Volverás a la pantalla de inicio de sesión.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    viewModel.onCerrarSesion()
                    onSesionCerrada()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Quedarme") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW — componentes compartidos
// ══════════════════════════════════════════════════════════════════════════════

/** En la pantalla principal de cada rol, el botón atrás sale de la app. */
@Composable
private fun SalirDeLaApp() {
    val contexto = LocalContext.current
    BackHandler { (contexto as? Activity)?.finish() }
}

@Composable
private fun BarraSuperior(titulo: String, onAtras: () -> Unit) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            IconButton(onClick = onAtras) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
private fun BarraPasajero(actual: String, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        ItemBarra(actual == Rutas.INICIO, "Inicio", Icons.Default.Home) { onSelect(Rutas.INICIO) }
        ItemBarra(actual == Rutas.HISTORIAL, "Viajes", Icons.AutoMirrored.Filled.List) {
            onSelect(Rutas.HISTORIAL)
        }
        ItemBarra(actual == Rutas.PERFIL, "Perfil", Icons.Default.Person) { onSelect(Rutas.PERFIL) }
    }
}

@Composable
private fun BarraConductor(actual: String, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        ItemBarra(actual == Rutas.CONDUCTOR_PANEL, "Panel", Icons.AutoMirrored.Filled.List) {
            onSelect(Rutas.CONDUCTOR_PANEL)
        }
        ItemBarra(actual == Rutas.PERFIL, "Perfil", Icons.Default.Person) { onSelect(Rutas.PERFIL) }
    }
}

@Composable
private fun RowScope.ItemBarra(
    seleccionado: Boolean,
    etiqueta: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = seleccionado,
        onClick = onClick,
        icon = { Icon(icono, contentDescription = null) },
        label = { Text(etiqueta) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
private fun SubtituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Indicador(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaSeleccion(
    titulo: String,
    subtitulo: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionada) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (seleccionada) 2.dp else 1.dp,
            color = if (seleccionada) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Avatar(iniciales: String, tamano: Dp = 48.dp, onClick: (() -> Unit)? = null) {
    val clic = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Box(
        modifier = Modifier
            .size(tamano)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .then(clic),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ChipMove(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun InsigniaServicio(tipo: TipoServicio) {
    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            text = tipo.etiqueta,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InsigniaEstado(estado: EstadoViaje) {
    val fondo = when (estado) {
        EstadoViaje.FINALIZADO -> MaterialTheme.colorScheme.primaryContainer
        EstadoViaje.CANCELADO -> MaterialTheme.colorScheme.surfaceVariant
        EstadoViaje.INICIADO -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val texto = when (estado) {
        EstadoViaje.FINALIZADO -> MaterialTheme.colorScheme.onPrimaryContainer
        EstadoViaje.CANCELADO -> MaterialTheme.colorScheme.onSurfaceVariant
        EstadoViaje.INICIADO -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(shape = RoundedCornerShape(999.dp), color = fondo) {
        Text(
            text = estado.etiqueta,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = texto
        )
    }
}

/** Origen y destino unidos por una línea vertical, como en el ticket de un viaje. */
@Composable
private fun LineaDeRuta(origen: String, destino: String) {
    Row(verticalAlignment = Alignment.Top) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 5.dp)
        ) {
            Box(Modifier.size(9.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
            Box(
                Modifier.width(2.dp).height(22.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Box(Modifier.size(9.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = origen,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = destino,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LineaDeEstados(actual: EstadoViaje) {
    if (actual == EstadoViaje.CANCELADO) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "El viaje fue cancelado.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val indiceActual = EstadoViaje.flujo.indexOf(actual)
    Column {
        EstadoViaje.flujo.forEachIndexed { indice, estado ->
            val hecho = indice < indiceActual
            val activo = indice == indiceActual
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(38.dp)) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            color = when {
                                hecho -> MaterialTheme.colorScheme.tertiary
                                activo -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hecho) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = estado.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                        color = if (indice <= indiceActual) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (activo) {
                        Text(
                            text = estado.detalle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Estrellas(
    calificacion: Int,
    tamano: Dp,
    maximo: Int = 5,
    onCalificar: ((Int) -> Unit)? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (1..maximo).forEach { posicion ->
            val clic = if (onCalificar != null) {
                Modifier.clickable { onCalificar(posicion) }
            } else {
                Modifier
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = if (onCalificar != null) "Calificar con $posicion estrellas" else null,
                tint = if (posicion <= calificacion) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(tamano).then(clic)
            )
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TarjetaViaje(viaje: Viaje, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viaje.fechaMillis.fechaLarga(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                InsigniaServicio(viaje.tipoServicio)
            }

            Spacer(Modifier.height(12.dp))
            LineaDeRuta(origen = viaje.origen.completa, destino = viaje.destino.completa)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = viaje.conductor?.nombre ?: "Sin conductor",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val calificacion = viaje.calificacion
                    when {
                        viaje.estado != EstadoViaje.FINALIZADO -> Text(
                            text = viaje.estado.etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (viaje.estado == EstadoViaje.CANCELADO)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.tertiary
                        )
                        calificacion != null -> Estrellas(
                            calificacion = calificacion.estrellas,
                            tamano = 14.dp
                        )
                        else -> Text(
                            text = "Toca para calificar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Text(
                    text = viaje.total.soles(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  UTILIDADES
// ══════════════════════════════════════════════════════════════════════════════

private const val DIA_EN_MILLIS = 24L * 60 * 60 * 1000

private val formatoLargo = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-PE"))
private val formatoCorto = SimpleDateFormat("dd/MM/yy", Locale.forLanguageTag("es-PE"))
private val formatoMes = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-PE"))

private fun Long.fechaLarga(): String = formatoLargo.format(Date(this))

private fun Long?.fechaCorta(): String = this?.let { formatoCorto.format(Date(it)) } ?: "inicio"

private fun Double.soles(): String = "S/ " + String.format(Locale.US, "%.2f", this)

private fun Double.redondear(): Double = (this * 10).roundToInt() / 10.0

private fun mesActual(): String =
    formatoMes.format(Date()).replaceFirstChar { it.uppercaseChar() }

private fun saludo(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}

private fun inicialesDe(nombre: String): String = nombre.trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")

private fun etiquetaDeEstrellas(estrellas: Int): String = when (estrellas) {
    1 -> "Muy malo"
    2 -> "Malo"
    3 -> "Regular"
    4 -> "Bueno"
    else -> "Excelente"
}

/** Texto del botón con el que el conductor mueve el viaje al siguiente estado. */
private fun accionParaEstado(siguiente: EstadoViaje): String = when (siguiente) {
    EstadoViaje.ACEPTADO -> "Aceptar viaje"
    EstadoViaje.EN_CAMINO -> "Voy en camino"
    EstadoViaje.INICIADO -> "Iniciar viaje"
    EstadoViaje.FINALIZADO -> "Finalizar viaje"
    else -> "Actualizar"
}

private fun iconoDeLugar(tipo: TipoLugar) = when (tipo) {
    TipoLugar.CASA -> Icons.Default.Home
    TipoLugar.TRABAJO -> Icons.Default.Place
    TipoLugar.OTRO -> Icons.Default.LocationOn
}

private fun inicioDeHoy(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/** El DateRangePicker devuelve medianoche UTC; lo pasamos a medianoche local. */
private fun inicioDelDiaLocal(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    val local = Calendar.getInstance().apply {
        set(Calendar.YEAR, utc.get(Calendar.YEAR))
        set(Calendar.MONTH, utc.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return local.timeInMillis
}

/* ══════════════════════════════════════════════════════════════════════════════
 *  DEPENDENCIAS (app/build.gradle.kts)
 *
 *  implementation(platform("androidx.compose:compose-bom:2025.02.00"))
 *  implementation("androidx.compose.ui:ui")
 *  implementation("androidx.compose.ui:ui-graphics")
 *  implementation("androidx.compose.material3:material3")
 *  implementation("androidx.activity:activity-compose:1.10.0")
 *  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 *  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 *  implementation("androidx.navigation:navigation-compose:2.8.7")
 *
 *  CUENTAS DE PRUEBA (autenticación simulada; sirve cualquier correo válido
 *  con clave de 6+ caracteres, estas solo son un atajo)
 *  Pasajero:  arny.saravia@moveperu.pe   / moveperu2026
 *  Conductor: carlos.mamani@moveperu.pe  / moveperu2026
 *
 *  ── Migración a Clean Architecture con Hilt ──
 *  1. Corta cada bloque a su paquete: domain/model, domain/repository,
 *     domain/usecase, data/repository, presentation/<pantalla>, navigation, ui/theme.
 *  2. Borra ServiceLocator y los `companion object { val Factory }`.
 *  3. Anota: @HiltAndroidApp en la Application, @AndroidEntryPoint en MainActivity,
 *     @HiltViewModel + @Inject constructor en cada ViewModel, @Inject constructor en
 *     los casos de uso y en los RepositoryImpl, y un @Module con @Binds para cada
 *     interfaz de repositorio.
 *  4. Cambia `viewModel(factory = ...)` por `hiltViewModel()`.
 *  5. Recién ahí reemplaza las implementaciones en memoria por Room + Retrofit y
 *     guarda la sesión en DataStore. Ni los ViewModels ni la UI cambian: para eso
 *     existen las interfaces de repositorio.
 * ══════════════════════════════════════════════════════════════════════════════ */