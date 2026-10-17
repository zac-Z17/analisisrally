package ni.uni.nanobots.sentinel.domain.reglas

import ni.uni.nanobots.sentinel.domain.modelo.AjusteEquipo
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Nivel
import ni.uni.nanobots.sentinel.domain.puertos.Lectura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectorTest {
    private val u = Umbrales.de(null, AjusteEquipo())
    private val det = Detector()
    private var t = 1_000_000L

    /** Avanza 20 ms (50 Hz) y procesa una lectura. */
    private fun paso(a: Double, r: Double = 0.0, vigilando: Boolean = true, prueba: Boolean = false): Deteccion? {
        t += 20
        return det.procesar(Lectura(t, a, r, 0.02), u, vigilando, prueba, Lugar.PUERTA)
    }

    @Test
    fun `umbrales de fabrica con sensibilidad media`() {
        assertEquals(0.8, u.leve, 1e-9)
        assertEquals(2.5, u.fuerte, 1e-9)
        assertEquals(8.0, u.muy, 1e-9)
        assertEquals(15.0, u.ang, 1e-9)
    }

    @Test
    fun `sensibilidad alta baja los umbrales y el ajuste solo afecta al leve`() {
        val alta = Umbrales.de(null, AjusteEquipo(Nivel.ALTA, ajuste = 2.0))
        assertEquals(0.8 * 0.6 * 2.0, alta.leve, 1e-9)
        assertEquals(2.5 * 0.6, alta.fuerte, 1e-9)
        assertEquals(10.0, alta.ang, 1e-9)
    }

    @Test
    fun `abrir la puerta es un intento`() {
        var d: Deteccion? = null
        // 100 °/s durante 0,2 s = 20° > 15°
        repeat(10) { d = d ?: paso(0.1, r = 100.0) }
        assertNotNull(d)
        assertEquals(ClaseAlerta.INTENTO, d!!.clase)
        assertTrue(d!!.detalle.startsWith("La puerta se abrió"))
    }

    @Test
    fun `un golpe muy fuerte es un intento`() {
        val d = paso(9.0)
        assertEquals(ClaseAlerta.INTENTO, d?.clase)
        assertEquals("Golpe muy fuerte", d?.detalle)
    }

    @Test
    fun `tres golpes fuertes en menos de 4 s son forcejeo`() {
        assertNull(paso(3.0))
        repeat(20) { paso(0.0) }          // 400 ms
        assertNull(paso(3.0))
        repeat(20) { paso(0.0) }
        val d = paso(3.0)
        assertEquals("Golpes fuertes repetidos (forcejeo)", d?.detalle)
    }

    @Test
    fun `golpes separados por mas de 4 s no son forcejeo`() {
        paso(3.0)
        repeat(110) { paso(0.0) }          // 2,2 s
        paso(3.0)
        repeat(110) { paso(0.0) }
        assertNull(paso(3.0))
    }

    @Test
    fun `despues de un intento espera 20 s`() {
        assertNotNull(paso(9.0))
        assertNull(paso(9.0))
        t += 20_000
        assertNotNull(paso(9.0))
    }

    @Test
    fun `sin vigilancia no detecta nada`() {
        assertNull(paso(9.0, vigilando = false))
    }

    @Test
    fun `en modo prueba avisa como prueba`() {
        assertEquals(ClaseAlerta.PRUEBA, paso(9.0, vigilando = false, prueba = true)?.clase)
    }

    @Test
    fun `vibracion leve se avisa a los 2,5 s y como maximo una cada 30 s`() {
        paso(1.0)
        assertNull(det.revisarLeve(t + 1_000, true, false, Lugar.VENTANA))
        val d = det.revisarLeve(t + 2_600, true, false, Lugar.VENTANA)
        assertEquals(ClaseAlerta.LEVE, d?.clase)
        assertEquals("Vibración rara en la ventana", d?.detalle)
        t += 5_000
        paso(1.0)
        assertNull(det.revisarLeve(t + 2_600, true, false, Lugar.VENTANA))
    }

    @Test
    fun `simular siempre da un intento`() {
        val d = det.simular(Simulacion.APERTURA, t, false, Lugar.PUERTA)
        assertEquals(ClaseAlerta.INTENTO, d.clase)
        assertEquals("La puerta se abrió (simulado)", d.detalle)
    }
}
