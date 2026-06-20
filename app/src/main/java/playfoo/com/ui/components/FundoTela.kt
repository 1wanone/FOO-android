package playfoo.com.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import playfoo.com.ui.theme.*

enum class TipoFundo { MENU, JOGO, MULTIPLAYER, PERFIL, DASHBOARD, TURMA }

@Composable
fun FundoTela(
    modifier: Modifier = Modifier,
    tipo: TipoFundo = TipoFundo.MENU,
    assetPainter: Painter? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val gradienteFallback = when (tipo) {
        TipoFundo.MENU        -> Brush.verticalGradient(listOf(RoxoEscuro, RoxoMedio))
        TipoFundo.JOGO        -> Brush.verticalGradient(listOf(RoxoEscuro, RoxoMedio))
        TipoFundo.MULTIPLAYER -> Brush.verticalGradient(listOf(Color(0xFF2A0840), RoxoEscuro))
        TipoFundo.PERFIL      -> Brush.verticalGradient(listOf(RoxoEscuro, Color(0xFF200540)))
        TipoFundo.DASHBOARD   -> Brush.verticalGradient(listOf(RoxoMedio, RoxoEscuro))
        TipoFundo.TURMA       -> Brush.verticalGradient(listOf(Color(0xFF200540), RoxoMedio))
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (assetPainter != null) {
            Image(
                painter = assetPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(gradienteFallback))
        }
        content()
    }
}