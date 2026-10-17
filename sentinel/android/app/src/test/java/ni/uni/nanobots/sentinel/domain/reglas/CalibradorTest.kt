package ni.uni.nanobots.sentinel.domain.reglas

import ni.uni.nanobots.sentinel.domain.modelo.Calibracion
import ni.uni.nanobots.sentinel.domain.puertos.Lectura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalibradorTest {
    private val cal = Calibrador()
    private var t = 0L

    private fun muestras(n: Int, a: (Int) -> Double, r: Double = 0.0) {
        repeat(n) { i ->
            t += 20
            cal.muestra(Lectura(t, a(i), r, 0.02))
        }
    }

    @Test
    fun `paso 1 mide el ruido y lo multiplica por 5`() {
        cal.empezar(1, 0)
        muestras(100, { 0.1 })
        assertFalse(cal.tic(4_000))
        assertTrue(cal.tic(5_000))
        val e = cal.estado(5_000)
        assertEquals(2, e.paso)
        assertTrue(e.ok1)
        assertEquals(0.5, cal.resultado().leve, 1e-9)
    }

    @Test
    fun `el umbral leve se limita entre 0,35 y 1,6`() {
        cal.empezar(1, 0)
        muestras(100, { 1.0 })
        cal.tic(5_000)
        assertEquals(1.6, cal.resultado().leve, 1e-9)
    }

    @Test
    fun `paso 2 termina solo al abrir y volver a cerrar`() {
        cal.empezar(1, 0); cal.tic(5_000)
        cal.empezar(2, 10_000)
        muestras(25, { 0.0 }, r = 80.0)      // 0,5 s a 80 °/s = 40°
        muestras(170, { 0.0 }, r = 0.0)      // se detiene: el giro baja 12 °/s hasta menos de 3°
        assertTrue(cal.tic(14_000))
        val e = cal.estado(14_000)
        assertTrue(e.ok2)
        assertEquals(40, e.giro)
        assertEquals(18.0, cal.resultado().ang, 1e-9)     // 40 × 0,45
    }

    @Test
    fun `paso 3 cuenta 3 golpes y calcula el umbral de golpe`() {
        cal.empezar(1, 0); muestras(50, { 0.1 }); cal.tic(5_000)
        cal.empezar(2, 5_000); cal.tic(15_000)
        cal.empezar(3, 15_000)
        repeat(3) { muestras(20, { i -> if (i == 10) 2.0 else 0.05 }) }
        assertTrue(cal.tic(16_000))
        val e = cal.estado(16_000)
        assertEquals(4, e.paso)
        assertEquals(3, e.golpes)
        val r = cal.resultado()
        assertEquals(2.5, r.fuerte, 1e-9)                 // 2,0 × 1,25
        assertEquals(7.5, r.muy, 1e-9)                    // máx(2,5 × 3; 6)
    }

    @Test
    fun `sin datos usa los valores de fabrica`() {
        // Igual que el prototipo: muy = máx(fuerte × 3; 6), así que queda 7,5 y no 8.
        val f = Calibracion.FABRICA
        assertEquals(Calibracion(f.leve, f.fuerte, 7.5, f.ang), cal.resultado())
    }

    @Test
    fun `picos separados por mas de 8 muestras`() {
        val datos = List(30) { i -> if (i == 5 || i == 8 || i == 20) 3.0 else 0.0 }
        assertEquals(listOf(3.0, 3.0), Calibrador.picosDe(datos, 1.0))
    }
}
