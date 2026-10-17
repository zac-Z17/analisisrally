package ni.uni.nanobots.sentinel.ui.comp

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.uni.nanobots.sentinel.ui.tema.Fuentes
import ni.uni.nanobots.sentinel.ui.tema.Iconos
import ni.uni.nanobots.sentinel.ui.tema.Paleta
import ni.uni.nanobots.sentinel.ui.tema.Tipo
import ni.uni.nanobots.sentinel.ui.tema.Tono

val RadioTarjeta = RoundedCornerShape(18.dp)
val RadioBoton = RoundedCornerShape(14.dp)

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    borde: Color = Paleta.Borde,
    fondo: Brush = SolidColor(Paleta.Sup),
    relleno: PaddingValues = PaddingValues(14.dp),
    alTocar: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RadioTarjeta)
            .background(fondo)
            .border(1.dp, borde, RadioTarjeta)
            .let { if (alTocar != null) it.clickable(onClick = alTocar) else it }
            .padding(relleno),
        content = contenido,
    )
}

/** Tarjeta con filas separadas por una línea. */
@Composable
fun ListaTarjeta(filas: List<@Composable () -> Unit>, vacio: String? = null) {
    Tarjeta(relleno = PaddingValues(horizontal = 14.dp, vertical = 2.dp)) {
        if (filas.isEmpty() && vacio != null) {
            Text(vacio, style = Tipo.Sub, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp))
        }
        filas.forEachIndexed { i, fila ->
            if (i > 0) HorizontalDivider(color = Paleta.Borde, thickness = 1.dp)
            fila()
        }
    }
}

@Composable
fun Fila(
    modifier: Modifier = Modifier,
    alTocar: (() -> Unit)? = null,
    contenido: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .let { if (alTocar != null) it.clickable(onClick = alTocar) else it }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = contenido,
    )
}

enum class EstiloBoton { PRINCIPAL, SECUNDARIO, VIOLETA, PELIGRO, BLANCO, OSCURO, BORDE, TEXTO }

@Composable
fun Boton(
    texto: String,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    estilo: EstiloBoton = EstiloBoton.PRINCIPAL,
    chico: Boolean = false,
    habilitado: Boolean = true,
) {
    val (fondo, color, borde) = when (estilo) {
        EstiloBoton.PRINCIPAL -> Triple(Paleta.Azul, Color.White, null)
        EstiloBoton.SECUNDARIO -> Triple(Paleta.Sup2, Paleta.Txt, Paleta.Borde)
        EstiloBoton.VIOLETA -> Triple(Paleta.Violeta, Paleta.Noche, null)
        EstiloBoton.PELIGRO -> Triple(Tono.ROJO.fondo, Paleta.Rojo, Paleta.Rojo.copy(alpha = 0.35f))
        EstiloBoton.BLANCO -> Triple(Color.White, Color(0xFFB42020), null)
        EstiloBoton.OSCURO -> Triple(Color.Black.copy(alpha = 0.24f), Color.White, null)
        EstiloBoton.BORDE -> Triple(Color.Transparent, Color.White, Color.White.copy(alpha = 0.75f))
        EstiloBoton.TEXTO -> Triple(Color.Transparent, Color.White.copy(alpha = 0.85f), null)
    }
    val forma = if (chico) RoundedCornerShape(12.dp) else RadioBoton
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = if (estilo == EstiloBoton.BLANCO) 56.dp else if (chico) 40.dp else 48.dp)
            .clip(forma)
            .background(fondo.copy(alpha = if (habilitado) fondo.alpha else fondo.alpha * 0.45f))
            .let { if (borde != null) it.border(if (estilo == EstiloBoton.BORDE) 1.5.dp else 1.dp, borde, forma) else it }
            .clickable(enabled = habilitado, role = Role.Button, onClick = alTocar)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val c = if (habilitado) color else color.copy(alpha = 0.45f)
        if (icono != null) Icon(icono, null, tint = c, modifier = Modifier.size(18.dp))
        Text(
            texto,
            style = Tipo.Boton.copy(
                fontSize = if (chico) 14.sp else if (estilo == EstiloBoton.BLANCO) 16.sp else 15.sp,
                fontWeight = if (estilo == EstiloBoton.BLANCO) FontWeight.Bold else FontWeight.SemiBold,
            ),
            color = c,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun IconoBoton(icono: ImageVector, descripcion: String, alTocar: () -> Unit, principal: Boolean = false, tam: Dp = 40.dp) {
    Box(
        Modifier
            .size(tam)
            .clip(CircleShape)
            .background(if (principal) Paleta.Azul else Paleta.Sup2)
            .clickable(role = Role.Button, onClick = alTocar)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, null, tint = if (principal) Color.White else Paleta.Txt, modifier = Modifier.size(tam * 0.47f))
    }
}

