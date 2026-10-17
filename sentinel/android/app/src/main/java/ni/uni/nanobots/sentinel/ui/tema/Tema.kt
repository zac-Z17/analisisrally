package ni.uni.nanobots.sentinel.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ni.uni.nanobots.sentinel.R

/** Paleta de diseno/diseno_app_sentinel.md (modo oscuro). */
object Paleta {
    val Noche = Color(0xFF0B1220)
    val Sup = Color(0xFF131C2E)
    val Sup2 = Color(0xFF1C2740)
    val Borde = Color(0xFF2A3654)
    val Negro = Color(0xFF04070D)
    val Nav = Color(0xFF0E1628)
    val AvisoFondo = Color(0xFF24304D)
    val InterruptorApagado = Color(0xFF3A4566)
    val Txt = Color(0xFFEAF0FB)
    val Txt2 = Color(0xFF94A3BF)
    val Azul = Color(0xFF2F6FEB)
    val AzulC = Color(0xFF4C8DFF)
    val Cian = Color(0xFF22D3EE)
    val Violeta = Color(0xFFA78BFA)
    val Verde = Color(0xFF22C55E)
    val Ambar = Color(0xFFF5A524)

    /** Solo para intentos de entrada. */
    val Rojo = Color(0xFFFF5A5F)
    val RojoF = Color(0xFFD92D2D)
    val Gris = Color(0xFF7C89A6)
}

/** Color de cada rol o estado, con su fondo suave. */
enum class Tono(val color: Color, val fondo: Color) {
    AZUL(Paleta.AzulC, Paleta.AzulC.copy(alpha = 0.15f)),
    CIAN(Paleta.Cian, Paleta.Cian.copy(alpha = 0.15f)),
    VIOLETA(Paleta.Violeta, Paleta.Violeta.copy(alpha = 0.16f)),
    VERDE(Paleta.Verde, Paleta.Verde.copy(alpha = 0.15f)),
    AMBAR(Paleta.Ambar, Paleta.Ambar.copy(alpha = 0.16f)),
    ROJO(Paleta.Rojo, Paleta.Rojo.copy(alpha = 0.14f)),
    GRIS(Paleta.Gris, Paleta.Gris.copy(alpha = 0.16f)),
}

object Fuentes {
    val Sora = FontFamily(
        Font(R.font.sora_semibold, FontWeight.SemiBold),
        Font(R.font.sora_bold, FontWeight.Bold),
        Font(R.font.sora_extrabold, FontWeight.ExtraBold),
    )
    val Inter = FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold),
    )
    val Mono = FontFamily(
        Font(R.font.jetbrainsmono_medium, FontWeight.Medium),
        Font(R.font.jetbrainsmono_semibold, FontWeight.SemiBold),
    )
}

/** Estilos de texto usados en toda la app. */
object Tipo {
    val Titulo = TextStyle(fontFamily = Fuentes.Sora, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp, color = Paleta.Txt)
    val Cuerpo = TextStyle(fontFamily = Fuentes.Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp, color = Paleta.Txt)
    val T1 = Cuerpo.copy(fontWeight = FontWeight.SemiBold)
    val T2 = Cuerpo.copy(fontSize = 12.5.sp, lineHeight = 17.sp, color = Paleta.Txt2)
    val Sub = Cuerpo.copy(fontSize = 13.5.sp, lineHeight = 19.sp, color = Paleta.Txt2)
    val Etiqueta = TextStyle(fontFamily = Fuentes.Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.99.sp, color = Paleta.Txt2)
    val Chip = TextStyle(fontFamily = Fuentes.Inter, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, letterSpacing = 0.63.sp)
    val Boton = TextStyle(fontFamily = Fuentes.Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    val Mono = TextStyle(fontFamily = Fuentes.Mono, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Paleta.Txt)
}

@Composable
fun SentinelTema(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Paleta.Azul,
            onPrimary = Color.White,
            background = Paleta.Noche,
            onBackground = Paleta.Txt,
            surface = Paleta.Sup,
            onSurface = Paleta.Txt,
            surfaceVariant = Paleta.Sup2,
            outline = Paleta.Borde,
            error = Paleta.Rojo,
        ),
        content = contenido,
    )
}
