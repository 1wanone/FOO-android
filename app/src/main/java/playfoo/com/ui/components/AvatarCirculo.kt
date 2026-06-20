package playfoo.com.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.theme.Rosa

@Composable
fun AvatarCirculo(
    config: AvatarConfig,
    tamanho: Dp = 90.dp,
    bordaCor: Color = Rosa,
    bordaEspessura: Dp = 3.dp
) {
    // Frame 2.2× o diâmetro: só o busto fica visível no crop circular
    val frameSize = tamanho * 2.2f

    Box(
        modifier = Modifier
            .size(tamanho)
            .border(bordaEspessura, bordaCor, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .size(frameSize)
                .offset(y = -(tamanho * 0.05f))
        ) {
            AvatarView(
                config   = config,
                estado   = EstadoAvatar.NEUTRO,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}