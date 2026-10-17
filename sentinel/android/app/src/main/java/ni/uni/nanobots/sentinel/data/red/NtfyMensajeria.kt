package ni.uni.nanobots.sentinel.data.red

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import ni.uni.nanobots.sentinel.data.json.CodecMensajes
import ni.uni.nanobots.sentinel.data.json.texto
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje
import ni.uni.nanobots.sentinel.domain.puertos.Mensajeria
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.math.min

/**
 * Mensajería por un servidor ntfy (https://ntfy.sh por defecto): publica con POST
 * y escucha el flujo JSON del tema (una línea por evento), reconectándose sola.
 */
class NtfyMensajeria(
    private val http: OkHttpClient,
    private val servidor: () -> String,
) : Mensajeria {

    private val _conectado = MutableStateFlow<Boolean?>(null)
    override val conectado: StateFlow<Boolean?> = _conectado.asStateFlow()

    override suspend fun enviar(tema: String, mensaje: Mensaje): Boolean = withContext(Dispatchers.IO) {
        val url = url(tema) ?: return@withContext false
        val ok = try {
            val pedido = Request.Builder().url(url).post(CodecMensajes.codificar(mensaje).toRequestBody(JSON)).build()
            http.newCall(pedido).execute().use { it.isSuccessful }
        } catch (e: IOException) {
            false
        }
        _conectado.value = ok
        ok
    }

    override fun escuchar(tema: String, desde: String?): Flow<Mensaje> = channelFlow {
        var since = desde
        var espera = ESPERA_INICIAL_MS
        while (isActive) {
            val base = url(tema)
            if (base == null) {
                _conectado.value = false
                delay(ESPERA_MAX_MS)
                continue
            }
            val url = base.newBuilder().addPathSegment("json").apply { since?.let { addQueryParameter("since", it) } }.build()
            val llamada = http.newCall(Request.Builder().url(url).build())
            try {
                coroutineScope {
                    val lector = launch(Dispatchers.IO) {
                        llamada.execute().use { r ->
                            if (!r.isSuccessful) throw IOException("HTTP ${r.code}")
                            val fuente = r.body?.source() ?: throw IOException("sin cuerpo")
                            while (true) {
                                val linea = fuente.readUtf8Line() ?: break
                                val ev = runCatching { Json.parseToJsonElement(linea) }.getOrNull() as? JsonObject ?: continue
                                when (ev["event"].texto()) {
                                    "open" -> {
                                        _conectado.value = true
                                        espera = ESPERA_INICIAL_MS
                                    }
                                    "message" -> {
                                        ev["id"].texto()?.let { since = it }
                                        val m = ev["message"].texto()?.let(CodecMensajes::decodificar) ?: continue
                                        send(m)
                                    }
                                }
                            }
                        }
                    }
                    // Si se cancela la escucha, cortar la conexión desbloquea al lector.
                    try {
                        lector.join()
                    } finally {
                        llamada.cancel()
                    }
                }
            } catch (e: IOException) {
                _conectado.value = false
            }
            delay(espera)
            espera = min(espera * 2, ESPERA_MAX_MS)
        }
    }

    private fun url(tema: String) = servidor().trimEnd('/').toHttpUrlOrNull()?.newBuilder()?.addPathSegment(tema)?.build()

    private companion object {
        val JSON = "application/json".toMediaType()
        const val ESPERA_INICIAL_MS = 1_000L
        const val ESPERA_MAX_MS = 30_000L
    }
}
