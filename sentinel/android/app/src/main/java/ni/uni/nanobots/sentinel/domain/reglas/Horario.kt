package ni.uni.nanobots.sentinel.domain.reglas

import ni.uni.nanobots.sentinel.comun.Formato
import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/** Reglas del horario de vigilancia. La noche pertenece al día en que empieza. */
object Horario {

    /** Lunes = 0 … domingo = 6. */
    private fun dia(d: ZonedDateTime) = d.dayOfWeek.value - 1

    fun enHorario(cfg: Config, d: ZonedDateTime): Boolean {
        if (!cfg.auto) return false
        val m = d.hour * 60 + d.minute
        val i = Formato.aMin(cfg.inicio)
        val f = Formato.aMin(cfg.fin)
        val hoy = dia(d)
        return when {
            i == f -> cfg.dias[hoy]
            i < f -> m in i until f && cfg.dias[hoy]
            m >= i -> cfg.dias[hoy]
            m < f -> cfg.dias[(hoy + 6) % 7]
            else -> false
        }
    }

    fun estado(cfg: Config, t: Long, zona: ZoneId = ZoneId.systemDefault()): Vigilancia = when {
        cfg.pausaHasta > t -> Vigilancia.PAUSA
        cfg.modo == Modo.OFF && (cfg.modoHasta == 0L || t < cfg.modoHasta) -> Vigilancia.REPOSO
        cfg.fuera || cfg.modo == Modo.ON || enHorario(cfg, Instant.ofEpochMilli(t).atZone(zona)) -> Vigilancia.VIGILANDO
        else -> Vigilancia.REPOSO
    }

    /** Próxima hora en que empieza la vigilancia automática, o 0 si está apagada. */
    fun proximoInicio(cfg: Config, t: Long, zona: ZoneId = ZoneId.systemDefault()): Long {
        if (!cfg.auto) return 0
        val ahora = Instant.ofEpochMilli(t).atZone(zona)
        val minutos = Formato.aMin(cfg.inicio).toLong()
        for (d in 0..7) {
            val c = ahora.toLocalDate().plusDays(d.toLong()).atStartOfDay(zona).plusMinutes(minutos)
            if (c.toInstant().toEpochMilli() > t && cfg.dias[dia(c)]) return c.toInstant().toEpochMilli()
        }
        return 0
    }
}
