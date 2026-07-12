package com.gamescounter.truco.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gamescounter.truco.ui.theme.ClayHighlight
import com.gamescounter.truco.ui.theme.ClayShadowDark

val KountaShape = RoundedCornerShape(24.dp)
val KountaShapeSmall = RoundedCornerShape(18.dp)

/**
 * Superficie "inflada" (claymorfismo): sombra oscura abajo-derecha + luz sutil
 * arriba-izquierda, dando volumen sin usar bordes duros. BlurMaskFilter sólo
 * funciona en canvas por software (API 28+); en 26-27 cae a [Modifier.shadow].
 */
fun Modifier.claymorphic(
    shape: RoundedCornerShape = KountaShape,
    elevation: Dp = 8.dp,
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        return this.shadow(elevation, shape, ambientColor = ClayShadowDark, spotColor = ClayShadowDark)
    }
    return this.drawBehind {
        val radius = shape.topStart.toPx(Size(size.width, size.height), this)
        val blurPx = elevation.toPx() * 1.6f
        val offsetPx = elevation.toPx() * 0.5f

        drawIntoCanvas { canvas ->
            val darkPaint = Paint().apply {
                isAntiAlias = true
                color = ClayShadowDark.copy(alpha = 0.45f).toArgb()
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawRoundRect(
                offsetPx,
                offsetPx,
                size.width + offsetPx,
                size.height + offsetPx,
                radius,
                radius,
                darkPaint,
            )

            val lightPaint = Paint().apply {
                isAntiAlias = true
                color = ClayHighlight.copy(alpha = 0.05f).toArgb()
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawRoundRect(
                -offsetPx,
                -offsetPx,
                size.width - offsetPx,
                size.height - offsetPx,
                radius,
                radius,
                lightPaint,
            )
        }
    }
}
