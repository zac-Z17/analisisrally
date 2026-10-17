package ni.uni.nanobots.sentinel.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ni.uni.nanobots.sentinel.SentinelApp
import ni.uni.nanobots.sentinel.ui.alarma.AlarmaPantalla
import ni.uni.nanobots.sentinel.ui.tema.SentinelTema

/** Pantalla roja que se abre sola sobre la pantalla de bloqueo cuando hay un intento de entrada. */
class AlarmaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val alarmaFlujo = (application as SentinelApp).contenedor.coordinador.alarma
        setContent {
            SentinelTema {
                val alarma by alarmaFlujo.collectAsStateWithLifecycle()
                LaunchedEffect(alarma) { if (alarma == null) finish() }
                alarma?.let { AlarmaPantalla(it) }
            }
        }
    }
}
