package ni.uni.nanobots.sentinel.servicio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Sirena y pitidos generados en el momento (sin archivos de audio). Suenan por el canal de
 * ALARMA, que no se silencia con el modo silencio del timbre.
 */
class Sirena(private val audio: AudioManager) {
    private val principal = Handler(Looper.getMainLooper())
    private var pista: AudioTrack? = null
    private var volumenPrevio: Int? = null
    private val _sonando = MutableStateFlow(false)
    val sonando: StateFlow<Boolean> = _sonando.asStateFlow()

    private val callarRunnable = Runnable { callarAhora() }

    /** Sirena: diente de sierra de 950 Hz que sube y baja ±420 Hz 1,6 veces por segundo. */
    fun sonar(segundos: Int) {
        principal.post { sonarAhora(segundos) }
    }

    fun callar() {
        principal.post { callarAhora() }
    }

    fun bip(frecuencia: Int, duracionMs: Int, volumen: Float) {
        principal.post { bipAhora(frecuencia, duracionMs, volumen) }
    }

    // Todo lo que toca la pista corre en el hilo principal, así no hay carreras.

    private fun sonarAhora(segundos: Int) {
        if (pista != null) return
        val muestras = cicloSirena()
        val t = crearPista(muestras.size) ?: return
        t.write(muestras, 0, muestras.size)
        t.setLoopPoints(0, muestras.size, -1)
        subirVolumen()
        t.play()
        pista = t
        _sonando.value = true
        principal.removeCallbacks(callarRunnable)
        if (segundos > 0) principal.postDelayed(callarRunnable, segundos * 1000L)
    }

    private fun callarAhora() {
        principal.removeCallbacks(callarRunnable)
        pista?.let {
            runCatching { it.stop() }
            it.release()
        }
        pista = null
        restaurarVolumen()
        _sonando.value = false
    }

    private fun bipAhora(frecuencia: Int, duracionMs: Int, volumen: Float) {
        val n = (FS * duracionMs / 1000.0).roundToInt().coerceAtLeast(64)
        val datos = ShortArray(n) { i ->
            val t = i.toDouble() / FS
            val env = exp(-6.9 * t * 1000 / duracionMs)        // baja a 0,001 al final
            (sin(2 * PI * frecuencia * t) * env * volumen * Short.MAX_VALUE).toInt().toShort()
        }
        val t = crearPista(n) ?: return
        t.write(datos, 0, n)
        t.play()
        principal.postDelayed({ runCatching { t.stop() }; t.release() }, duracionMs + 200L)
    }

    private fun crearPista(muestras: Int): AudioTrack? = runCatching {
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(FS)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(muestras * 2)
            .build()
    }.getOrNull()

    /** Una vuelta exacta del oscilador lento, para que el bucle no haga «clic». */
    private fun cicloSirena(): ShortArray {
        val n = (FS / 1.6).roundToInt()
        val vueltas = (950.0 * n / FS).roundToInt()
        val f0 = vueltas.toDouble() * FS / n
        var fase = 0.0
        return ShortArray(n) { i ->
            fase += (f0 + 420 * sin(2 * PI * i / n)) / FS
            val sierra = (fase - kotlin.math.floor(fase)) * 2 - 1
            (sierra * 0.5 * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun subirVolumen() {
        runCatching {
            volumenPrevio = audio.getStreamVolume(AudioManager.STREAM_ALARM)
            audio.setStreamVolume(AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
        }
    }

    private fun restaurarVolumen() {
        volumenPrevio?.let { v -> runCatching { audio.setStreamVolume(AudioManager.STREAM_ALARM, v, 0) } }
        volumenPrevio = null
    }

    private companion object {
        const val FS = 22_050
    }
}
