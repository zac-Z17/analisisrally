package ni.uni.nanobots.sentinel.ui.comp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Código QR dibujado módulo por módulo (nítido en cualquier pantalla). */
@Composable
fun CodigoQr(texto: String) {
    val matriz: BitMatrix? = remember(texto) {
        runCatching {
            QRCodeWriter().encode(
                texto, BarcodeFormat.QR_CODE, 0, 0,
                mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 1),
            )
        }.getOrNull()
    }
    if (matriz == null) return
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp)
            .semantics { contentDescription = "Código QR para vincular" },
    ) {
        Canvas(Modifier.size(210.dp)) {
            val n = matriz.width
            val celda = size.minDimension / n
            for (y in 0 until n) for (x in 0 until n) {
                if (matriz[x, y]) drawRect(Color.Black, Offset(x * celda, y * celda), Size(celda + 0.5f, celda + 0.5f))
            }
        }
    }
}
