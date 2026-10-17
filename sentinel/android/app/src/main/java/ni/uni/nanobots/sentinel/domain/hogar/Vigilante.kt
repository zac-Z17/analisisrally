package ni.uni.nanobots.sentinel.domain.hogar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import ni.uni.nanobots.sentinel.domain.puertos.FuenteSensores
import ni.uni.nanobots.sentinel.domain.reglas.Deteccion
import ni.uni.nanobots.sentinel.domain.reglas.Detector
import ni.uni.nanobots.sentinel.domain.reglas.Simulacion
import ni.uni.nanobots.sentinel.domain.reglas.Umbrales
import ni.uni.nanobots.sentinel.domain.puertos.Reloj

/** Une los sensores con el [Detector] y avisa al hogar de lo que detecta. */
class Vigilante(
    private val fuente: FuenteSensores,
    private val repo: RepositorioEstado,
    private val coord: Coordinador,
    private val reloj: Reloj,
) {
    private val detector = Detector()

    /** Durante la calibración no se detecta. */
    @Volatile
    var pausado = false

    fun iniciar(scope: CoroutineScope): Job = scope.launch {
        launch {
            while (isActive) {
                delay(500)
                val lugar = repo.estado.value.yo.lugar
                synchronized(detector) { detector.revisarLeve(reloj.monotono(), vigilando(), prueba(), lugar) }?.let(::emitir)
            }
        }
        fuente.lecturas.collect { l ->
            coord.sensoresConDatos()
            if (pausado) return@collect
            val est = repo.estado.value
            val u = Umbrales.de(est.cal, est.cfg.ajusteDe(est.yo.id))
            synchronized(detector) { detector.procesar(l, u, vigilando(), prueba(), est.yo.lugar) }?.let(::emitir)
        }
    }

    /** Simula un intento. Devuelve false si no está vigilando ni en modo prueba. */
    fun simular(tipo: Simulacion): Boolean {
        if (!vigilando() && !prueba()) return false
        val lugar = repo.estado.value.yo.lugar
        emitir(synchronized(detector) { detector.simular(tipo, reloj.monotono(), prueba(), lugar) })
        return true
    }

    private fun vigilando() = coord.vigilancia.value == Vigilancia.VIGILANDO
    private fun prueba() = reloj.ahora() < coord.pruebaHasta.value
    private fun emitir(d: Deteccion) = coord.emitirEvento(d.clase, d.detalle, d.pico)
}
