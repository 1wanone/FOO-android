package playfoo.com.ui.game

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import playfoo.com.R
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.game.components.AvatarHandOnly
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
            // [0] Fundo
            Image(
                painter = painterResource(R.drawable.fundo_jogo),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            // [1] PlayerDesk esquerdo (cadeira + avatar local)
            PlayerDesk(avatarConfig = avatarConfigLocal, estado = estadoLocal)
            // [2] PlayerDesk direito (cadeira + avatar remoto)
            PlayerDesk(avatarConfig = avatarConfigRemoto, estado = estadoRemoto)
            // [3] Mesa dupla full-width — na frente dos avatares
            Image(
                painter = painterResource(R.drawable.mesa_2pc),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
            // [4] Mão local — na frente da mesa
            AvatarHandOnly(config = avatarConfigLocal)
            // [5] Mão remota — na frente da mesa
            AvatarHandOnly(config = avatarConfigRemoto)
        }
    ) { measurables, constraints ->

        val W    = constraints.maxWidth
        val H    = constraints.maxHeight
        val half = W / 2

        // Mesa: ratio exato da imagem 633×236
        val mesaW = W
        val mesaH = (W * 236f / 633f).toInt()
        val mesaY = H - mesaH

        // Tamanho da mão = mesmo avW/avH usado dentro do PlayerDesk
        val avW = (half * 0.65f).toInt()
        val avH = (avW * 2400f / 2500f).toInt()
        val avX = 0

        val fundo      = measurables[0].measure(Constraints.fixed(W, H))
        val deskLocal  = measurables[1].measure(Constraints.fixed(half, mesaY))
        val deskRemoto = measurables[2].measure(Constraints.fixed(half, mesaY))
        val mesa       = measurables[3].measure(Constraints.fixed(mesaW, mesaH))
        val maoLocal   = measurables[4].measure(Constraints.fixed(avW, avH))
        val maoRemota  = measurables[5].measure(Constraints.fixed(avW, avH))

        val avY = mesaY - (avH * 0.37f).toInt()

        layout(W, H) {
            fundo.place(0, 0)
            deskLocal.place(0, 0)
            deskRemoto.place(half, 0)
            mesa.place(0, mesaY)
            maoLocal.place(avX, avY)
            maoRemota.place(half + avX, avY)
        }
    }
}
