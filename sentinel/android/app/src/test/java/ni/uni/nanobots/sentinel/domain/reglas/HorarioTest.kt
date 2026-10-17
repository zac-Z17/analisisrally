package ni.uni.nanobots.sentinel.domain.reglas

import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class HorarioTest {
    private val zona = ZoneId.of("America/Managua")

    /** 2026-10-16 es viernes. */
    private fun en(dia: Int, h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, dia, h, m, 0, 0, zona)
    private fun ms(d: ZonedDateTime) = d.toInstant().toEpochMilli()

    @Test
    fun `de 11 p m a 4 a m vigila de noche y no de dia`() {
        val c = Config()
        assertTrue(Horario.enHorario(c, en(16, 23, 30)))
        assertTrue(Horario.enHorario(c, en(17, 3, 59)))
        assertFalse(Horario.enHorario(c, en(17, 4, 0)))
        assertFalse(Horario.enHorario(c, en(16, 12, 0)))
    }

    @Test
    fun `la madrugada pertenece al dia en que empezo la noche`() {
        // Viernes apagado: la madrugada del sábado tampoco vigila.
        val c = Config(dias = listOf(true, true, true, true, false, true, true))
        assertFalse(Horario.enHorario(c, en(16, 23, 30)))
        assertFalse(Horario.enHorario(c, en(17, 2, 0)))
        // Sábado sí: la noche del sábado vigila.
        assertTrue(Horario.enHorario(c, en(17, 23, 30)))
    }

    @Test
    fun `pausa y modos manuales`() {
        val t = ms(en(16, 12))
        assertEquals(Vigilancia.PAUSA, Horario.estado(Config(pausaHasta = t + 1), t, zona))
        assertEquals(Vigilancia.VIGILANDO, Horario.estado(Config(modo = Modo.ON), t, zona))
        assertEquals(Vigilancia.VIGILANDO, Horario.estado(Config(fuera = true), t, zona))
        val noche = ms(en(16, 23, 30))
        assertEquals(Vigilancia.REPOSO, Horario.estado(Config(modo = Modo.OFF, modoHasta = noche + 1), noche, zona))
        // Al pasar «modoHasta» vuelve el horario.
        assertEquals(Vigilancia.VIGILANDO, Horario.estado(Config(modo = Modo.OFF, modoHasta = noche - 1), noche, zona))
    }

    @Test
    fun `proximo inicio salta los dias apagados`() {
        val c = Config(dias = listOf(true, true, true, true, false, false, true))
        assertEquals(ms(en(18, 23)), Horario.proximoInicio(c, ms(en(16, 12)), zona))
        assertEquals(0L, Horario.proximoInicio(c.copy(auto = false), ms(en(16, 12)), zona))
    }
}
