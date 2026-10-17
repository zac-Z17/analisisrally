package ni.uni.nanobots.sentinel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ni.uni.nanobots.sentinel.di.Contenedor
import ni.uni.nanobots.sentinel.domain.hogar.Aviso
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp

/** Decide qué pantalla ve el usuario según el rol de este equipo. */
class MainViewModel(c: Contenedor) : ViewModel() {
    val listo: StateFlow<Boolean> = c.repo.listo
    val estado: StateFlow<EstadoApp> = c.repo.estado
    val alarma: StateFlow<Alerta?> = c.coordinador.alarma
    val avisos: SharedFlow<Aviso> = c.coordinador.avisos

    init {
        viewModelScope.launch {
            c.repo.esperarListo()
            // La demostración no usa el servicio: su sesión vive mientras la app esté abierta.
            if (c.repo.estado.value.hogar != null) c.coordinador.asegurarSesion()
        }
    }
}
