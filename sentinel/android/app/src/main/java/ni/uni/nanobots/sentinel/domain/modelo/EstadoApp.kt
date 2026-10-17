package ni.uni.nanobots.sentinel.domain.modelo

import ni.uni.nanobots.sentinel.comun.Ids

const val RELAY_DEF = "https://ntfy.sh"
const val MAX_ALERTAS = 120
const val MAX_VECINOS = 5

/** Todo lo que este equipo guarda. Es la única fuente de verdad (ver RepositorioEstado). */
data class EstadoApp(
    val yo: Perfil = Perfil(id = Ids.nuevo(8)),
    val hogar: Hogar? = null,
    val admin: Boolean = false,
    val cfg: Config = Config(),
    val equipos: Map<String, Equipo> = emptyMap(),
    val pendientes: List<Pendiente> = emptyList(),
    val alertas: List<Alerta> = emptyList(),
    val cal: Calibracion? = null,
    val relay: String = RELAY_DEF,
    val demo: Boolean = false,
    /** Últimos avisos dados (clave → hora), para no repetirlos. */
    val avisos: Map<String, Long> = emptyMap(),
) {
    val rol: Rol? get() = yo.rol
    val sentinelas: List<Equipo> get() = equipos.values.filter { it.rol == Rol.SENTINELA }
    val receptores: List<Equipo> get() = equipos.values.filter { it.rol == Rol.RECEPTOR }
    val vecinos: List<Equipo> get() = equipos.values.filter { it.rol == Rol.VECINO }
    val vecinosActivos: List<Equipo> get() = vecinos.filter { it.id !in cfg.vecinosOff }
}
