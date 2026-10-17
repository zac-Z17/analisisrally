package ni.uni.nanobots.sentinel.domain.hogar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.puertos.AlmacenEstado

/**
 * Única fuente de verdad del estado de este equipo. Todos los cambios pasan por
 * [actualizar] y se guardan solos poco después.
 */
class RepositorioEstado(private val almacen: AlmacenEstado, scope: CoroutineScope) {
    private val _estado = MutableStateFlow(EstadoApp())
    val estado: StateFlow<EstadoApp> = _estado.asStateFlow()

    private val _listo = MutableStateFlow(false)
    val listo: StateFlow<Boolean> = _listo.asStateFlow()

    init {
        scope.launch {
            almacen.leer()?.let { _estado.value = it }
            _listo.value = true
            guardarCambios()
        }
    }

    suspend fun esperarListo() {
        listo.first { it }
    }

    fun actualizar(cambio: (EstadoApp) -> EstadoApp) = _estado.update(cambio)

    fun reemplazar(nuevo: EstadoApp) {
        _estado.value = nuevo
    }

    @OptIn(FlowPreview::class)
    private suspend fun guardarCambios() {
        _estado.debounce(GUARDAR_TRAS_MS).collect { almacen.escribir(it) }
    }

    private companion object {
        const val GUARDAR_TRAS_MS = 300L
    }
}
