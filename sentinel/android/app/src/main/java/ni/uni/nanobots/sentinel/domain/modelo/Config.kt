package ni.uni.nanobots.sentinel.domain.modelo

/**
 * Configuración compartida por todo el hogar. Viaja en los mensajes «cfg»:
 * gana la de [v] más alta.
 */
data class Config(
    val v: Long = 0,
    val modo: Modo = Modo.AUTO,
    val modoHasta: Long = 0,
    val pausaHasta: Long = 0,
    val auto: Boolean = true,
    val inicio: String = "23:00",
    val fin: String = "04:00",
    /** De lunes a domingo. */
    val dias: List<Boolean> = List(7) { true },
    val carga: Boolean = true,
    val cargaIni: String = "21:00",
    val cargaFin: String = "22:00",
    val aviso10: Boolean = true,
    val batMin: Int = 60,
    val avisoTemp: Boolean = true,
    val tempMax: Int = 45,
    val fuera: Boolean = false,
    val equipos: Map<String, AjusteEquipo> = emptyMap(),
    val vecinosOff: Set<String> = emptySet(),
) {
    fun ajusteDe(id: String): AjusteEquipo = equipos[id] ?: AjusteEquipo()
}
