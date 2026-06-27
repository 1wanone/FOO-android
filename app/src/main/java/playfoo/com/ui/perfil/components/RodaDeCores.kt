package playfoo.com.ui.perfil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun RodaDeCores(
    corSelecionada: Color,
    onCorSelecionada: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue        by remember { mutableFloatStateOf(0f) }
    var saturacao  by remember { mutableFloatStateOf(1f) }
    var brilho     by remember { mutableFloatStateOf(0.8f) }
    var tamanho    by remember { mutableStateOf(IntSize.Zero) }

    // Inicializa a partir da cor passada
    LaunchedEffect(corSelecionada) {
        val hsv = floatArrayOf(0f, 0f, 0f)
        android.graphics.Color.RGBToHSV(
            (corSelecionada.red   * 255).toInt(),
            (corSelecionada.green * 255).toInt(),
            (corSelecionada.blue  * 255).toInt(),
            hsv
        )
        hue       = hsv[0]
        saturacao = hsv[1]
        brilho    = hsv[2]
    }

    fun handleOffset(offset: Offset) {
        val cx = tamanho.width / 2f
        val cy = tamanho.height / 2f
        val r  = minOf(cx, cy)
        val dx = offset.x - cx
        val dy = offset.y - cy
        val dist = sqrt(dx * dx + dy * dy)
        if (dist <= r) {
            hue       = ((atan2(dy, dx) * 180.0 / PI + 360.0) % 360.0).toFloat()
            saturacao = (dist / r).coerceIn(0f, 1f)
            onCorSelecionada(Color.hsv(hue, saturacao, brilho))
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .onSizeChanged { tamanho = it }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press ||
                                event.type == PointerEventType.Move
                            ) {
                                event.changes.firstOrNull()?.let { change ->
                                    handleOffset(change.position)
                                    change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            val cx = size.width  / 2f
            val cy = size.height / 2f
            val r  = minOf(cx, cy)

            // Roda de matiz (hue)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Red,
                        Color(0xFFFF8000),
                        Color.Yellow,
                        Color(0xFF80FF00),
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color(0xFF8000FF),
                        Color.Magenta,
                        Color.Red
                    ),
                    center = Offset(cx, cy)
                ),
                radius = r,
                center = Offset(cx, cy)
            )

            // Gradiente radial: branco no centro (baixa saturação)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r
                ),
                radius = r,
                center = Offset(cx, cy)
            )

            // Escurecimento pelo brilho
            if (brilho < 1f) {
                drawCircle(
                    color  = Color.Black.copy(alpha = 1f - brilho),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }

            // Indicador de posição selecionada
            val angRad    = hue * PI.toFloat() / 180f
            val indX      = cx + r * saturacao * cos(angRad)
            val indY      = cy + r * saturacao * sin(angRad)
            val indOffset = Offset(indX, indY)

            drawCircle(
                color  = Color.White,
                radius = 14.dp.toPx(),
                center = indOffset,
                style  = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color  = Color.hsv(hue, saturacao, brilho),
                radius = 10.dp.toPx(),
                center = indOffset
            )
        }

        // Slider de brilho
        Text("Brilho", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Slider(
            value         = brilho,
            onValueChange = {
                brilho = it
                onCorSelecionada(Color.hsv(hue, saturacao, brilho))
            },
            modifier = Modifier.fillMaxWidth(),
            colors   = SliderDefaults.colors(
                thumbColor          = Color.White,
                activeTrackColor    = Color.White,
                inactiveTrackColor  = Color.White.copy(alpha = 0.25f)
            )
        )

        // Preview da cor selecionada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(Color.hsv(hue, saturacao, brilho), RoundedCornerShape(8.dp))
        )
    }
}
