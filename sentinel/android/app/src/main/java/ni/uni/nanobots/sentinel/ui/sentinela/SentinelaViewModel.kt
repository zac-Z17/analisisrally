package ni.uni.nanobots.sentinel.ui.sentinela

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ni.uni.nanobots.sentinel.di.Contenedor
import ni.uni.nanobots.sentinel.domain.hogar.DatosLocales
import ni.uni.nanobots.sentinel.domain.hogar.TipoAviso
import ni.uni.nanobots.sentinel.domain.modelo.Calibracion
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import ni.uni.nanobots.sentinel.domain.puertos.Lectura
import ni.uni.nanobots.sentinel.domain.reglas.Calibrador
import ni.uni.nanobots.sentinel.domain.reglas.EstadoCalibracion
import ni.uni.nanobots.sentinel.domain.reglas.Simulacion
import ni.uni.nanobots.sentinel.domain.reglas.Umbrales
import ni.uni.nanobots.sentinel.ui.relojUi

data class UiSentinela(
    val ajustes: Boolean = false,
    val pin: String = "",
    val pinOk: Boolean = false,
    val confirmarSalir: Boolean = false,
)

class SentinelaViewModel(private val c: Contenedor) : ViewModel() {
    val estado: StateFlow<EstadoApp> = c.repo.estado
    val conectado: StateFlow<Boolean?> = c.coordinador.conectado
    val local: StateFlow<DatosLocales> = c.coordinador.local
    val vigilancia: StateFlow<Vigilancia> = c.coordinador.vigilancia
    val pruebaHasta: StateFlow<Long> = c.coordinador.pruebaHasta
    val sirenaSonando: StateFlow<Boolean> = c.sirenaSonando
    val ahora: StateFlow<Long> = relojUi(c, 500)
    val lecturas: Flow<Lectura> = c.sensores.lecturas
    val tieneAcelerometro = c.sensores.tieneAcelerometro
    val tieneGiroscopio = c.sensores.tieneGiroscopio

    private val _ui = MutableStateFlow(UiSentinela())
    val ui: StateFlow<UiSentinela> = _ui.asStateFlow()

    fun umbrales(e: EstadoApp): Umbrales = Umbrales.de(e.cal, e.cfg.ajusteDe(e.yo.id))

    // ---------- pantalla principal ----------

    fun probar() = c.coordinador.probarLocal()
    fun apagarSirena() = c.callar()

    // ---------- ajustes con PIN ----------

    fun abrirAjustes() = _ui.update { UiSentinela(ajustes = true) }
    fun cerrarAjustes() = _ui.update { UiSentinela() }

    fun tecla(d: String) {
        val u = _ui.value
        val pin = if (d == BORRAR) u.pin.dropLast(1) else if (u.pin.length < 4) u.pin + d else u.pin
        if (pin.length < 4) {
            _ui.update { it.copy(pin = pin) }
            return
        }
        if (c.coordinador.pinCorrecto(pin)) {
            _ui.update { it.copy(pin = "", pinOk = true) }
        } else {
            _ui.update { it.copy(pin = "") }
            c.coordinador.avisar("PIN incorrecto", TipoAviso.CANDADO)
            c.vibrar(200)
        }
    }

    fun lugar(l: Lugar) = c.coordinador.cambiarLugar(l)
    fun activar() {
        c.coordinador.activarVigilancia()
        cerrarAjustes()
    }

    fun detener() {
        c.coordinador.detenerVigilancia()
        cerrarAjustes()
    }

    fun sirena() = c.coordinador.alternarSirena(estado.value.yo.id)

    fun recalibrar() {
        cerrarAjustes()
        calibrador.reiniciar()
        _calibracion.value = calibrador.estado(c.reloj.ahora())
        c.coordinador.recalibrar()
    }

    fun simular(tipo: Simulacion) {
        if (!c.vigilante.simular(tipo)) c.coordinador.avisar("Primero activa la vigilancia o usa «Probar»")
    }

    fun confirmarSalir(si: Boolean) = _ui.update { it.copy(confirmarSalir = si) }
    fun salir() {
        _ui.value = UiSentinela()
        c.coordinador.salir()
    }

    // ---------- calibración ----------

    private val calibrador = Calibrador()
    private val _calibracion = MutableStateFlow(EstadoCalibracion())
    val calibracion: StateFlow<EstadoCalibracion> = _calibracion.asStateFlow()
    private var medicion: Job? = null

    fun empezarPaso(p: Int) {
        medicion?.cancel()
        calibrador.empezar(p, c.reloj.ahora())
        c.vigilante.pausado = true
        _calibracion.value = calibrador.estado(c.reloj.ahora())
        medicion = viewModelScope.launch {
            val lector = launch { c.sensores.lecturas.collect { calibrador.muestra(it) } }
            while (isActive) {
                delay(200)
                val ahora = c.reloj.ahora()
                val termino = calibrador.tic(ahora)
                _calibracion.value = calibrador.estado(ahora)
                if (termino) {
                    lector.cancel()
                    c.vigilante.pausado = false
                    c.vibrar(80)
                    c.bip(990, 120, 0.2f)
                    break
                }
            }
        }
    }

    fun guardarCalibracion(deFabrica: Boolean) {
        medicion?.cancel()
        c.vigilante.pausado = false
        c.coordinador.guardarCalibracion(if (deFabrica) Calibracion.FABRICA else calibrador.resultado())
        if (!deFabrica) c.coordinador.avisar("¡Listo! Sensibilidad ajustada", TipoAviso.CHECK)
        calibrador.reiniciar()
        _calibracion.value = calibrador.estado(c.reloj.ahora())
    }

    override fun onCleared() {
        c.vigilante.pausado = false
    }

    companion object {
        const val BORRAR = "borrar"
    }
}
