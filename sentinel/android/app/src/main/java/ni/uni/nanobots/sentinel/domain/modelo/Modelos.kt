package ni.uni.nanobots.sentinel.domain.modelo

/*
 * Modelos del dominio. Cada enum guarda su «clave», el texto que viaja por la red
 * (mismo protocolo que el prototipo web: ver sentinel/README.md, sección 4).
 */

enum class Rol(val clave: String) {
    SENTINELA("sentinela"), RECEPTOR("receptor"), VECINO("vecino");

    companion object {
        fun de(clave: String?): Rol? = entries.firstOrNull { it.clave == clave }
    }
}

enum class Lugar(val clave: String) {
    PUERTA("puerta"), VENTANA("ventana");

    companion object {
        fun de(clave: String?): Lugar = if (clave == VENTANA.clave) VENTANA else PUERTA
    }
}

enum class Modo(val clave: String) {
    AUTO("auto"), ON("on"), OFF("off");

    companion object {
        fun de(clave: String?): Modo? = entries.firstOrNull { it.clave == clave }
    }
}

enum class Vigilancia(val clave: String) {
    VIGILANDO("vigilando"), REPOSO("reposo"), PAUSA("pausa");

    companion object {
        fun de(clave: String?): Vigilancia? = entries.firstOrNull { it.clave == clave }
    }
}

/** Sensibilidad: multiplica los umbrales de aceleración por [k] y usa [ang] grados de giro. */
enum class Nivel(val clave: String, val k: Double, val ang: Double) {
    BAJA("baja", 1.6, 25.0), MEDIA("media", 1.0, 15.0), ALTA("alta", 0.6, 10.0);

    companion object {
        fun de(clave: String?): Nivel? = entries.firstOrNull { it.clave == clave }
    }
}

enum class ClaseAlerta(val clave: String, val porRed: Boolean) {
    INTENTO("intento", true),
    LEVE("leve", true),
    PRUEBA("prueba", true),
    BATERIA("bateria", true),
    CALOR("calor", true),
    CONEXION("conexion", false),
    TRANQUILA("tranquila", false);

    companion object {
        fun de(clave: String?): ClaseAlerta? = entries.firstOrNull { it.clave == clave }
        fun deRed(clave: String?): ClaseAlerta? = de(clave)?.takeIf { it.porRed }
    }
}

/** Respuesta a «¿Qué fue?». */
enum class Etiqueta(val clave: String) {
    LLUVIA("lluvia"), VIENTO("viento"), ANIMAL("animal"), YO("yo"), FALSA("falsa");

    /** Lluvia, viento y animal hacen al Sentinela menos sensible a vibraciones leves. */
    val ajustaSensibilidad: Boolean get() = this == LLUVIA || this == VIENTO || this == ANIMAL

    companion object {
        fun de(clave: String?): Etiqueta? = entries.firstOrNull { it.clave == clave }
    }
}

enum class Comando(val clave: String) {
    SIRENA("sirena"), CALLAR("callar"), PROBAR("probar"), RESUELTA("resuelta"), DESVINCULAR("desvincular");

    companion object {
        fun de(clave: String?): Comando? = entries.firstOrNull { it.clave == clave }
    }
}

data class AjusteEquipo(
    val nivel: Nivel = Nivel.MEDIA,
    /** Sube 15 % con cada «lluvia», «viento» o «animal», hasta 2,5. */
    val ajuste: Double = 1.0,
    val sirena: Boolean = true,
)

data class Hogar(val id: String, val nombre: String, val pin: String)

/** Este equipo. */
data class Perfil(
    val id: String,
    val nombre: String = "",
    val rol: Rol? = null,
    val lugar: Lugar = Lugar.PUERTA,
    val ref: String = "",
)

/** Otro equipo del hogar, tal como lo vemos por sus latidos. */
data class Equipo(
    val id: String,
    val rol: Rol,
    val nombre: String,
    val lugar: Lugar = Lugar.PUERTA,
    val ref: String = "",
    val admin: Boolean = false,
    val vig: Vigilancia? = null,
    val bat: Int? = null,
    val carga: Boolean = false,
    val sensoresOk: Boolean = true,
    val temp: Int? = null,
    val visto: Long = 0,
    val offAvisado: Boolean = false,
    val demo: Boolean = false,
)

data class Alerta(
    val mid: String,
    val clase: ClaseAlerta,
    val equipoId: String,
    val nombre: String,
    val t: Long,
    val detalle: String,
    val que: Etiqueta? = null,
    val resuelta: Boolean = false,
)

/** Vecino invitado que aún no se une con el código. */
data class Pendiente(val id: String, val nombre: String, val tel: String = "", val ref: String = "")

/** Umbrales base del Sentinela (m/s² y grados). */
data class Calibracion(val leve: Double, val fuerte: Double, val muy: Double, val ang: Double) {
    companion object {
        val FABRICA = Calibracion(leve = 0.8, fuerte = 2.5, muy = 8.0, ang = 15.0)
    }
}
