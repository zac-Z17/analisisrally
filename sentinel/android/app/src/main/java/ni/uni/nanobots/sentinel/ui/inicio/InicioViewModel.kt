package ni.uni.nanobots.sentinel.ui.inicio

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ni.uni.nanobots.sentinel.di.Contenedor
import ni.uni.nanobots.sentinel.domain.hogar.EstadoUnion
import ni.uni.nanobots.sentinel.domain.modelo.Lugar
import ni.uni.nanobots.sentinel.domain.modelo.Rol

enum class ModoReceptor { CREAR, UNIR }

data class FormInicio(
    /** false = elegir rol; true = formulario para unirse o crear el hogar. */
    val enFormulario: Boolean = false,
    val rol: Rol = Rol.SENTINELA,
    val modoReceptor: ModoReceptor = ModoReceptor.CREAR,
    val lugar: Lugar = Lugar.PUERTA,
    val codigo: String = "",
    val nombre: String = "",
    val ref: String = "",
    val casa: String = "",
    val pin: String = "",
    val error: String? = null,
)

class InicioViewModel(private val c: Contenedor) : ViewModel() {
    private val _form = MutableStateFlow(FormInicio(nombre = c.repo.estado.value.yo.nombre))
    val form: StateFlow<FormInicio> = _form.asStateFlow()
    val union: StateFlow<EstadoUnion> = c.vinculador.union

    fun elegirRol(r: Rol) = _form.update { it.copy(rol = r, enFormulario = r == Rol.VECINO, error = null) }
    fun continuar() = _form.update { it.copy(enFormulario = true, error = null) }
    fun volver() {
        c.vinculador.cancelarUnion()
        _form.update { it.copy(enFormulario = false, error = null) }
    }

    fun modoReceptor(m: ModoReceptor) = editar { it.copy(modoReceptor = m) }
    fun lugar(l: Lugar) = editar { it.copy(lugar = l) }
    fun codigo(v: String) = editar { it.copy(codigo = v.filter(Char::isDigit).take(6)) }
    fun nombre(v: String) = editar { it.copy(nombre = v) }
    fun ref(v: String) = editar { it.copy(ref = v) }
    fun casa(v: String) = editar { it.copy(casa = v) }
    fun pin(v: String) = editar { it.copy(pin = v.filter(Char::isDigit).take(4)) }

    private fun editar(cambio: (FormInicio) -> FormInicio) {
        c.vinculador.limpiarError()
        _form.update { cambio(it).copy(error = null) }
    }

    fun unirse() {
        val f = _form.value
        c.vinculador.unirse(f.rol, f.codigo, f.nombre, f.ref, f.lugar)
    }

    fun crearHogar() {
        val f = _form.value
        val error = c.vinculador.crearHogar(f.casa, f.nombre, f.pin)
        if (error != null) {
            _form.update { it.copy(error = error) }
            return
        }
        // Igual que el prototipo: al crear el hogar se abre «Vincular» para el primer Sentinela.
        c.vinculador.abrir(Rol.SENTINELA)
    }

    fun demo() = c.coordinador.cargarDemo()

    /**
     * Acepta el enlace del QR («…#unir-482913-s») o solo los 6 números.
     * Devuelve false si el texto no sirve.
     */
    fun codigoEscaneado(texto: String?): Boolean {
        val t = texto.orEmpty().trim()
        val enlace = Regex("#unir-(\\d{6})-([srv])").find(t)
        val codigo = enlace?.groupValues?.get(1) ?: t.takeIf { Regex("^\\d{6}$").matches(it) } ?: return false
        val rol = when (enlace?.groupValues?.get(2)) {
            "s" -> Rol.SENTINELA
            "r" -> Rol.RECEPTOR
            "v" -> Rol.VECINO
            else -> null
        }
        _form.update {
            it.copy(
                codigo = codigo,
                rol = rol ?: it.rol,
                modoReceptor = if ((rol ?: it.rol) == Rol.RECEPTOR) ModoReceptor.UNIR else it.modoReceptor,
                enFormulario = true,
                error = null,
            )
        }
        return true
    }

    fun avisar(texto: String) = c.coordinador.avisar(texto)
}
