package ni.uni.nanobots.sentinel.comun

import java.security.SecureRandom

/** Identificadores aleatorios, con el mismo alfabeto que el prototipo web. */
object Ids {
    private const val ALF = "abcdefghijkmnpqrstuvwxyz23456789"
    private val VALIDO = Regex("^[a-z0-9]{2,24}$")
    private val azar = SecureRandom()

    fun nuevo(n: Int = 10): String = buildString { repeat(n) { append(ALF[azar.nextInt(ALF.length)]) } }

    fun codigo6(): String = (100_000 + azar.nextInt(900_000)).toString()

    fun valido(id: String?): Boolean = id != null && VALIDO.matches(id)
}
