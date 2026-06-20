package playfoo.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import playfoo.com.ui.theme.*

@Composable
fun CardCartoon(
    modifier: Modifier = Modifier,
    corFundo: Color = FundoCard,
    corBorda: Color = RoxoMedio,
    espessuraBorda: Dp = 2.dp,
    raio: Dp = 16.dp,
    padding: Dp = 16.dp,
    elevacao: Dp = 8.dp,
    @Suppress("UNUSED_PARAMETER") assetPainter: Painter? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevacao, RoundedCornerShape(raio))
            .clip(RoundedCornerShape(raio))
            .border(espessuraBorda, corBorda, RoundedCornerShape(raio))
            .background(corFundo)
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}