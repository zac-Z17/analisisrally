package ni.uni.nanobots.sentinel.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import ni.uni.nanobots.sentinel.data.json.CodecEstado
import ni.uni.nanobots.sentinel.domain.modelo.EstadoApp
import ni.uni.nanobots.sentinel.domain.puertos.AlmacenEstado
import java.io.IOException

private val Context.almacen by preferencesDataStore(name = "sentinel")

/** Guarda el estado en DataStore como un solo JSON. */
class AlmacenDataStore(private val context: Context) : AlmacenEstado {
    private val clave = stringPreferencesKey("estado")

    override suspend fun leer(): EstadoApp? = try {
        context.almacen.data.first()[clave]?.let(CodecEstado::decodificar)
    } catch (e: IOException) {
        null
    }

    override suspend fun escribir(estado: EstadoApp) {
        try {
            context.almacen.edit { it[clave] = CodecEstado.codificar(estado) }
        } catch (e: IOException) {
            // Sin espacio o disco dañado: se sigue trabajando en memoria.
        }
    }
}
