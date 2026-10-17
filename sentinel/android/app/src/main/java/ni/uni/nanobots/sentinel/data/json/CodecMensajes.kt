package ni.uni.nanobots.sentinel.data.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import ni.uni.nanobots.sentinel.comun.Ids
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Comando
import ni.uni.nanobots.sentinel.domain.modelo.Contenido
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Mensaje
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia

/** Convierte los mensajes del protocolo Sentinel a JSON y de vuelta, validando todo lo que llega. */
object CodecMensajes {

    fun codificar(m: Mensaje): String = buildJsonObject {
        put("mid", m.mid)
        put("de", m.de)
        put("t", m.t)
        when (val c = m.contenido) {
            is Contenido.Solicitud -> {
                put("tipo", "solicitud"); put("rol", c.rol.clave); put("nombre", c.nombre)
                put("lugar", c.lugar.clave); put("ref", c.ref)
            }
            is Contenido.Bienvenida -> {
                put("tipo", "bienvenida"); put("para", c.para)
                put("hogar", CodecHogar.json(c.hogar)); put("cfg", CodecConfig.json(c.cfg))
            }
            is Contenido.Hola -> {
                put("tipo", "hola"); c.rol?.let { put("rol", it.clave) }; put("cfgv", c.cfgv)
            }
            is Contenido.Estado -> {
                put("tipo", "estado"); put("rol", Rol.SENTINELA.clave); put("nombre", c.nombre)
                put("lugar", c.lugar.clave); c.vig?.let { put("vig", it.clave) }
                putNullable("bat", c.bat); put("carga", c.carga)
                put("sensores", if (c.sensoresOk) "ok" else "sin"); put("cfgv", c.cfgv)
                c.temp?.let { put("temp", it) }
            }
            is Contenido.Presencia -> {
                put("tipo", "presencia"); put("rol", c.rol.clave); put("nombre", c.nombre)
                put("ref", c.ref); put("admin", c.admin); put("cfgv", c.cfgv)
            }
            is Contenido.Cfg -> {
                put("tipo", "cfg"); put("cfg", CodecConfig.json(c.cfg))
            }
            is Contenido.Evento -> {
                put("tipo", "evento"); put("clase", c.clase.clave); put("detalle", c.detalle)
                if (c.pico != null) put("pico", c.pico) else put("pico", JsonNull)
                putJsonObject("equipo") { put("id", c.equipoId); put("nombre", c.equipoNombre) }
            }
            is Contenido.Orden -> {
                put("tipo", "cmd"); put("cmd", c.cmd.clave)
                c.para?.let { put("para", it) }; c.alerta?.let { put("alerta", it) }
                if (c.falsa) put("falsa", true)
                put("desde", c.desde)
            }
            is Contenido.Marca -> {
                put("tipo", "etiqueta"); put("alerta", c.alerta); put("que", c.que.clave)
            }
        }
    }.toString()

    /** null si el texto no es un mensaje válido de Sentinel. */
    fun decodificar(texto: String): Mensaje? {
        val o = runCatching { Json.parseToJsonElement(texto) }.getOrNull() as? JsonObject ?: return null
        val mid = o["mid"].texto()?.takeIf(Ids::valido) ?: return null
        val de = o["de"].texto()?.takeIf(Ids::valido) ?: return null
        val t = o["t"].numero()?.toLong() ?: 0L
        val contenido = contenido(o) ?: return null
        return Mensaje(mid, de, t, contenido)
    }

    private fun contenido(o: JsonObject): Contenido? {
        return when (o["tipo"].texto()) {
            "solicitud" -> Contenido.Solicitud(
                rol = Rol.de(o["rol"].texto()) ?: return null,
                nombre = o["nombre"].txt(),
                lugar = Lugar.de(o["lugar"].texto()),
                ref = o["ref"].txt(),
            )
            "bienvenida" -> Contenido.Bienvenida(
                para = o["para"].texto() ?: return null,
                hogar = CodecHogar.desde(o["hogar"]) ?: return null,
                cfg = CodecConfig.desde(o["cfg"]),
            )
            "hola" -> Contenido.Hola(Rol.de(o["rol"].texto()), o["cfgv"].numero()?.toLong() ?: 0)
            "estado" -> Contenido.Estado(
                nombre = o["nombre"].txt(),
                lugar = Lugar.de(o["lugar"].texto()),
                vig = Vigilancia.de(o["vig"].texto()),
                bat = o["bat"].entero(0, 100),
                carga = o["carga"].verdadero(),
                sensoresOk = o["sensores"].texto() != "sin",
                cfgv = o["cfgv"].numero()?.toLong() ?: 0,
                temp = o["temp"].entero(-40, 120),
            )
            "presencia" -> Contenido.Presencia(
                rol = Rol.de(o["rol"].texto()) ?: return null,
                nombre = o["nombre"].txt(),
                ref = o["ref"].txt(),
                admin = o["admin"].verdadero(),
                cfgv = o["cfgv"].numero()?.toLong() ?: 0,
            )
            "cfg" -> Contenido.Cfg(CodecConfig.desde(o["cfg"] as? JsonObject ?: return null))
            "evento" -> Contenido.Evento(
                clase = ClaseAlerta.deRed(o["clase"].texto()) ?: return null,
                detalle = o["detalle"].txt(120),
                pico = o["pico"].numero(),
                equipoId = (o["equipo"] as? JsonObject)?.get("id").txt(24),
                equipoNombre = (o["equipo"] as? JsonObject)?.get("nombre").txt(),
            )
            "cmd" -> Contenido.Orden(
                cmd = Comando.de(o["cmd"].texto()) ?: return null,
                para = o["para"].texto(),
                alerta = o["alerta"].texto(),
                falsa = o["falsa"].verdadero(),
                desde = o["desde"].txt(),
            )
            "etiqueta" -> Contenido.Marca(
                alerta = o["alerta"].texto() ?: return null,
                que = Etiqueta.de(o["que"].texto()) ?: return null,
            )
            else -> null
        }
    }

    private fun JsonObjectBuilder.putNullable(clave: String, valor: Int?) {
        if (valor != null) put(clave, valor) else put(clave, JsonNull)
    }
}
