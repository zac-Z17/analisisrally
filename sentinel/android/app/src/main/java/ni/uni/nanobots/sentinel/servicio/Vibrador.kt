package ni.uni.nanobots.sentinel.servicio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Vibración compatible desde Android 7. */
class Vibrador(context: Context) {
    private val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /** Patrón como navigator.vibrate(): vibra, pausa, vibra… */
    fun vibrar(patron: LongArray) {
        val v = vibrador ?: return
        if (patron.isEmpty()) return
        val conPausaInicial = longArrayOf(0) + patron
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(conPausaInicial, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(conPausaInicial, -1)
            }
        }
    }

    fun detener() {
        runCatching { vibrador?.cancel() }
    }
}
