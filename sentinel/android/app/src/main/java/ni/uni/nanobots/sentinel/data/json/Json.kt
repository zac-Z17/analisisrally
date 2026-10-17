package ni.uni.nanobots.sentinel.data.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import ni.uni.nanobots.sentinel.comun.Ids
import ni.uni.nanobots.sentinel.domain.modelo.AjusteEquipo
import ni.uni.nanobots.sentinel.domain.modelo.Config
import ni.uni.nanobots.sentinel.domain.modelo.Hogar
import ni.uni.nanobots.sentinel.domain.modelo.Modo
import ni.uni.nanobots.sentinel.domain.modelo.Nivel
import kotlin.math.roundToInt

/*
 * Lectura tolerante de JSON: lo que llega por la red puede venir mal o con mala intención,
 * así que cada campo se valida y, si no sirve, se usa el valor por defecto (como limpiarCfg()).
 */

internal fun JsonElement?.texto(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.content

/** Como String(v ?? '').slice(0, n) del prototipo. */
internal fun JsonElement?.txt(max: Int = 40): String =
    ((this as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content ?: "").take(max)

internal fun JsonElement?.numero(): Double? =
    (this as? JsonPrimitive)?.takeIf { !it.isString && it !is JsonNull }?.doubleOrNull?.takeIf { it.isFinite() }

internal fun JsonElement?.booleano(): Boolean? =
    (this as? JsonPrimitive)?.takeIf { !it.isString && it !is JsonNull }?.booleanOrNull

/** Verdadero como en JavaScript (para los días: 1/0 o true/false). */
internal fun JsonElement?.verdadero(): Boolean = when (this) {
    null, is JsonNull -> false
    is JsonPrimitive -> booleano() ?: numero()?.let { it != 0.0 } ?: content.isNotEmpty()
    else -> true
}

internal fun JsonElement?.entero(min: Int, max: Int): Int? = numero()?.roundToInt()?.coerceIn(min, max)

private val HHMM = Regex("^\\d{2}:\\d{2}$")
private val PIN = Regex("^\\d{4}$")

object CodecConfig {
    fun desde(x: JsonElement?): Config {
        val b = Config()
        val o = x as? JsonObject ?: return b
        fun num(k: String) = o[k].numero()
        fun bool(k: String, def: Boolean) = o[k].booleano() ?: def
        fun hora(k: String, def: String) = o[k].texto()?.takeIf { HHMM.matches(it) } ?: def
        return Config(
            v = num("v")?.toLong() ?: b.v,
            modo = Modo.de(o["modo"].texto()) ?: b.modo,
            modoHasta = num("modoHasta")?.toLong() ?: b.modoHasta,
            pausaHasta = num("pausaHasta")?.toLong() ?: b.pausaHasta,
            auto = bool("auto", b.auto),
            inicio = hora("inicio", b.inicio),
            fin = hora("fin", b.fin),
            dias = (o["dias"] as? JsonArray)?.takeIf { it.size == 7 }?.map { it.verdadero() } ?: b.dias,
            carga = bool("carga", b.carga),
            cargaIni = hora("cargaIni", b.cargaIni),
            cargaFin = hora("cargaFin", b.cargaFin),
            aviso10 = bool("aviso10", b.aviso10),
            batMin = o["batMin"].entero(0, 100) ?: b.batMin,
            avisoTemp = bool("avisoTemp", b.avisoTemp),
            tempMax = o["tempMax"].entero(0, 100) ?: b.tempMax,
            fuera = bool("fuera", b.fuera),
            equipos = (o["equipos"] as? JsonObject)?.mapNotNull { (id, e) ->
                val eo = e as? JsonObject
                if (!Ids.valido(id) || eo == null) {
                    null
                } else {
                    id to AjusteEquipo(
                        nivel = Nivel.de(eo["nivel"].texto()) ?: Nivel.MEDIA,
                        ajuste = eo["ajuste"].numero()?.coerceIn(1.0, 2.5) ?: 1.0,
                        sirena = eo["sirena"].booleano() != false,
                    )
                }
            }?.toMap() ?: emptyMap(),
            vecinosOff = (o["vecinosOff"] as? JsonObject)?.keys?.filter(Ids::valido)?.toSet() ?: emptySet(),
        )
    }

    fun json(c: Config): JsonObject = buildJsonObject {
        put("v", c.v)
        put("modo", c.modo.clave)
        put("modoHasta", c.modoHasta)
        put("pausaHasta", c.pausaHasta)
        put("auto", c.auto)
        put("inicio", c.inicio)
        put("fin", c.fin)
        putJsonArray("dias") { c.dias.forEach { add(JsonPrimitive(if (it) 1 else 0)) } }
        put("carga", c.carga)
        put("cargaIni", c.cargaIni)
        put("cargaFin", c.cargaFin)
        put("aviso10", c.aviso10)
        put("batMin", c.batMin)
        put("avisoTemp", c.avisoTemp)
        put("tempMax", c.tempMax)
        put("fuera", c.fuera)
        putJsonObject("equipos") {
            c.equipos.forEach { (id, a) ->
                putJsonObject(id) {
                    put("nivel", a.nivel.clave)
                    put("ajuste", a.ajuste)
                    put("sirena", a.sirena)
                }
            }
        }
        putJsonObject("vecinosOff") { c.vecinosOff.forEach { put(it, true) } }
    }
}

object CodecHogar {
    fun desde(x: JsonElement?): Hogar? {
        val o = x as? JsonObject ?: return null
        val id = o["id"].texto()?.takeIf(Ids::valido) ?: return null
        return Hogar(
            id = id,
            nombre = o["nombre"].txt().ifBlank { "Mi casa" },
            pin = o["pin"].texto()?.takeIf { PIN.matches(it) } ?: "",
        )
    }

    fun json(h: Hogar): JsonObject = buildJsonObject {
        put("id", h.id)
        put("nombre", h.nombre)
        put("pin", h.pin)
    }
}
