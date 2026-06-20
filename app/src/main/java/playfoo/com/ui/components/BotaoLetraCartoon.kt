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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import playfoo.com.ui.game.components.EstadoLetra
import playfoo.com.ui.theme.*

@Composable
fun BotaoLetraCartoon(
    letra: Char,
    estado: EstadoLetra,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    assetDisponivel: Painter? = null,
    assetCorreta: Painter? = null,
    assetErrada: Painter? = null
) {
    val corFundo = when (estado) {
        EstadoLetra.DISPONIVEL -> AzulCinza.copy(alpha = 0.5f)
        EstadoLetra.CORRETA    -> Ciano.copy(alpha = 0.8f)
        EstadoLetra.ERRADA     -> ErroVermelho.copy(alpha = 0.8f)
    }
    val corSombra = when (estado) {
        EstadoLetra.DISPONIVEL -> Color(0xFF1A3040)
        EstadoLetra.CORRETA    -> Color(0xFF158080)
        EstadoLetra.ERRADA     -> Color(0xFF880000)
    }
    val assetAtual = when (estado) {
        EstadoLetra.DISPONIVEL -> assetDisponivel
        EstadoLetra.CORRETA    -> assetCorreta
        EstadoLetra.ERRADA     -> assetErrada
    }
    val podeClicar = habilitado && estado == EstadoLetra.DISPONIVEL
    // Apenas letras ainda disponíveis ficam semi-transparentes quando o teclado está desativado
    val alpha = if (!habilitado && estado == EstadoLetra.DISPONIVEL) 0.5f else 1f

    Box(
        modifier = modifier
            .size(width = 32.dp, height = 42.dp)
            .alpha(alpha)
            .shadow(3.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(corSombra)
            .clickable(enabled = podeClicar, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (assetAtual != null) {
            Image(
                painter = assetAtual,
                contentDescription = letra.toString(),
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = letra.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(corFundo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
