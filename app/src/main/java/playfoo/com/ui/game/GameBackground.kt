package playfoo.com.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import playfoo.com.R
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar

@Composable
fun GameBackground(
    avatarConfig: AvatarConfig = AvatarConfig(),
    estado: EstadoAvatar = EstadoAvatar.NEUTRO,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            // 0: fundo
            Image(painterResource(R.drawable.fundo_jogo), null, contentScale = ContentScale.Crop)
            // 1: cadeira (crop 2500x2400)
            Image(painterResource(R.drawable.cadeira), null, contentScale = ContentScale.FillBounds)
            // 2: avatar
            AvatarView(config = avatarConfig, estado = estado)
            // 3: mesa (crop 2500x1240)
            Image(painterResource(R.drawable.mesa_pc_crop), null, contentScale = ContentScale.FillBounds)
        }
    ) { measurables, constraints ->
        val W = constraints.maxWidth
        val H = constraints.maxHeight

        // Mesa: 100% largura, proporção 2500x1240
        val mesaW = W
        val mesaH = (W * 1240f / 2500f).toInt()
        val mesaY = H - mesaH

        // Avatar+cadeira: 60% largura, proporção 2500x2400 (cropado y=600..3000)
        val avW = (W * 0.60f).toInt()
        val avH = (avW * 2400f / 2500f).toInt()
        val avX = (W * 0.04f).toInt()
        val avY = H - avH

        val fundo = measurables[0].measure(Constraints.fixed(W, H))
        val cad   = measurables[1].measure(Constraints.fixed(avW, avH))
        val av    = measurables[2].measure(Constraints.fixed(avW, avH))
        val mesa  = measurables[3].measure(Constraints.fixed(mesaW, mesaH))

        layout(W, H) {
            fundo.place(0,   0)
            cad.place(avX,   avY)
            av.place(avX,    avY)
            mesa.place(0,    mesaY)
        }
    }
}