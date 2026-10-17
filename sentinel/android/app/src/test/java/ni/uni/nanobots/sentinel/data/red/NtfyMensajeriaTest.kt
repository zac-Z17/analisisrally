package ni.uni.nanobots.sentinel.data.red

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import ni.uni.nanobots.sentinel.data.json.CodecMensajes
import ni.uni.nanobots.sentinel.domain.modelo.Contenido
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NtfyMensajeriaTest {
    private val servidor = MockWebServer().apply { start() }
    private val red = NtfyMensajeria(OkHttpClient()) { servidor.url("/").toString() }

    @After
    fun cerrar() = servidor.shutdown()

    @Test
    fun `publica el mensaje como JSON en el tema`() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(200))
        val m = Mensaje("mid1", "de1", 5, Contenido.Hola(Rol.RECEPTOR, 3))
        assertTrue(red.enviar("snt-casa", m))
        val pedido = servidor.takeRequest()
        assertEquals("POST", pedido.method)
        assertEquals("/snt-casa", pedido.path)
        assertEquals(m, CodecMensajes.decodificar(pedido.body.readUtf8()))
        assertEquals(true, red.conectado.value)
    }

    @Test
    fun `escucha el flujo JSON e ignora lo que no es de Sentinel`() = runBlocking {
        val m = Mensaje("mid2", "de2", 7, Contenido.Hola(Rol.SENTINELA, 1))
        val texto = CodecMensajes.codificar(m).replace("\"", "\\\"")
        val cuerpo = listOf(
            """{"id":"a","event":"open","topic":"snt-casa"}""",
            """{"id":"b","event":"keepalive"}""",
            """{"id":"c","event":"message","message":"hola, no soy JSON"}""",
            """{"id":"d","event":"message","message":"$texto"}""",
        ).joinToString("\n", postfix = "\n")
        servidor.enqueue(MockResponse().setBody(cuerpo))
        val recibido = withTimeout(5_000) { red.escuchar("snt-casa", "3m").first() }
        assertEquals(m, recibido)
        assertEquals("/snt-casa/json?since=3m", servidor.takeRequest().path)
    }
}
