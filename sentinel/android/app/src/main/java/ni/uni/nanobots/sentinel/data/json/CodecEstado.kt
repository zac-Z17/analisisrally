package ni.uni.nanobots.sentinel.data.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import ni.uni.nanobots.sentinel.comun.Ids
import ni.uni.nanobots.sentinel.domain.modelo.Alerta
import ni.uni.nanobots.sentinel.domain.modelo.Calibracion
import ni.uni.nanobots.sentinel.domain.modelo.ClaseAlerta
import ni.uni.nanobots.sentinel.domain.modelo.Equipo
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.modelo.Etiqueta
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.MAX_ALERTAS
import ni.uni.nanobots.sentinel.domain.modelo.Pendiente
import ni.uni.nanobots.sentinel.domain.modelo.Perfil
import ni.uni.nanobots.sentinel.domain.modelo.RELAY_DEF
import ni.uni.nanobots.sentinel.domain.modelo.Rol
import ni.uni.nanobots.sentinel.domain.modelo.Vigilancia

/** Guarda el estado completo de este equipo como JSON (se valida también al leer). */
object CodecEstado {

    fun codificar(e: EstadoApp): String = buildJsonObject {
        put("version", 1)
        putJsonObject("yo") {
            put("id", e.yo.id); put("nombre", e.yo.nombre); e.yo.rol?.let { put("rol", it.clave) }
            put("lugar", e.yo.lugar.clave); put("ref", e.yo.ref)
        }
        e.hogar?.let { put("hogar", CodecHogar.json(it)) }
        put("admin", e.admin)
        put("cfg", CodecConfig.json(e.cfg))
        putJsonObject("equipos") { e.equipos.forEach { (id, q) -> put(id, equipo(q)) } }
        put("pendientes", buildJsonArray { e.pendientes.forEach { add(pendiente(it)) } })
        put("alertas", buildJsonArray { e.alertas.forEach { add(alerta(it)) } })
        e.cal?.let {
            putJsonObject("cal") { put("leve", it.leve); put("fuerte", it.fuerte); put("muy", it.muy); put("ang", it.ang) }
        }
        put("relay", e.relay)
        put("demo", e.demo)
        putJsonObject("avisos") { e.avisos.forEach { (k, v) -> put(k, v) } }
    }.toString()

    fun decodificar(texto: String): EstadoApp? {
        val o = runCatching { Json.parseToJsonElement(texto) }.getOrNull() as? JsonObject ?: return null
        val yo = o["yo"] as? JsonObject ?: return null
        val id = yo["id"].texto()?.takeIf(Ids::valido) ?: return null
        return EstadoApp(
            yo = Perfil(
                id = id,
                nombre = yo["nombre"].txt(),
                rol = Rol.de(yo["rol"].texto()),
                lugar = Lugar.de(yo["lugar"].texto()),
                ref = yo["ref"].txt(),
            ),
            hogar = CodecHogar.desde(o["hogar"]),
            admin = o["admin"].booleano() ?: false,
            cfg = CodecConfig.desde(o["cfg"]),
            equipos = (o["equipos"] as? JsonObject)?.mapNotNull { (k, v) -> equipoDe(k, v)?.let { k to it } }?.toMap().orEmpty(),
            pendientes = (o["pendientes"] as? JsonArray)?.mapNotNull(::pendienteDe).orEmpty(),
            alertas = (o["alertas"] as? JsonArray)?.mapNotNull(::alertaDe)?.take(MAX_ALERTAS).orEmpty(),
            cal = (o["cal"] as? JsonObject)?.let { c ->
                val leve = c["leve"].numero(); val fuerte = c["fuerte"].numero()
                val muy = c["muy"].numero(); val ang = c["ang"].numero()
                if (leve != null && fuerte != null && muy != null && ang != null) Calibracion(leve, fuerte, muy, ang) else null
            },
            relay = o["relay"].texto()?.takeIf { it.startsWith("http") } ?: RELAY_DEF,
            demo = o["demo"].booleano() ?: false,
            avisos = (o["avisos"] as? JsonObject)?.mapNotNull { (k, v) -> v.numero()?.let { k to it.toLong() } }?.toMap().orEmpty(),
        )
    }

    private fun equipo(q: Equipo) = buildJsonObject {
        put("rol", q.rol.clave); put("nombre", q.nombre); put("lugar", q.lugar.clave); put("ref", q.ref)
        put("admin", q.admin); q.vig?.let { put("vig", it.clave) }; putN("bat", q.bat); put("carga", q.carga)
        put("sensoresOk", q.sensoresOk); putN("temp", q.temp); put("visto", q.visto)
        put("offAvisado", q.offAvisado); put("demo", q.demo)
    }

    private fun equipoDe(id: String, x: JsonElement): Equipo? {
        val o = x as? JsonObject ?: return null
        if (!Ids.valido(id)) return null
        return Equipo(
            id = id,
            rol = Rol.de(o["rol"].texto()) ?: return null,
            nombre = o["nombre"].txt(),
            lugar = Lugar.de(o["lugar"].texto()),
            ref = o["ref"].txt(),
            admin = o["admin"].booleano() ?: false,
            vig = Vigilancia.de(o["vig"].texto()),
            bat = o["bat"].entero(0, 100),
            carga = o["carga"].booleano() ?: false,
            sensoresOk = o["sensoresOk"].booleano() ?: true,
            temp = o["temp"].entero(-40, 120),
            visto = o["visto"].numero()?.toLong() ?: 0,
            offAvisado = o["offAvisado"].booleano() ?: false,
            demo = o["demo"].booleano() ?: false,
        )
    }

    private fun pendiente(p: Pendiente) = buildJsonObject {
        put("id", p.id); put("nombre", p.nombre); put("tel", p.tel); put("ref", p.ref)
    }

    private fun pendienteDe(x: JsonElement): Pendiente? {
        val o = x as? JsonObject ?: return null
        return Pendiente(o["id"].texto() ?: return null, o["nombre"].txt(), o["tel"].txt(), o["ref"].txt())
    }

    private fun alerta(a: Alerta) = buildJsonObject {
        put("mid", a.mid); put("clase", a.clase.clave); put("equipoId", a.equipoId); put("nombre", a.nombre)
        put("t", a.t); put("detalle", a.detalle); a.que?.let { put("que", it.clave) }; put("resuelta", a.resuelta)
    }

    private fun alertaDe(x: JsonElement): Alerta? {
        val o = x as? JsonObject ?: return null
        return Alerta(
            mid = o["mid"].texto() ?: return null,
            clase = ClaseAlerta.de(o["clase"].texto()) ?: return null,
            equipoId = o["equipoId"].txt(),
            nombre = o["nombre"].txt(),
            t = o["t"].numero()?.toLong() ?: 0,
            detalle = o["detalle"].txt(120),
            que = Etiqueta.de(o["que"].texto()),
            resuelta = o["resuelta"].booleano() ?: false,
        )
    }

    private fun JsonObjectBuilder.putN(clave: String, valor: Int?) {
        if (valor != null) put(clave, valor) else put(clave, JsonNull)
    }
}
