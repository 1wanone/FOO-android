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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BotaoCartoonTipo { PRIMARIO, SECUNDARIO, PERIGO, SUCESSO, GOOGLE }

@Composable
fun BotaoCartoon(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tipo: BotaoCartoonTipo = BotaoCartoonTipo.PRIMARIO,
    habilitado: Boolean = true,
    altura: Dp = 56.dp,
    fontSize: TextUnit = 16.sp,
    // SLOT DE ASSET — quando o desenhista entregar, passar o painter aqui
    // Exemplo: assetPainter = painterResource(R.drawable.btn_primario)
    assetPainter: Painter? = null
) {
    val corFundo = when (tipo) {
        BotaoCartoonTipo.PRIMARIO   -> Color(0xFF6C63FF)
        BotaoCartoonTipo.SECUNDARIO -> Color(0xFF4CAF50)
        BotaoCartoonTipo.PERIGO     -> Color(0xFFE53935)
        BotaoCartoonTipo.SUCESSO    -> Color(0xFF00BCD4)
        BotaoCartoonTipo.GOOGLE     -> Color(0xFF4285F4)
    }
    val corSombra = when (tipo) {
        BotaoCartoonTipo.PRIMARIO   -> Color(0xFF3D35CC)
        BotaoCartoonTipo.SECUNDARIO -> Color(0xFF2E7D32)
        BotaoCartoonTipo.PERIGO     -> Color(0xFFB71C1C)
        BotaoCartoonTipo.SUCESSO    -> Color(0xFF00838F)
        BotaoCartoonTipo.GOOGLE     -> Color(0xFF1A73E8)
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
                Text(
                    text = texto,
                    color = Color.White,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
