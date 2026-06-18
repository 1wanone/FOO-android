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

enum class TipoFundo { MENU, JOGO, MULTIPLAYER, PERFIL, DASHBOARD, TURMA }

@Composable
fun FundoTela(
    modifier: Modifier = Modifier,
    tipo: TipoFundo = TipoFundo.MENU,
    // SLOT DE ASSET — passar painterResource(R.drawable.fundo_menu)
    assetPainter: Painter? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val gradienteFallback = when (tipo) {
        TipoFundo.MENU        -> Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)))
        TipoFundo.JOGO        -> Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF2D3A4A)))
        TipoFundo.MULTIPLAYER -> Brush.verticalGradient(listOf(Color(0xFF1A0A2E), Color(0xFF2D1B4E), Color(0xFF3D2B5E)))
        TipoFundo.PERFIL      -> Brush.verticalGradient(listOf(Color(0xFF0A2E1A), Color(0xFF1B4E2D), Color(0xFF2B5E3D)))
        TipoFundo.DASHBOARD   -> Brush.verticalGradient(listOf(Color(0xFF2E1A0A), Color(0xFF4E2D1B), Color(0xFF5E3D2B)))
        TipoFundo.TURMA       -> Brush.verticalGradient(listOf(Color(0xFF0A1A2E), Color(0xFF1B2D4E), Color(0xFF2B3D5E)))
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
