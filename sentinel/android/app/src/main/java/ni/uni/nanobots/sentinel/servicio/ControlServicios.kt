package ni.uni.nanobots.sentinel.servicio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp

object ControlServicios {
    /** Enciende el servicio si este equipo pertenece a un hogar real; si no, lo apaga. */
    fun sincronizar(context: Context, estado: EstadoApp) {
        val intent = Intent(context, ServicioHogar::class.java)
        if (estado.hogar != null && estado.rol != null && !estado.demo) {
            runCatching { ContextCompat.startForegroundService(context, intent) }
        } else {
            context.stopService(intent)
        }
    }
}

/** Al encender el celular, vuelve a cuidar la casa sin que nadie abra la app. */
class ArranqueReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        // El servicio revisa el estado guardado y se apaga solo si no hay hogar.
        runCatching { ContextCompat.startForegroundService(context, Intent(context, ServicioHogar::class.java)) }
    }
}
