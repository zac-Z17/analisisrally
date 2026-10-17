package ni.uni.nanobots.sentinel.domain.hogar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Comando
import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Contenido
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.Hogar
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Perfil
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Temas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoordinadorTest {
    private val hogar = Hogar("hogar1234567", "Mi casa", "1234")

    /** Un equipo completo (estado, coordinador y vinculador) conectado a la red en memoria. */
    private class Equipo(scope: CoroutineScope, test: TestScope, red: RedEnMemoria, inicial: EstadoApp?) {
        val efectos = EfectosFalsos()
        val reloj = RelojVirtual(test.testScheduler)
        val repo = RepositorioEstado(AlmacenEnMemoria(inicial), scope)
        val co = Coordinador(repo, red, efectos, reloj, scope)
        val vinc = Vinculador(repo, red, co, efectos, reloj, scope)
        val e get() = repo.estado.value
    }

    private fun TestScope.equipo(red: RedEnMemoria, inicial: EstadoApp?) =
        Equipo(backgroundScope, this, red, inicial).also { runCurrent() }

    private fun receptor(rol: Rol = Rol.RECEPTOR, cfg: Config = Config()) =
        EstadoApp(yo = Perfil("yo1234", "Mamá", rol), hogar = hogar, cfg = cfg)

    private fun Equipo.recibir(c: Contenido, de: String = "sent1", mid: String = "m" + (0..99999).random(), atras: Long = 0) =
        co.alMensaje(Mensaje(mid, de, reloj.ahora() - atras, c))

    private fun intento() = Contenido.Evento(ClaseAlerta.INTENTO, "Golpe muy fuerte", 9.0, "sent1", "Puerta principal")

    @Test
    fun `el receptor muestra la alarma una sola vez por intento`() = runTest {
        val r = equipo(RedEnMemoria(), receptor())
        r.recibir(intento(), mid = "m1")
        r.recibir(intento(), mid = "m1")
        assertEquals("m1", r.co.alarma.value?.mid)
        assertEquals(1, r.efectos.alarmas.size)
        assertEquals(1, r.e.alertas.size)
        assertEquals("Puerta principal", r.e.alertas.first().nombre)
    }

    @Test
    fun `el vecino solo recibe intentos y nada si la familia lo pauso`() = runTest {
        val v = equipo(RedEnMemoria(), receptor(Rol.VECINO, Config(vecinosOff = setOf("yo1234"))))
        v.recibir(Contenido.Evento(ClaseAlerta.LEVE, "Vibración", 1.0, "sent1", "Puerta"))
        assertTrue(v.e.alertas.isEmpty())
        v.recibir(intento())
        assertNull(v.co.alarma.value)
        assertEquals(1, v.e.alertas.size)
    }

    @Test
    fun `gana la configuracion mas nueva`() = runTest {
        val r = equipo(RedEnMemoria(), receptor(cfg = Config(v = 10)))
        r.recibir(Contenido.Cfg(Config(v = 5, modo = Modo.ON)))
        assertEquals(Modo.AUTO, r.e.cfg.modo)
        r.recibir(Contenido.Cfg(Config(v = 20, modo = Modo.ON)))
        assertEquals(Modo.ON, r.e.cfg.modo)
    }

    @Test
    fun `las ordenes de hace mas de 90 s se ignoran`() = runTest {
        val s = equipo(RedEnMemoria(), receptor(Rol.SENTINELA))
        s.recibir(Contenido.Orden(Comando.SIRENA, desde = "Mamá"), atras = 120_000)
        assertTrue(s.efectos.sirenas.isEmpty())
        s.recibir(Contenido.Orden(Comando.SIRENA, desde = "Mamá"))
        assertEquals(listOf(20), s.efectos.sirenas)
    }

    @Test
    fun `responder lluvia baja la sensibilidad del sentinela`() = runTest {
        val red = RedEnMemoria()
        val r = equipo(red, receptor())
        r.recibir(Contenido.Evento(ClaseAlerta.LEVE, "Vibración", 1.0, "sent1", "Ventana"), mid = "leve1")
        r.co.etiquetar("leve1", Etiqueta.LLUVIA)
        runCurrent()
        assertEquals(1.15, r.e.cfg.ajusteDe("sent1").ajuste, 1e-9)
        assertEquals(Etiqueta.LLUVIA, r.e.alertas.first().que)
        val tipos = red.enviados.map { it.second.contenido::class.simpleName }
        assertTrue(tipos.containsAll(listOf("Marca", "Cfg")))
    }

    @Test
    fun `el sentinela avisa el intento y hace sonar su sirena`() = runTest {
        val red = RedEnMemoria()
        val s = equipo(red, receptor(Rol.SENTINELA))
        s.co.emitirEvento(ClaseAlerta.INTENTO, "Golpe muy fuerte", 9.123)
        runCurrent()
        val (tema, m) = red.enviados.last()
        assertEquals(Temas.hogar(hogar.id), tema)
        assertEquals(9.12, (m.contenido as Contenido.Evento).pico!!, 1e-9)
        assertEquals(listOf(15), s.efectos.sirenas)
        assertEquals(ClaseAlerta.INTENTO, s.e.alertas.first().clase)
    }

    @Test
    fun `vincular un sentinela y recibir su alerta de punta a punta`() = runTest {
        val red = RedEnMemoria()
        val admin = equipo(red, null)
        val nuevo = equipo(red, null)

        assertNull(admin.vinc.crearHogar("Mi casa", "Mi celular", "4321"))
        admin.vinc.abrir(Rol.SENTINELA)
        runCurrent()
        val codigo = admin.vinc.vinculacion.value!!.codigo

        nuevo.vinc.unirse(Rol.SENTINELA, codigo, "Puerta principal", "", Lugar.PUERTA)
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(admin.e.hogar, nuevo.e.hogar)
        assertEquals(Rol.SENTINELA, admin.e.equipos[nuevo.e.yo.id]?.rol)
        assertEquals(listOf(nuevo.e.yo.id), admin.vinc.vinculacion.value!!.unidos)

        nuevo.co.emitirEvento(ClaseAlerta.INTENTO, "Golpes fuertes repetidos (forcejeo)", 3.1)
        runCurrent()
        assertNotNull(admin.co.alarma.value)
        assertEquals("Puerta principal", admin.co.alarma.value?.nombre)

        // «Fue falsa alarma» cierra la alarma y calla al Centinela.
        nuevo.efectos.sirenaSonando.value = true
        admin.co.marcarFalsaAlarma()
        runCurrent()
        assertNull(admin.co.alarma.value)
        assertEquals(false, nuevo.efectos.sirenaSonando.value)
        assertEquals(Etiqueta.FALSA, nuevo.e.alertas.first().que)
    }

    @Test
    fun `un codigo mal escrito no se envia`() = runTest {
        val n = equipo(RedEnMemoria(), null)
        n.vinc.unirse(Rol.SENTINELA, "12", "Puerta", "", Lugar.PUERTA)
        assertEquals(EstadoUnion.Error("Escribe los 6 números del código."), n.vinc.union.value)
    }
}
