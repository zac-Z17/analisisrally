package ni.uni.nanobots.sentinel.domain.hogar

/** Mensaje corto para el usuario (el «toast» del prototipo). El tipo decide el ícono. */
data class Aviso(val texto: String, val tipo: TipoAviso = TipoAviso.INFO)

enum class TipoAviso { INFO, CHECK, ALERTA, VIBRACION, SIRENA, BATERIA, SIN_RED, RAYO, PAUSA, ESCUDO, CANDADO, PLAY, USUARIO }

/** Datos de este Centinela que no se guardan: batería, temperatura y si llegan lecturas. */
data class DatosLocales(
    val bat: Int? = null,
    val carga: Boolean = false,
    val temp: Int? = null,
    val sensoresOk: Boolean = false,
)

/** Conjunto acotado de ids de mensajes ya vistos, para no procesarlos dos veces. */
internal class VistosRecientes(private val max: Int) {
    private val ids = LinkedHashSet<String>()

    /** true si el id es nuevo. */
    @Synchronized
    fun agregar(id: String): Boolean {
        if (!ids.add(id)) return false
        if (ids.size > max) ids.remove(ids.first())
        return true
    }
}
