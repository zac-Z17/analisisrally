package ni.uni.nanobots.sentinel.data.json

import ni.uni.nanobots.sentinel.domain.modelo.AjusteEquipo
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.Calibracion
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Comando
import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Contenido
import ni.uni.nanobots.sentinel.domain.modelo.Equipo
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.Hogar
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Nivel
import ni.uni.nanobots.sentinel.domain.modelo.Perfil
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodecTest {

    @Test
    fun `lee un mensaje tal como lo manda el prototipo web`() {
        val web = """{"tipo":"evento","clase":"intento","detalle":"Golpe muy fuerte","pico":9.12,
            "equipo":{"id":"ab12cd34","nombre":"Puerta principal"},"mid":"k3m9p2x7qa","de":"ab12cd34","t":1760000000000}"""
        val m = CodecMensajes.decodificar(web)!!
        assertEquals("k3m9p2x7qa", m.mid)
        assertEquals(1760000000000, m.t)
        val c = m.contenido as Contenido.Evento
        assertEquals(ClaseAlerta.INTENTO, c.clase)
        assertEquals("Puerta principal", c.equipoNombre)
        assertEquals(9.12, c.pico!!, 1e-9)
    }

    @Test
    fun `ida y vuelta de todos los tipos de mensaje`() {
        val cfg = Config(v = 5, modo = Modo.ON, equipos = mapOf("ab12" to AjusteEquipo(Nivel.ALTA, 1.3, false)), vecinosOff = setOf("cd34"))
        val contenidos = listOf(
            Contenido.Solicitud(Rol.VECINO, "Doña Marta", Lugar.PUERTA, "Casa de al lado"),
            Contenido.Bienvenida("ab12", Hogar("hogar123456", "Mi casa", "1234"), cfg),
            Contenido.Hola(Rol.RECEPTOR, 5),
            Contenido.Estado("Ventana cocina", Lugar.VENTANA, Vigilancia.VIGILANDO, 64, true, true, 5, 31),
            Contenido.Presencia(Rol.RECEPTOR, "Mamá", "", true, 5),
            Contenido.Cfg(cfg),
            Contenido.Evento(ClaseAlerta.LEVE, "Vibración rara", null, "ab12", "Ventana"),
            Contenido.Orden(Comando.RESUELTA, alerta = "zz99", falsa = true, desde = "Mamá"),
            Contenido.Marca("zz99", Etiqueta.LLUVIA),
        )
        contenidos.forEach { c ->
            val m = Mensaje("mid123", "de456", 42, c)
            assertEquals(m, CodecMensajes.decodificar(CodecMensajes.codificar(m)))
        }
    }

    @Test
    fun `rechaza mensajes con ids invalidos o tipos desconocidos`() {
        assertNull(CodecMensajes.decodificar("""{"tipo":"hola","mid":"<script>","de":"ab12","t":1}"""))
        assertNull(CodecMensajes.decodificar("""{"tipo":"borrar-todo","mid":"ab12","de":"ab12","t":1}"""))
        assertNull(CodecMensajes.decodificar("""{"tipo":"evento","clase":"conexion","mid":"ab12","de":"ab12","t":1}"""))
        assertNull(CodecMensajes.decodificar("no es json"))
    }

    @Test
    fun `una configuracion con basura queda con valores por defecto`() {
        val c = CodecConfig.desde(
            kotlinx.serialization.json.Json.parseToJsonElement(
                """{"v":7,"modo":"turbo","inicio":"25h","dias":[1,0,1],"batMin":"mucho",
                   "equipos":{"ID MALO":{"nivel":"alta"},"ab12":{"nivel":"x","ajuste":9}},"vecinosOff":{"cd34":1}}""",
            ),
        )
        assertEquals(7L, c.v)
        assertEquals(Modo.AUTO, c.modo)
        assertEquals("23:00", c.inicio)
        assertEquals(List(7) { true }, c.dias)
        assertEquals(60, c.batMin)
        assertEquals(mapOf("ab12" to AjusteEquipo(Nivel.MEDIA, 2.5, true)), c.equipos)
        assertEquals(setOf("cd34"), c.vecinosOff)
    }

    @Test
    fun `los dias viajan como 1 y 0`() {
        val json = CodecConfig.json(Config(dias = listOf(true, false, true, true, true, true, false))).toString()
        assertEquals(true, json.contains("\"dias\":[1,0,1,1,1,1,0]"))
    }

    @Test
    fun `ida y vuelta del estado guardado`() {
        val e = EstadoApp(
            yo = Perfil("yo123456", "Mi celular", Rol.RECEPTOR, Lugar.PUERTA, ""),
            hogar = Hogar("hogar123456", "Mi casa", "1234"),
            admin = true,
            cfg = Config(v = 3),
            equipos = mapOf("ab12" to Equipo("ab12", Rol.SENTINELA, "Puerta", Lugar.PUERTA, bat = 80, temp = 30, visto = 9)),
            alertas = listOf(Alerta("a1", ClaseAlerta.LEVE, "ab12", "Puerta", 5, "Vibración", Etiqueta.VIENTO, true)),
            cal = Calibracion(0.5, 2.0, 6.0, 12.0),
            avisos = mapOf("calor" to 77L),
        )
        assertEquals(e, CodecEstado.decodificar(CodecEstado.codificar(e)))
    }
}
