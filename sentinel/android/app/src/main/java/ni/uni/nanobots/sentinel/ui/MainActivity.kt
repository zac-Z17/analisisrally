package ni.uni.nanobots.sentinel.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.servicio.ControlServicios
import ni.uni.nanobots.sentinel.ui.alarma.AlarmaPantalla
import ni.uni.nanobots.sentinel.ui.comp.AvisoHost
import ni.uni.nanobots.sentinel.ui.inicio.InicioPantalla
import ni.uni.nanobots.sentinel.ui.receptor.ReceptorPantalla
import ni.uni.nanobots.sentinel.ui.sentinela.SentinelaPantalla
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.SentinelTema
import ni.uni.nanobots.sentinel.ui.vecino.VecinoPantalla

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels { fabrica { MainViewModel(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent { SentinelTema { RaizApp(vm) } }

        // El servicio en primer plano acompaña al hogar: se enciende al unirse y se apaga al salir.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.listo.filter { it }.collect {
                    vm.estado
                        .map { Triple(it.hogar?.id, it.rol, it.demo) }
                        .distinctUntilChanged()
                        .collect { ControlServicios.sincronizar(this@MainActivity, vm.estado.value) }
                }
            }
        }
    }
}

@Composable
private fun RaizApp(vm: MainViewModel) {
    val listo by vm.listo.collectAsStateWithLifecycle()
    val estado by vm.estado.collectAsStateWithLifecycle()
    val alarma by vm.alarma.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize().background(Paleta.Noche)) {
        if (!listo) return@Box
        val conNav = estado.hogar != null && estado.rol == Rol.RECEPTOR
        when {
            estado.hogar == null -> InicioPantalla()
            estado.rol == Rol.SENTINELA -> SentinelaPantalla()
            estado.rol == Rol.VECINO -> VecinoPantalla()
            else -> ReceptorPantalla()
        }
        alarma?.let { AlarmaPantalla(it) }
        AvisoHost(vm.avisos, margenInferior = if (conNav && alarma == null) 84.dp else 24.dp)
    }
}
