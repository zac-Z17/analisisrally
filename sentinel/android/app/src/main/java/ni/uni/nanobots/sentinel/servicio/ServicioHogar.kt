package ni.uni.nanobots.sentinel.servicio

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ni.uni.nanobots.sentinel.SentinelApp
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import kotlin.math.roundToInt

/**
 * Servicio en primer plano que mantiene viva la sesión del hogar con la app cerrada.
 * En el Centinela además mantiene la CPU despierta y lee los sensores con la pantalla apagada.
 */
class ServicioHogar : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val c by lazy { (application as SentinelApp).contenedor }

    private var candado: PowerManager.WakeLock? = null
    private var bateria: BroadcastReceiver? = null
    private var vigilancia: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, Notificador.ID_SERVICIO, c.notificador.servicio("Centinel está activo"), tipo)
        scope.launch {
            c.repo.esperarListo()
            launch { mantenerTexto() }
            c.repo.estado.map { Clave(it.hogar?.id, it.rol, it.demo) }.distinctUntilChanged().collect { k ->
                if (k.hogar == null || k.rol == null || k.demo) {
                    stopSelf()
                    return@collect
                }
                c.coordinador.asegurarSesion()
                if (k.rol == Rol.SENTINELA) iniciarSentinela() else detenerSentinela()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        detenerSentinela()
        scope.cancel()
        super.onDestroy()
    }

    @SuppressLint("WakelockTimeout")
    private fun iniciarSentinela() {
        if (vigilancia != null) return
        candado = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sentinel:vigilancia")
            .apply { acquire() }
        val receptor = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = leerBateria(intent)
        }
        ContextCompat.registerReceiver(this, receptor, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
            ?.let(::leerBateria)
        bateria = receptor
        vigilancia = c.vigilante.iniciar(scope)
    }

    private fun detenerSentinela() {
        vigilancia?.cancel()
        vigilancia = null
        bateria?.let { runCatching { unregisterReceiver(it) } }
        bateria = null
        candado?.takeIf { it.isHeld }?.release()
        candado = null
    }

    private fun leerBateria(i: Intent) {
        val nivel = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val escala = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val estado = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val enchufado = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        val temp = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        c.coordinador.actualizarBateria(
            nivel = if (nivel >= 0 && escala > 0) (nivel * 100f / escala).roundToInt() else null,
            carga = enchufado || estado == BatteryManager.BATTERY_STATUS_CHARGING || estado == BatteryManager.BATTERY_STATUS_FULL,
            temp = if (temp == Int.MIN_VALUE) null else (temp / 10f).roundToInt(),
        )
    }

    private suspend fun mantenerTexto() {
        combine(c.repo.estado, c.coordinador.vigilancia) { e, v -> texto(e, v) }
            .distinctUntilChanged()
            .collect { c.notificador.actualizarServicio(it) }
    }

    private fun texto(e: EstadoApp, v: Vigilancia): String {
        val casa = e.hogar?.nombre.orEmpty()
        return when (e.rol) {
            Rol.SENTINELA -> when (v) {
                Vigilancia.VIGILANDO -> "Vigilando · ${e.yo.nombre}"
                Vigilancia.PAUSA -> "En pausa · ${e.yo.nombre}"
                Vigilancia.REPOSO -> "En reposo · ${e.yo.nombre}"
            }
            Rol.VECINO -> "Cuidas la casa «$casa»"
            else -> "Atento a las alertas de «$casa»"
        }
    }

    private data class Clave(val hogar: String?, val rol: Rol?, val demo: Boolean)
}