@Composable
fun Chip(texto: String, tono: Tono, modifier: Modifier = Modifier, icono: ImageVector? = null) {
    Row(
        modifier
            .height(24.dp)
            .clip(CircleShape)
            .background(tono.fondo)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icono != null) Icon(icono, null, tint = tono.color, modifier = Modifier.size(12.dp))
        Text(texto, style = Tipo.Chip, color = tono.color, maxLines = 1)
    }
}

@Composable
fun Punto(color: Color, tam: Dp = 8.dp) {
    Box(Modifier.size(tam).clip(CircleShape).background(color))
}

@Composable
fun IcoCaja(icono: ImageVector, tono: Tono, tam: Dp = 40.dp, tamIcono: Dp = 20.dp, circular: Boolean = false) {
    Box(
        Modifier
            .size(tam)
            .clip(if (circular) CircleShape else RoundedCornerShape(tam * 0.3f))
            .background(tono.fondo),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, null, tint = tono.color, modifier = Modifier.size(tamIcono))
    }
}

@Composable
fun IconoLetra(nombre: String, tono: Tono) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(tono.fondo), contentAlignment = Alignment.Center) {
        Text(
            (nombre.firstOrNull() ?: '?').uppercase(),
            style = TextStyle(fontFamily = Fuentes.Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            color = tono.color,
        )
    }
}

/** Interruptor de 46 × 28 como el del diseño. */
@Composable
fun Interruptor(activo: Boolean, alCambiar: (Boolean) -> Unit, descripcion: String) {
    val x by animateFloatAsState(if (activo) 1f else 0f, tween(150), label = "interruptor")
    Box(
        Modifier
            .size(46.dp, 28.dp)
            .clip(CircleShape)
            .background(if (activo) Paleta.Azul else Paleta.InterruptorApagado)
            .clickable(role = Role.Switch) { alCambiar(!activo) }
            .semantics { contentDescription = descripcion },
    ) {
        Box(
            Modifier
                .offset { IntOffset((3.dp + 18.dp * x).roundToPx(), 3.dp.roundToPx()) }
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

data class Opcion<T>(val valor: T, val texto: String, val icono: ImageVector? = null)

@Composable
fun <T> Segmentado(opciones: List<Opcion<T>>, seleccion: T, alElegir: (T) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RadioBoton)
            .background(Paleta.Sup)
            .border(1.dp, Paleta.Borde, RadioBoton)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        opciones.forEach { o ->
            val activo = o.valor == seleccion
            Row(
                Modifier
                    .weight(1f)
                    .heightIn(min = 38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activo) Paleta.Azul else Color.Transparent)
                    .clickable(role = Role.RadioButton) { alElegir(o.valor) },
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val c = if (activo) Color.White else Paleta.Txt2
                if (o.icono != null) Icon(o.icono, null, tint = c, modifier = Modifier.size(16.dp))
                Text(o.texto, style = Tipo.Boton.copy(fontSize = 14.sp), color = c, maxLines = 1)
            }
        }
    }
}

@Composable
fun Banner(texto: String, icono: ImageVector, tono: Tono, alTocar: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RadioBoton)
            .background(tono.fondo)
            .let { if (alTocar != null) it.clickable(onClick = alTocar) else it }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icono, null, tint = tono.color, modifier = Modifier.size(18.dp))
        Text(texto, style = Tipo.Cuerpo.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = tono.color)
    }
}

/** Anillo de estado con latido suave mientras vigila. */
@Composable
fun Anillo(icono: ImageVector, tono: Tono, tam: Dp, latido: Boolean, tamIcono: Dp = tam * 0.3f) {
    val transicion = rememberInfiniteTransition(label = "latido")
    val fase by transicion.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2_400), RepeatMode.Restart), label = "fase",
    )
    Box(Modifier.size(tam), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(tam)) {
            val r = size.minDimension / 2
            if (latido) {
                val esc = 0.85f + 0.27f * fase
                drawCircle(tono.fondo.copy(alpha = tono.fondo.alpha * 4 * (1 - fase)), r * esc, style = Stroke(2.dp.toPx()))
            } else {
                drawCircle(tono.fondo, r, style = Stroke(2.dp.toPx()))
            }
            drawCircle(tono.fondo, r * 0.38f)
            drawCircle(tono.color, r * 0.68f, style = Stroke(3.dp.toPx()))
        }
        Icon(icono, null, tint = tono.color, modifier = Modifier.size(tamIcono))
    }
}

