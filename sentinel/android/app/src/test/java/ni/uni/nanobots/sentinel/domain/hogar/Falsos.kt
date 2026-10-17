package ni.uni.nanobots.sentinel.domain.hogar

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje
import ni.uni.nanobots.sentinel.domain.puertos.AlmacenEstado
import ni.uni.nanobots.sentinel.domain.puertos.Efectos
import ni.uni.nanobots.sentinel.domain.puertos.Mensajeria
import ni.uni.nanobots.sentinel.domain.puertos.Reloj

/** Servidor de mensajes en memoria: lo que un equipo envía le llega a todos los que escuchan el tema. */
class RedEnMemoria : Mensajeria {
    override val conectado: StateFlow<Boolean?> = MutableStateFlow(true)
    private val temas = HashMap<String, MutableSharedFlow<Mensaje>>()
    val enviados = mutableListOf<Pair<String, Mensaje>>()

    private fun tema(t: String) = temas.getOrPut(t) { MutableSharedFlow(extraBufferCapacity = 64) }

    override suspend fun enviar(tema: String, mensaje: Mensaje): Boolean {
        enviados += tema to mensaje
        tema(tema).emit(mensaje)
        return true
    }

    override fun escuchar(tema: String, desde: String?): Flow<Mensaje> = tema(tema)
}

class EfectosFalsos : Efectos {
    override val sirenaSonando = MutableStateFlow(false)
    val alarmas = mutableListOf<Alerta>()
    val sirenas = mutableListOf<Int>()
    val notificaciones = mutableListOf<String>()

    override fun sirena(segundos: Int) {
        sirenas += segundos
        sirenaSonando.value = true
    }

    override fun callar() {
        sirenaSonando.value = false
    }

    override fun bip(frecuencia: Int, duracionMs: Int, volumen: Float) = Unit
    override fun vibrar(vararg patronMs: Long) = Unit
    override fun detenerVibracion() = Unit
    override fun mostrarAlarma(alerta: Alerta) {
        alarmas += alerta
    }

    override fun cerrarAlarma() = Unit
    override fun notificar(texto: String) {
        notificaciones += texto
    }
}

class AlmacenEnMemoria(var guardado: EstadoApp? = null) : AlmacenEstado {
    override suspend fun leer() = guardado
    override suspend fun escribir(estado: EstadoApp) {
        guardado = estado
    }
}

/** Reloj que avanza con el tiempo virtual de la prueba. */
class RelojVirtual(private val s: TestCoroutineScheduler, private val base: Long = 1_760_000_000_000) : Reloj {
    override fun ahora() = base + s.currentTime
    override fun monotono() = s.currentTime
}
