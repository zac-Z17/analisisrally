package ni.uni.nanobots.sentinel.domain.modelo

/** Sobre común de todos los mensajes: id del mensaje, equipo que lo envía y hora en ms. */
data class Mensaje(val mid: String, val de: String, val t: Long, val contenido: Contenido)

sealed interface Contenido {
    data class Solicitud(val rol: Rol, val nombre: String, val lugar: Lugar, val ref: String) : Contenido
    data class Bienvenida(val para: String, val hogar: Hogar, val cfg: Config) : Contenido
    data class Hola(val rol: Rol?, val cfgv: Long) : Contenido

    /** Latido del Sentinela. [temp] solo lo envía la app Android (el navegador no la conoce). */
    data class Estado(
        val nombre: String,
        val lugar: Lugar,
        val vig: Vigilancia?,
        val bat: Int?,
        val carga: Boolean,
        val sensoresOk: Boolean,
        val cfgv: Long,
        val temp: Int? = null,
    ) : Contenido

    data class Presencia(val rol: Rol, val nombre: String, val ref: String, val admin: Boolean, val cfgv: Long) : Contenido
    data class Cfg(val cfg: Config) : Contenido
    data class Evento(
        val clase: ClaseAlerta,
        val detalle: String,
        val pico: Double?,
        val equipoId: String,
        val equipoNombre: String,
    ) : Contenido

    data class Orden(
        val cmd: Comando,
        val para: String? = null,
        val alerta: String? = null,
        val falsa: Boolean = false,
        val desde: String = "",
    ) : Contenido

    /** Mensaje «etiqueta»: respuesta a «¿Qué fue?». */
    data class Marca(val alerta: String, val que: Etiqueta) : Contenido
}

object Temas {
    fun hogar(id: String) = "snt-$id"
    fun vinculo(codigo: String) = "snt-v-$codigo"
}
