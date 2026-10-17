package ni.uni.nanobots.sentinel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import ni.uni.nanobots.sentinel.SentinelApp
import ni.uni.nanobots.sentinel.di.Contenedor

/** Crea un ViewModel con acceso al [Contenedor] de la app. */
inline fun <reified VM : ViewModel> fabrica(crossinline crear: (Contenedor) -> VM): ViewModelProvider.Factory =
    viewModelFactory {
        initializer { crear((this[APPLICATION_KEY] as SentinelApp).contenedor) }
    }

/** Hora actual que se renueva sola, para «hace 3 min» y las cuentas regresivas. */
fun ViewModel.relojUi(c: Contenedor, cadaMs: Long = 1_000): StateFlow<Long> = flow {
    while (true) {
        emit(c.reloj.ahora())
        delay(cadaMs)
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), c.reloj.ahora())
