package ni.uni.nanobots.sentinel.domain.hogar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ni.uni.nanobots.sentinel.comun.Ids
import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Contenido
import ni.uni.nanobots.sentinel.domain.modelo.Equipo
import ni.uni.nanobots.sentinel.domain.modelo.Hogar
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Pendiente
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Temas
import ni.uni.nanobots.sentinel.domain.puertos.Efectos
import ni.uni.nanobots.sentinel.domain.puertos.Mensajeria
import ni.uni.nanobots.sentinel.domain.puertos.Reloj

/** Código de vinculación que muestra el administrador. */
data class Vinculacion(
    val rol: Rol,
    val codigo: String,
    val vence: Long,
    val unidos: List<String> = emptyList(),
    /** Vecino pendiente al que se le envía la invitación por WhatsApp. */
    val para: Pendiente? = null,
)

sealed interface EstadoUnion {
    data object Inactiva : EstadoUnion
    data object Esperando : EstadoUnion
    data class Error(val mensaje: String) : EstadoUnion
}

/**
 * Vinculación por código de 6 números (vence en 5 min):
 * el equipo nuevo pide unirse («solicitud») y el administrador responde con el hogar («bienvenida»).
 */
class Vinculador(
    private val repo: RepositorioEstado,
    private val red: Mensajeria,
    private val coord: Coordinador,
    private val efectos: Efectos,
    private val reloj: Reloj,
    private val scope: CoroutineScope,
) {
    private val _vinculacion = MutableStateFlow<Vinculacion?>(null)
    val vinculacion: StateFlow<Vinculacion?> = _vinculacion.asStateFlow()

    private val _union = MutableStateFlow<EstadoUnion>(EstadoUnion.Inactiva)
    val union: StateFlow<EstadoUnion> = _union.asStateFlow()

    private var trabajoVinc: Job? = null
    private var trabajoUnion: Job? = null

    // ---------- administrador ----------

    /** Crea el hogar en este equipo, que queda como administrador. Devuelve un error o null. */
    fun crearHogar(casa: String, nombre: String, pin: String): String? {
        val p = pin.filter(Char::isDigit)
        if (p.length != 4) return "El PIN debe tener 4 números."
        repo.actualizar {
            it.copy(
                yo = it.yo.copy(rol = Rol.RECEPTOR, nombre = nombre.trim().ifBlank { "Mi celular" }),
                hogar = Hogar(Ids.nuevo(12), casa.trim().ifBlank { "Mi casa" }, p),
                admin = true,
                cfg = Config(v = reloj.ahora()),
            )
        }
        coord.arrancar()
        return null
    }

    fun abrir(rol: Rol, para: Pendiente? = null) {
        cerrar()
        val v = Vinculacion(rol, Ids.codigo6(), reloj.ahora() + VIGENCIA_MS, para = para)
        _vinculacion.value = v
        if (repo.estado.value.demo) return
        val tema = Temas.vinculo(v.codigo)
        trabajoVinc = scope.launch {
            launch {
                while (isActive && reloj.ahora() < v.vence) delay(1_000)
                // venció: se genera otro código con el mismo rol
                _vinculacion.value?.let { actual -> scope.launch { abrir(actual.rol, actual.para) } }
            }
            coord.escuchar(tema, null).collect { m ->
                val s = m.contenido as? Contenido.Solicitud ?: return@collect
                val est = repo.estado.value
                val hogar = est.hogar ?: return@collect
                coord.enviar(tema, Contenido.Bienvenida(m.de, hogar, est.cfg))
                if (_vinculacion.value?.unidos?.contains(m.de) == true) return@collect
                _vinculacion.update { it?.copy(unidos = it.unidos + m.de) }
                val nombre = s.nombre.ifBlank { "Equipo" }
                repo.actualizar { e ->
                    val prev = e.equipos[m.de]
                    val eq = (prev ?: Equipo(id = m.de, rol = s.rol, nombre = nombre)).copy(
                        rol = s.rol, nombre = nombre, lugar = s.lugar, ref = s.ref, visto = reloj.ahora(),
                    )
                    val pendientes = if (s.rol == Rol.VECINO) {
                        e.pendientes.filterNot { it.nombre.equals(nombre, ignoreCase = true) }
                    } else {
                        e.pendientes
                    }
                    e.copy(equipos = e.equipos + (m.de to eq), pendientes = pendientes)
                }
                efectos.vibrar(100)
                efectos.bip(990, 120, 0.25f)
                val como = when (s.rol) {
                    Rol.SENTINELA -> "Sentinela"
                    Rol.RECEPTOR -> "Receptor"
                    Rol.VECINO -> "vecino de confianza"
                }
                coord.avisar("$nombre se unió como $como", TipoAviso.CHECK)
            }
        }
    }

    /** El código no depende del rol: la solicitud dice qué es el equipo nuevo. */
    fun cambiarRol(rol: Rol) = _vinculacion.update { it?.copy(rol = rol) }

    fun cerrar() {
        trabajoVinc?.cancel()
        trabajoVinc = null
        _vinculacion.value = null
    }

    // ---------- equipo nuevo ----------

    fun unirse(rol: Rol, codigo: String, nombre: String, ref: String, lugar: Lugar) {
        val c = codigo.filter(Char::isDigit)
        if (c.length != 6) return error("Escribe los 6 números del código.")
        if (nombre.isBlank()) return error("Ponle un nombre a este equipo.")
        repo.actualizar { it.copy(yo = it.yo.copy(rol = rol, nombre = nombre.trim(), ref = ref.trim(), lugar = lugar)) }
        _union.value = EstadoUnion.Esperando
        trabajoUnion?.cancel()
        val tema = Temas.vinculo(c)
        trabajoUnion = scope.launch {
            var hecho = false
            val escucha = launch {
                coord.escuchar(tema, null).collect { m ->
                    val b = m.contenido as? Contenido.Bienvenida ?: return@collect
                    if (hecho || b.para != repo.estado.value.yo.id) return@collect
                    hecho = true
                    repo.actualizar { it.copy(hogar = b.hogar, cfg = b.cfg, admin = false) }
                    _union.value = EstadoUnion.Inactiva
                    coord.avisar("Listo: unido a «${b.hogar.nombre}»", TipoAviso.CHECK)
                    efectos.vibrar(100)
                    coord.arrancar()
                    trabajoUnion?.cancel()
                }
            }
            withTimeoutOrNull(4_000) { red.conectado.first { it == true } }
            repeat(INTENTOS) {
                if (hecho) return@launch
                val yo = repo.estado.value.yo
                coord.enviar(tema, Contenido.Solicitud(rol, yo.nombre, yo.lugar, yo.ref))
                delay(ENTRE_INTENTOS_MS)
            }
            if (!hecho) {
                escucha.cancel()
                error(
                    "No hubo respuesta. Revisa que el código siga en pantalla en el celular de la familia " +
                        "(vence en 5 min) y que ambos tengan internet.",
                )
            }
        }
    }

    fun limpiarError() {
        if (_union.value is EstadoUnion.Error) _union.value = EstadoUnion.Inactiva
    }

    fun cancelarUnion() {
        trabajoUnion?.cancel()
        _union.value = EstadoUnion.Inactiva
    }

    private fun error(mensaje: String) {
        _union.value = EstadoUnion.Error(mensaje)
    }

    private companion object {
        const val VIGENCIA_MS = 5 * 60_000L
        const val INTENTOS = 5
        const val ENTRE_INTENTOS_MS = 5_000L
    }
}
