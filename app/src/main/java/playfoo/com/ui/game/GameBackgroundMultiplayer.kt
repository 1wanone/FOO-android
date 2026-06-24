package playfoo.com.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import playfoo.com.R
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar

@Composable
fun GameBackgroundMultiplayer(
    avatarConfigLocal: AvatarConfig = AvatarConfig(),
    estadoLocal: EstadoAvatar = EstadoAvatar.NEUTRO,
    avatarConfigRemoto: AvatarConfig = AvatarConfig(),
    estadoRemoto: EstadoAvatar = EstadoAvatar.NEUTRO,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            // 0: fundo
            Image(
                painter = painterResource(R.drawable.fundo_jogo),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            // 1: avatar local (esquerda)
            AvatarView(config = avatarConfigLocal, estado = estadoLocal, mostrarMao = false)
            // 2: avatar remoto (direita, espelhado para ficar de frente ao local)
            Box(modifier = Modifier.graphicsLayer(scaleX = -1f)) {
                AvatarView(config = avatarConfigRemoto, estado = estadoRemoto, mostrarMao = false)
            }
            // 3: mesa esquerda (normal)
            Image(
                painter = painterResource(R.drawable.mesa_pc_crop),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
            // 4: mesa direita (espelhada horizontalmente)
            Image(
                painter = painterResource(R.drawable.mesa_pc_crop),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.graphicsLayer(scaleX = -1f)
            )
        }
    ) { measurables, constraints ->
        val W = constraints.maxWidth
        val H = constraints.maxHeight

        // Mesa: cada metade = 55% da largura, proporção 2500x1240
        val mesaMetadeW = (W * 0.55f).toInt()
        val mesaH       = (mesaMetadeW * 1240f / 2500f).toInt()
        val mesaY       = H - mesaH

        // Avatar: 42% da largura, proporção 2500x2400
        val avW = (W * 0.42f).toInt()
        val avH = (avW * 2400f / 2500f).toInt()

        val avLocalX  = (W * 0.02f).toInt()
        val avLocalY  = mesaY - (avH * 0.43f).toInt()
        val avRemotoX = W - avW - (W * 0.02f).toInt()
        val avRemotoY = avLocalY

        val fundo    = measurables[0].measure(Constraints.fixed(W, H))
        val avLocal  = measurables[1].measure(Constraints.fixed(avW, avH))
        val avRemoto = measurables[2].measure(Constraints.fixed(avW, avH))
        val mesaEsq  = measurables[3].measure(Constraints.fixed(mesaMetadeW, mesaH))
        val mesaDir  = measurables[4].measure(Constraints.fixed(mesaMetadeW, mesaH))

        layout(W, H) {
            fundo.place(0, 0)
            avLocal.place(avLocalX, avLocalY)
            avRemoto.place(avRemotoX, avRemotoY)
            mesaEsq.place(0, mesaY)
            mesaDir.place(W - mesaMetadeW, mesaY)
        }
    }
}