@Composable
fun IconoBateria(pct: Int?, color: Color = Paleta.Txt) {
    Canvas(Modifier.size(24.dp, 13.dp)) {
        val u = size.width / 24f
        drawRoundRect(color.copy(alpha = 0.7f), Offset(0.75f * u, 0.75f * u), Size(20f * u, 11.5f * u), CornerRadius(3f * u), style = Stroke(1.5f * u))
        val p = (pct ?: 0).coerceIn(0, 100) / 100f
        drawRoundRect(color, Offset(2.5f * u, 2.5f * u), Size(16.5f * u * p, 8f * u), CornerRadius(1.5f * u))
        drawRoundRect(color.copy(alpha = 0.7f), Offset(22f * u, 4.5f * u), Size(1.6f * u, 4f * u), CornerRadius(0.8f * u))
    }
}

@Composable
fun EtiquetaSeccion(texto: String, color: Color = Paleta.Txt2, modifier: Modifier = Modifier) {
    Text(texto.uppercase(), style = Tipo.Etiqueta, color = color, modifier = modifier)
}

@Composable
fun SeccionTitulo(titulo: String, tono: Tono, contador: Int) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        EtiquetaSeccion(titulo, tono.color, Modifier.weight(1f))
        Chip("$contador", tono)
    }
}

@Composable
fun Campo(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    placeholder: String = "",
    codigo: Boolean = false,
    numerico: Boolean = false,
    mono: Boolean = false,
    maximo: Int = 30,
    teclado: KeyboardType? = null,
) {
    var foco by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (etiqueta.isNotEmpty()) Text(etiqueta, style = Tipo.T2.copy(fontWeight = FontWeight.SemiBold))
        val estilo = when {
            codigo -> TextStyle(fontFamily = Fuentes.Mono, fontWeight = FontWeight.Medium, fontSize = 26.sp, letterSpacing = 7.8.sp, textAlign = TextAlign.Center, color = Paleta.Txt)
            mono -> Tipo.Mono.copy(fontSize = 15.sp)
            else -> Tipo.Cuerpo.copy(fontSize = 16.sp)
        }
        BasicTextField(
            value = valor,
            onValueChange = { alCambiar(it.take(maximo)) },
            singleLine = true,
            textStyle = estilo,
            cursorBrush = SolidColor(Paleta.AzulC),
            keyboardOptions = KeyboardOptions(keyboardType = teclado ?: if (numerico || codigo) KeyboardType.NumberPassword else KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { foco = it.isFocused },
            decorationBox = { campo ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Paleta.Sup2)
                        .border(1.dp, if (foco) Paleta.AzulC else Paleta.Borde, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = if (codigo) Alignment.Center else Alignment.CenterStart,
                ) {
                    if (valor.isEmpty()) Text(placeholder, style = estilo.copy(color = Paleta.Txt2.copy(alpha = 0.6f)))
                    campo()
                }
            },
        )
    }
}

@Composable
fun Cabecera(
    titulo: String,
    pre: String? = null,
    alVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = if (alVolver != null) 10.dp else 18.dp, end = 18.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (alVolver != null) IconoBoton(Iconos.Atras, "Volver", alVolver)
        Column(Modifier.weight(1f)) {
            if (pre != null) Text(pre, style = Tipo.T2)
            Text(titulo, style = Tipo.Titulo, maxLines = 1)
        }
        acciones()
    }
}

/** Dato pequeño (batería, temperatura, señal). */
@Composable
fun Dato(valor: String, etiqueta: String, modifier: Modifier = Modifier, icono: @Composable () -> Unit) {
    Column(
        modifier.padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        icono()
        Text(valor, style = Tipo.T1)
        Text(etiqueta, style = Tipo.T2.copy(fontSize = 11.5.sp))
    }
}

@Composable
fun Confirmacion(pregunta: String, textoSi: String, alSi: () -> Unit, alNo: () -> Unit, detalle: String? = null) {
    Tarjeta(borde = Paleta.Rojo.copy(alpha = 0.45f)) {
        Text(pregunta, style = Tipo.T1)
        if (detalle != null) Text(detalle, style = Tipo.T2)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Boton(textoSi, alSi, Modifier.weight(1f), estilo = EstiloBoton.PELIGRO, chico = true)
            Boton("Cancelar", alNo, Modifier.weight(1f), estilo = EstiloBoton.SECUNDARIO, chico = true)
        }
    }
}

@Composable
fun Pulso(contenido: @Composable () -> Unit) {
    val t = rememberInfiniteTransition(label = "pulso")
    val s by t.animateFloat(1f, 1.07f, infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "s")
    Box(Modifier.scale(s)) { contenido() }
}

@Composable
fun Espacio(alto: Dp) = Spacer(Modifier.height(alto))

@Composable
fun AnchoEspacio(ancho: Dp) = Spacer(Modifier.width(ancho))
