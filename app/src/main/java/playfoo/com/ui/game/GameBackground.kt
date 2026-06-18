package playfoo.com.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import playfoo.com.R
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar

/**
 * Análise pixel a pixel (2500px de largura, escalonado por fillMaxWidth):
 *
 * mesa_pc_crop.png (2500×1240):
 *   - versão cropada da mesa_pc.png, sem os 61.3% de transparência no topo
 *   - conteúdo real começa na linha 0 → frame inteiro é conteúdo visível
 *   - com fillMaxWidth(0.80f) na tela de 360px: 288×142px
 *   - topo da mesa fica a 142px do fundo da área de cenário ✓
 *
 * avatar layers (2500×3000):
 *   - com fillMaxWidth(0.80f): 288×345px
 *   - cabeça em 35.7% → y=123px do topo do frame
 *   - cintura em 66.3% → y=229px do topo do frame = 116px do fundo
 *   - a mesa (topo em 142px do fundo) cobre da cintura para baixo ✓
 *
 * cadeira.png NÃO existe no repositório.
 */
@Composable
fun GameBackground(
    avatarConfig: AvatarConfig = AvatarConfig(),
    estado: EstadoAvatar = EstadoAvatar.NEUTRO,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // 1. Fundo — Crop garante que cobre a tela toda sem distorção
        Image(
            painter = painterResource(R.drawable.fundo_jogo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Avatar — layers compostas pela AvatarView
        //    fillMaxWidth(0.80f) + FillWidth + BottomCenter
        //    Frame: 288×345px, cintura em 116px do fundo
        AvatarView(
            config = avatarConfig,
            estado = estado,
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .align(Alignment.BottomCenter)
        )

        // 3. Mesa (versão cropada — sem transparência extra no topo)
        //    Mesma largura e alinhamento do avatar
        //    Frame: 288×142px, topo em 142px do fundo → cobre cintura ✓
        Image(
            painter = painterResource(R.drawable.mesa_pc_crop),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )
    }
}