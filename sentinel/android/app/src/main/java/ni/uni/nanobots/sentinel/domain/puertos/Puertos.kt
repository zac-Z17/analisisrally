package ni.uni.nanobots.sentinel.domain.puertos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje

/*
 * Puertos: lo que el dominio necesita del mundo exterior. Las implementaciones
 * Android viven en data/ y servicio/; las pruebas usan versiones falsas.
 */

/** Mensajería entre los equipos del hogar (hoy ntfy; mañana wifi local o FCM). */
interface Mensajeria {
    /** null mientras no se sabe; false si la última conexión falló. */
    val conectado: StateFlow<Boolean?>

    suspend fun enviar(tema: String, mensaje: Mensaje): Boolean

    /** Mensajes válidos del tema. Se reconecta sola hasta que se cancele la colección. */
    fun escuchar(tema: String, desde: String? = null): Flow<Mensaje>
}

interface AlmacenEstado {
    suspend fun leer(): EstadoApp?
    suspend fun escribir(estado: EstadoApp)
}

/** Reacciones físicas del equipo: sonido, vibración, notificaciones y pantalla de alarma. */
interface Efectos {
    val sirenaSonando: StateFlow<Boolean>
    fun sirena(segundos: Int)
    fun callar()
    fun bip(frecuencia: Int = 880, duracionMs: Int = 150, volumen: Float = 0.3f)
    fun vibrar(vararg patronMs: Long)
    fun detenerVibracion()
    fun mostrarAlarma(alerta: Alerta)
    fun cerrarAlarma()

    /** Notificación del sistema, solo si la app no está a la vista. */
    fun notificar(texto: String)
}

/** Una lectura combinada de acelerómetro y giroscopio. */
data class Lectura(
    /** Hora monotónica en ms (no cambia si el usuario mueve el reloj). */
    val t: Long,
    /** Tamaño de la aceleración lineal, en m/s². */
    val a: Double,
    /** Tamaño de la velocidad de giro, en °/s. */
    val r: Double,
    /** Segundos desde la lectura anterior. */
    val dt: Double,
)

interface FuenteSensores {
    val tieneAcelerometro: Boolean
    val tieneGiroscopio: Boolean
    val lecturas: Flow<Lectura>
}

interface Reloj {
    fun ahora(): Long
    fun monotono(): Long
}
