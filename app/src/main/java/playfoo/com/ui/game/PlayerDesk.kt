package playfoo.com.ui.game

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import playfoo.com.R
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar

/**
 * Cadeira + avatar (sem mão) para um jogador.
 * Recebe W=half e H=mesaY do pai.
 * A mão é posicionada pelo pai (GameBackgroundMultiplayer) na frente da mesa.
 */
@Composable
fun PlayerDesk(
    avatarConfig: AvatarConfig,
    estado: EstadoAvatar,
) {
    Layout(
        content = {
            // [0] Cadeira
            Image(
                painter = painterResource(R.drawable.cadeira),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
            // [1] Avatar (corpo sem mão)
            AvatarView(config = avatarConfig, estado = estado, mostrarMao = false)
        }
    ) { measurables, constraints ->

        val W = constraints.maxWidth   // half da tela
        val H = constraints.maxHeight  // mesaY

        val avW      = (W * 0.80f).toInt()
        val avH      = (avW * 2400f / 2500f).toInt()
        val avX      = (W * 0.02f).toInt()           // avX mínimo → mão o mais à esquerda possível
        val cadeiraX = avX + (W * 0.03f).toInt()
        val avY      = H - (avH * 0.50f).toInt()

        val cadeira = measurables[0].measure(Constraints.fixed(avW, avH))
        val avatar  = measurables[1].measure(Constraints.fixed(avW, avH))

        layout(W, H) {
            cadeira.place(cadeiraX, avY)
            avatar.place(avX, avY)
        }
    }
}
