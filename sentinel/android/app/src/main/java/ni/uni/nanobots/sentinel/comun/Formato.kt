package ni.uni.nanobots.sentinel.comun

import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Textos de horas y fechas en español de Nicaragua («11:00 p. m.»). */
object Formato {
    private val ES = Locale("es")
    private val FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ES)
    private val DIA_SEMANA = DateTimeFormatter.ofPattern("EEEE", ES)

    /** «23:30» → 1410 minutos. */
    fun aMin(hhmm: String): Int {
        val p = hhmm.split(":")
        return (p.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    fun hhmm(min: Int): String {
        val m = ((min % 1440) + 1440) % 1440
        return "${(m / 60).toString().padStart(2, '0')}:${(m % 60).toString().padStart(2, '0')}"
    }

    fun fmtMin(min: Int): String {
        val m = ((min % 1440) + 1440) % 1440
        val h24 = m / 60
        val ap = if (h24 < 12) "a. m." else "p. m."
        val h = (h24 % 12).let { if (it == 0) 12 else it }
        return "$h:${(m % 60).toString().padStart(2, '0')} $ap"
    }

    fun fmtHora(t: Long, zona: ZoneId = ZoneId.systemDefault()): String {
        val d = Instant.ofEpochMilli(t).atZone(zona)
        return fmtMin(d.hour * 60 + d.minute)
    }

    fun hace(t: Long, ahora: Long): String {
        val s = ((ahora - t) / 1000.0).roundToInt()
        if (s < 60) return "hace un momento"
        val m = (s / 60.0).roundToInt()
        if (m < 60) return "hace $m min"
        return "hace ${(m / 60.0).roundToInt()} h"
    }

    fun diaDe(t: Long, ahora: Long, zona: ZoneId = ZoneId.systemDefault()): String {
        val d = Instant.ofEpochMilli(t).atZone(zona).toLocalDate()
        val hoy = Instant.ofEpochMilli(ahora).atZone(zona).toLocalDate()
        return when (d) {
            hoy -> "Hoy"
            hoy.minusDays(1) -> "Ayer"
            else -> d.format(FECHA_LARGA)
        }
    }

    /** «hoy», «mañana» o «el jueves». */
    fun cuando(t: Long, ahora: Long, zona: ZoneId = ZoneId.systemDefault()): String {
        val d = Instant.ofEpochMilli(t).atZone(zona).toLocalDate()
        val hoy = Instant.ofEpochMilli(ahora).atZone(zona).toLocalDate()
        return when (d) {
            hoy -> "hoy"
            hoy.plusDays(1) -> "mañana"
            else -> "el " + d.format(DIA_SEMANA)
        }
    }

    fun lugar(l: Lugar): String = if (l == Lugar.VENTANA) "ventana" else "puerta"

    fun listaNombres(n: List<String>): String =
        if (n.size <= 1) n.firstOrNull().orEmpty() else n.dropLast(1).joinToString(", ") + " y " + n.last()

    fun redondear2(x: Double): Double = (x * 100).roundToInt() / 100.0
}
