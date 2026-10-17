package ni.uni.nanobots.sentinel.data.sensores

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import ni.uni.nanobots.sentinel.domain.puertos.FuenteSensores
import ni.uni.nanobots.sentinel.domain.puertos.Lectura
import kotlin.math.sqrt

/**
 * Lecturas del acelerómetro y del giroscopio. Usa la aceleración lineal si el celular la tiene;
 * si no, resta la gravedad con un filtro paso bajo (α = 0,9), igual que el prototipo.
 * Se comparte entre el servicio, la calibración y la gráfica: los sensores se registran una sola vez.
 */
class FuenteSensoresAndroid(private val sm: SensorManager, scope: CoroutineScope) : FuenteSensores {
    private val lineal: Sensor? = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val acel: Sensor? = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val giro: Sensor? = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    override val tieneAcelerometro: Boolean = lineal != null || acel != null
    override val tieneGiroscopio: Boolean = giro != null

    override val lecturas: Flow<Lectura> = callbackFlow {
        val oyente = object : SensorEventListener {
            private var r = 0.0
            private var ultimoNs = 0L
            private var g: FloatArray? = null

            override fun onSensorChanged(ev: SensorEvent) {
                val v = ev.values
                when (ev.sensor.type) {
                    Sensor.TYPE_GYROSCOPE -> r = Math.toDegrees(norma(v[0], v[1], v[2]))
                    Sensor.TYPE_LINEAR_ACCELERATION -> emitir(ev.timestamp, norma(v[0], v[1], v[2]))
                    Sensor.TYPE_ACCELEROMETER -> {
                        val gg = g ?: floatArrayOf(v[0], v[1], v[2]).also { g = it }
                        for (i in 0..2) gg[i] = 0.9f * gg[i] + 0.1f * v[i]
                        emitir(ev.timestamp, norma(v[0] - gg[0], v[1] - gg[1], v[2] - gg[2]))
                    }
                }
            }

            private fun emitir(ns: Long, a: Double) {
                val dt = if (ultimoNs == 0L) 0.02 else ((ns - ultimoNs) / 1e9).coerceIn(0.001, 0.2)
                ultimoNs = ns
                trySend(Lectura(SystemClock.elapsedRealtime(), a, r, dt))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        (lineal ?: acel)?.let { sm.registerListener(oyente, it, SensorManager.SENSOR_DELAY_GAME) }
        giro?.let { sm.registerListener(oyente, it, SensorManager.SENSOR_DELAY_GAME) }
        awaitClose { sm.unregisterListener(oyente) }
    }.shareIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 2_000))

    private fun norma(x: Float, y: Float, z: Float): Double = sqrt((x * x + y * y + z * z).toDouble())
}
