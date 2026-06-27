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
            // [0] Fundo
            Image(
                painter = painterResource(R.drawable.fundo_jogo),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            // [1] Cadeira Local
            Image(
                painter = painterResource(R.drawable.cadeira),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // [2] Avatar Local
            AvatarView(config = avatarConfigLocal, estado = estadoLocal, mostrarMao = false)

            // [3] Cadeira Remota
            Image(
                painter = painterResource(R.drawable.cadeira),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // [4] Avatar Remoto
            AvatarView(config = avatarConfigRemoto, estado = estadoRemoto, mostrarMao = false)

            // [5] Mesa Local — FillBounds sem distorção pois mesaH/mesaW = 1240/2500
            Image(
                painter = painterResource(R.drawable.mesa_pc_crop),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // [6] Mesa Remota
            Image(
                painter = painterResource(R.drawable.mesa_pc_crop),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // [7] Mão Local
            AvatarHandOnly(config = avatarConfigLocal)

            // [8] Mão Remota
            AvatarHandOnly(config = avatarConfigRemoto)
        }
    ) { measurables, constraints ->

        val W = constraints.maxWidth
        val H = constraints.maxHeight
        val half = W / 2

        // Fórmulas idênticas ao GameBackground (single player),
        // substituindo W por half em cada lado.
        val mesaW = half
        val mesaH = (half * 1240f / 2500f).toInt() // ratio exato da imagem → sem distorção
        val mesaY = H - mesaH

        val avW      = (half * 0.85f).toInt()
        val avH      = (avW  * 2400f / 2500f).toInt()
        val avX      = (half * 0.03f).toInt()
        val cadeiraX = avX + (half * 0.03f).toInt()
        val avY      = mesaY - (avH * 0.45f).toInt()

        val fundo         = measurables[0].measure(Constraints.fixed(W, H))
        val cadeiraLocal  = measurables[1].measure(Constraints.fixed(avW, avH))
        val avatarLocal   = measurables[2].measure(Constraints.fixed(avW, avH))
        val cadeiraRemota = measurables[3].measure(Constraints.fixed(avW, avH))
        val avatarRemoto  = measurables[4].measure(Constraints.fixed(avW, avH))
        val mesaLocal     = measurables[5].measure(Constraints.fixed(mesaW, mesaH))
        val mesaRemota    = measurables[6].measure(Constraints.fixed(mesaW, mesaH))
        val maoLocal      = measurables[7].measure(Constraints.fixed(avW, avH))
        val maoRemota     = measurables[8].measure(Constraints.fixed(avW, avH))

        layout(W, H) {
            // Fundo ocupa toda a tela
            fundo.place(0, 0)

            // Lado esquerdo - Jogador Local
            cadeiraLocal.place(cadeiraX, avY)
            avatarLocal.place(avX, avY)
            mesaLocal.place(0, mesaY)
            maoLocal.place(avX, avY)

            // Lado direito - Jogador Remoto
            cadeiraRemota.place(half + cadeiraX, avY)
            avatarRemoto.place(half + avX, avY)
            mesaRemota.place(half, mesaY)
            maoRemota.place(half + avX, avY)
        }
    }
}