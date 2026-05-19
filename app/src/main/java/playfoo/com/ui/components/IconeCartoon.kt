package playfoo.com.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconeCartoon(
    fallbackEmoji: String? = null,
    fallbackIcone: ImageVector? = null,
    descricao: String = "",
    tamanho: Dp = 32.dp,
    emojiFontSize: TextUnit = 24.sp,
    corIcone: Color = Color.White,
    // SLOT DE ASSET — passar painterResource(R.drawable.icone_x) quando pronto
    assetPainter: Painter? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(tamanho),
        contentAlignment = Alignment.Center
    ) {
        when {
            assetPainter != null -> {
                Image(
                    painter = assetPainter,
                    contentDescription = descricao,
                    modifier = Modifier.fillMaxSize()
                )
            }
            fallbackEmoji != null -> {
                Text(text = fallbackEmoji, fontSize = emojiFontSize)
            }
            fallbackIcone != null -> {
                Icon(
                    imageVector = fallbackIcone,
                    contentDescription = descricao,
                    tint = corIcone,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
