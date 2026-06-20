package playfoo.com.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import playfoo.com.ui.theme.*

enum class BotaoCartoonTipo { PRIMARIO, SECUNDARIO, PERIGO, NEUTRO, SUCESSO, GOOGLE }

@Composable
fun BotaoCartoon(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tipo: BotaoCartoonTipo = BotaoCartoonTipo.PRIMARIO,
    habilitado: Boolean = true,
    altura: Dp = 56.dp,
    fontSize: TextUnit = 15.sp,
    icone: ImageVector? = null,
    assetPainter: Painter? = null
) {
    val corFundo = when (tipo) {
        BotaoCartoonTipo.PRIMARIO   -> Rosa
        BotaoCartoonTipo.SECUNDARIO -> Pink
        BotaoCartoonTipo.PERIGO     -> ErroVermelho
        BotaoCartoonTipo.NEUTRO     -> AzulCinza
        BotaoCartoonTipo.SUCESSO    -> Ciano
        BotaoCartoonTipo.GOOGLE     -> AzulCinza
    }
    val corSombra = when (tipo) {
        BotaoCartoonTipo.PRIMARIO   -> Color(0xFFB05060)
        BotaoCartoonTipo.SECUNDARIO -> Color(0xFF9A1050)
        BotaoCartoonTipo.PERIGO     -> Color(0xFF8B0000)
        BotaoCartoonTipo.NEUTRO     -> Color(0xFF2D5060)
        BotaoCartoonTipo.SUCESSO    -> Color(0xFF1A7070)
        BotaoCartoonTipo.GOOGLE     -> Color(0xFF2D5060)
    }
    val alpha = if (habilitado) 1f else 0.5f

    Box(
        modifier = modifier
            .height(altura)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(corSombra.copy(alpha = alpha))
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (assetPainter != null) {
            Image(
                painter = assetPainter,
                contentDescription = texto,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(corFundo.copy(alpha = alpha)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icone?.let {
                        FooIcone(icone = it, cor = Color.White, tamanho = 20.dp)
                    }
                    Text(
                        text = texto.uppercase(),
                        color = Color.White,
                        fontSize = fontSize,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}