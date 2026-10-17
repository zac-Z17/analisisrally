package ni.uni.nanobots.sentinel.ui.receptor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.di.Contenedor
import ni.uni.nanobots.sentinel.domain.hogar.Vinculacion
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.Nivel
import ni.uni.nanobots.sentinel.domain.modelo.RELAY_DEF
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.ui.relojUi
import java.net.URLEncoder

enum class Pestana(val titulo: String) {
    INICIO("Inicio"), EQUIPOS("Equipos"), HORARIO("Horario"), VECINOS("Vecinos"), ALERTAS("Alertas")
}

enum class Filtro(val texto: String) { TODAS("Todas"), INTENTOS("Intentos"), LEVES("Leves"), SISTEMA("Sistema") }

/** Paneles que se abren sobre las pestañas. */
sealed interface Hoja {
    data object Vincular : Hoja
    data class Detalle(val id: String) : Hoja
    data object Ajustes : Hoja
    data object Contacto : Hoja
}

data class UiReceptor(
    val pestana: Pestana = Pestana.INICIO,
    val hoja: Hoja? = null,
    val filtro: Filtro = Filtro.TODAS,
    /** Qué se está confirmando: «desv:<id>» o «salir». */
    val confirmar: String? = null,
)

/** Pantallas del Receptor (y del Vecino, que usa una parte). */
class ReceptorViewModel(private val c: Contenedor) : ViewModel() {
    val estado: StateFlow<EstadoApp> = c.repo.estado
    val conectado: StateFlow<Boolean?> = c.coordinador.conectado
    val vinculacion: StateFlow<Vinculacion?> = c.vinculador.vinculacion
    val ahora: StateFlow<Long> = relojUi(c)

    private val _ui = MutableStateFlow(UiReceptor())
    val ui: StateFlow<UiReceptor> = _ui.asStateFlow()

    private val co = c.coordinador

    // navegación
    fun pestana(p: Pestana) = _ui.update { it.copy(pestana = p, hoja = null, confirmar = null) }
    fun filtro(f: Filtro) = _ui.update { it.copy(filtro = f) }
    fun abrirDetalle(id: String) = _ui.update { it.copy(hoja = Hoja.Detalle(id), confirmar = null) }
    fun abrirAjustes() = _ui.update { it.copy(hoja = Hoja.Ajustes, confirmar = null) }
    fun abrirContacto() = _ui.update { it.copy(hoja = Hoja.Contacto) }
    fun confirmar(que: String?) = _ui.update { it.copy(confirmar = que) }

    fun cerrarHoja() {
        if (_ui.value.hoja == Hoja.Vincular) c.vinculador.cerrar()
        _ui.update { it.copy(hoja = null, confirmar = null) }
    }

    // vinculación
    fun vincular(rol: Rol = Rol.SENTINELA) {
        c.vinculador.abrir(rol)
        _ui.update { it.copy(hoja = Hoja.Vincular) }
    }

    fun invitarPendiente(id: String) {
        c.vinculador.abrir(Rol.VECINO, estado.value.pendientes.firstOrNull { it.id == id })
        _ui.update { it.copy(hoja = Hoja.Vincular) }
    }

    fun rolVinculo(rol: Rol) = c.vinculador.cambiarRol(rol)

    /** Enlace del QR: abre el prototipo web si el otro equipo no tiene la app; la app lee el código. */
    fun enlaceUnir(v: Vinculacion): String {
        val relay = estado.value.relay
        val consulta = if (relay != RELAY_DEF) "?relay=" + URLEncoder.encode(relay, "UTF-8") else ""
        return "$PAGINA_WEB$consulta#unir-${v.codigo}-${v.rol.clave.first()}"
    }

    fun enlaceWhatsApp(v: Vinculacion): String {
        val casa = estado.value.hogar?.nombre.orEmpty()
        val msj = "Te invito a ser vecino(a) de confianza de «$casa» en Sentinel. Abre este enlace y usa el código " +
            "${v.codigo} (vence en 5 min): ${enlaceUnir(v)}"
        val tel = v.para?.tel.orEmpty().filter(Char::isDigit)
        return "https://wa.me/$tel?text=" + URLEncoder.encode(msj, "UTF-8").replace("+", "%20")
    }

    // vigilancia
    fun pausar() = co.pausar()
    fun reanudar() = co.reanudar()
    fun detener() = co.detenerVigilancia()
    fun activar() = co.activarVigilancia()
    fun volverAlHorario() = co.volverAlHorario()

    // horario
    fun alternar(campo: CampoCfg) = co.cambiarCfg {
        when (campo) {
            CampoCfg.AUTO -> it.copy(auto = !it.auto)
            CampoCfg.CARGA -> it.copy(carga = !it.carga)
            CampoCfg.AVISO10 -> it.copy(aviso10 = !it.aviso10)
            CampoCfg.AVISO_TEMP -> it.copy(avisoTemp = !it.avisoTemp)
            CampoCfg.FUERA -> it.copy(fuera = !it.fuera)
        }
    }

    fun hora(campo: CampoHora, minutos: Int) = co.cambiarCfg {
        val v = Formato.hhmm(minutos)
        when (campo) {
            CampoHora.INICIO -> it.copy(inicio = v)
            CampoHora.FIN -> it.copy(fin = v)
            CampoHora.CARGA_INI -> it.copy(cargaIni = v)
            CampoHora.CARGA_FIN -> it.copy(cargaFin = v)
        }
    }

    fun dia(i: Int) = co.cambiarCfg { it.copy(dias = it.dias.mapIndexed { j, d -> if (i == j) !d else d }) }
    fun batMin(delta: Int) = co.cambiarCfg { it.copy(batMin = (it.batMin + delta).coerceIn(20, 90)) }

    // equipos
    fun nivel(id: String, n: Nivel) = co.cambiarNivel(id, n)
    fun sirena(id: String) = co.alternarSirena(id)
    fun probar(id: String) = co.probarEquipo(id)
    fun desvincular(id: String) {
        co.desvincularEquipo(id)
        _ui.update { it.copy(hoja = null, confirmar = null) }
    }

    // vecinos
    fun alternarVecino(id: String) = co.alternarVecino(id)
    fun quitarPendiente(id: String) = co.quitarPendiente(id)
    fun agregarPendiente(nombre: String, tel: String, ref: String): Boolean {
        if (nombre.isBlank()) {
            co.avisar("Escribe el nombre del vecino")
            return false
        }
        co.agregarPendiente(nombre.trim(), tel.trim(), ref.trim())
        _ui.update { it.copy(hoja = null) }
        return true
    }

    // alertas
    fun etiquetar(mid: String, que: Etiqueta) = co.etiquetar(mid, que)
    fun simularIntento() = co.simularIntento()

    // ajustes
    fun guardarAjustes(nombre: String, casa: String, pin: String, relay: String) {
        co.guardarAjustes(nombre, casa, pin, relay)
        _ui.update { it.copy(hoja = null) }
    }

    fun probarSonido() = c.probarSonido()

    fun salir() {
        _ui.value = UiReceptor()
        c.vinculador.cerrar()
        co.salir()
    }

    fun avisar(texto: String) = co.avisar(texto)

    companion object {
        const val PAGINA_WEB = "https://zac-z17.github.io/analisisrally/sentinel/prototipo-web/"
    }
}

enum class CampoCfg { AUTO, CARGA, AVISO10, AVISO_TEMP, FUERA }
enum class CampoHora { INICIO, FIN, CARGA_INI, CARGA_FIN }
