package ni.uni.nanobots.sentinel.di

import android.app.Application
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ni.uni.nanobots.sentinel.data.local.AlmacenDataStore
import ni.uni.nanobots.sentinel.data.red.NtfyMensajeria
import ni.uni.nanobots.sentinel.data.sensores.FuenteSensoresAndroid
import ni.uni.nanobots.sentinel.domain.hogar.Coordinador
import ni.uni.nanobots.sentinel.domain.hogar.RepositorioEstado
import ni.uni.nanobots.sentinel.domain.hogar.Vigilante
import ni.uni.nanobots.sentinel.domain.hogar.Vinculador
import ni.uni.nanobots.sentinel.domain.puertos.FuenteSensores
import ni.uni.nanobots.sentinel.domain.puertos.Reloj
import ni.uni.nanobots.sentinel.servicio.EfectosAndroid
import ni.uni.nanobots.sentinel.servicio.Notificador
import ni.uni.nanobots.sentinel.servicio.Sirena
import ni.uni.nanobots.sentinel.servicio.Vibrador
import ni.uni.nanobots.sentinel.servicio.VisibilidadApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Inyección de dependencias manual: aquí se arma todo una sola vez por proceso.
 * Las pantallas y el servicio comparten estas mismas instancias.
 */
class Contenedor(app: Application) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val reloj: Reloj = object : Reloj {
        override fun ahora() = System.currentTimeMillis()
        override fun monotono() = SystemClock.elapsedRealtime()
    }

    val visibilidad = VisibilidadApp()
    val notificador = Notificador(app)

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)     // ntfy manda un «keepalive» cada 45 s
        .build()

    val repo = RepositorioEstado(AlmacenDataStore(app), scope)
    private val red = NtfyMensajeria(http) { repo.estado.value.relay }

    private val efectos = EfectosAndroid(
        Sirena(app.getSystemService(AudioManager::class.java)),
        Vibrador(app),
        notificador,
        visibilidad,
    )
    val sirenaSonando = efectos.sirenaSonando

    val coordinador = Coordinador(repo, red, efectos, reloj, scope)
    val vinculador = Vinculador(repo, red, coordinador, efectos, reloj, scope)

    val sensores: FuenteSensores = FuenteSensoresAndroid(app.getSystemService(SensorManager::class.java), scope)
    val vigilante = Vigilante(sensores, repo, coordinador, reloj)

    /** Para «Probar el sonido de alarma» en Ajustes. */
    fun probarSonido() = efectos.sirena(2)

    fun callar() = efectos.callar()

    fun bip(frecuencia: Int, duracionMs: Int, volumen: Float) = efectos.bip(frecuencia, duracionMs, volumen)

    fun vibrar(vararg patron: Long) = efectos.vibrar(*patron)
}
