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
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.AvatarHandOnly
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
            // 0: Fundo
            Image(
                painter = painterResource(R.drawable.fundo_jogo),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            // 1: Cadeira
            Image(
                painter = painterResource(R.drawable.cadeira),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // 2: Avatar (Corpo sem a mão, fica atrás da mesa)
            AvatarView(
                config = avatarConfig,
                estado = estado,
                mostrarMao = false
            )

            // 3: Mesa (Fica na frente do corpo)
            Image(
                painter = painterResource(R.drawable.mesa_pc_crop),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // 4: Mão do Avatar (Fica na frente da mesa e do mouse)
            AvatarHandOnly(
                config = avatarConfig
            )
        }
    ) { measurables, constraints ->

        val W = constraints.maxWidth
        val H = constraints.maxHeight

        // Mesa
        val mesaW = W
        val mesaH = (W * 1240f / 2500f).toInt()
        val mesaY = H - mesaH

        // Avatar + cadeira (tamanhos)
        val avW = (W * 0.50f).toInt()
        val avH = (avW * 2400f / 2500f).toInt()

        // EIXO X: Encaixa horizontalmente a mão no mouse perfeitamente
        val avX = (W * 0.075f).toInt()

        // EIXO Y: Desce o avatar para a mão encostar no mouse sem flutuar
        val avY = mesaY - (avH * 0.43f).toInt()

        // AJUSTE DA CADEIRA: Empurra apenas a cadeira um pouco para a direita
        // para alinhar melhor com as costas do avatar. (Altere o 0.03f se precisar de mais ou menos ajuste).
        val cadeiraX = avX + (W * 0.03f).toInt()

        val fundo = measurables[0].measure(Constraints.fixed(W, H))
        val cadeira = measurables[1].measure(Constraints.fixed(avW, avH))
        val avatar = measurables[2].measure(Constraints.fixed(avW, avH))
        val mesa = measurables[3].measure(Constraints.fixed(mesaW, mesaH))
        val mao = measurables[4].measure(Constraints.fixed(avW, avH))

        layout(W, H) {
            fundo.place(0, 0)

            // A cadeira agora tem seu próprio eixo X (cadeiraX)
            cadeira.place(cadeiraX, avY)

            // O avatar e a mão usam a posição (avX) que estava perfeita
            avatar.place(avX, avY)
            mesa.place(0, mesaY)
            mao.place(avX, avY)
        }
    }
}