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
            // 0: fundo
            Image(painterResource(R.drawable.fundo_jogo), null,
                contentScale = ContentScale.Crop)
            // 1: cadeira local (esquerda)
            Image(painterResource(R.drawable.cadeira), null,
                contentScale = ContentScale.FillBounds)
            // 2: avatar local sem mão
            AvatarView(config = avatarConfigLocal, estado = estadoLocal,
                mostrarMao = false)
            // 3: cadeira remota (direita)
            Image(painterResource(R.drawable.cadeira), null,
                contentScale = ContentScale.FillBounds)
            // 4: avatar remoto sem mão — SEM espelhar, posição direita apenas
            AvatarView(config = avatarConfigRemoto, estado = estadoRemoto,
                mostrarMao = false)
            // 5: mesa esquerda
            Image(painterResource(R.drawable.mesa_pc_crop), null,
                contentScale = ContentScale.FillBounds)
            // 6: mesa direita
            Image(painterResource(R.drawable.mesa_pc_crop), null,
                contentScale = ContentScale.FillBounds)
            // 7: mão local (na frente da mesa esquerda)
            AvatarHandOnly(config = avatarConfigLocal)
            // 8: mão remota (na frente da mesa direita)
            AvatarHandOnly(config = avatarConfigRemoto)
        }
    ) { measurables, constraints ->
        val W = constraints.maxWidth
        val H = constraints.maxHeight

        // Mesa: 50% da largura total cada, proporção 2500x1240
        val mesaW = (W * 0.50f).toInt()
        val mesaH = (mesaW * 1240f / 2500f).toInt()
        val mesaY = H - mesaH

        // Avatar: 40% da largura total, proporção 2500x2400
        val avW = (W * 0.40f).toInt()
        val avH = (avW * 2400f / 2500f).toInt()

        // Y do avatar igual ao single player: mão encosta no mouse
        val avLocalY  = mesaY - (avH * 0.43f).toInt()
        val avRemotoY = avLocalY

        // X: local na esquerda, remoto na direita (posição simétrica)
        val avLocalX  = (W * 0.075f).toInt()
        val avRemotoX = W - avW - (W * 0.075f).toInt()

        // Cadeira: deslocada levemente para a direita do avatar (igual ao single player)
        val cadX_local  = avLocalX  + (W * 0.03f).toInt()
        val cadX_remoto = avRemotoX + (W * 0.03f).toInt()

        val mesaEsqX = 0
        val mesaDirX = W / 2

        val fundo     = measurables[0].measure(Constraints.fixed(W, H))
        val cadLocal  = measurables[1].measure(Constraints.fixed(avW, avH))
        val avLocal   = measurables[2].measure(Constraints.fixed(avW, avH))
        val cadRemoto = measurables[3].measure(Constraints.fixed(avW, avH))
        val avRemoto  = measurables[4].measure(Constraints.fixed(avW, avH))
        val mesaEsq   = measurables[5].measure(Constraints.fixed(mesaW, mesaH))
        val mesaDir   = measurables[6].measure(Constraints.fixed(mesaW, mesaH))
        val maoLocal  = measurables[7].measure(Constraints.fixed(avW, avH))
        val maoRemoto = measurables[8].measure(Constraints.fixed(avW, avH))

        layout(W, H) {
            fundo.place(0, 0)
            cadLocal.place(cadX_local,   avLocalY)
            avLocal.place(avLocalX,      avLocalY)
            cadRemoto.place(cadX_remoto, avRemotoY)
            avRemoto.place(avRemotoX,    avRemotoY)
            mesaEsq.place(mesaEsqX, mesaY)
            mesaDir.place(mesaDirX, mesaY)
            maoLocal.place(avLocalX,  avLocalY)
            maoRemoto.place(avRemotoX, avRemotoY)
        }
    }
}