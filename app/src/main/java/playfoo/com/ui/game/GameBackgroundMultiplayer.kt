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

/**
 * Dois avatares sentados, cada um em metade da tela.
 * Baseado EXATAMENTE no GameBackground.kt do single player:
 *   - mesma proporção de avatar: 60% da metade = 30% da tela total
 *   - mesma proporção de mesa: 100% da metade cada
 *   - mesmo cálculo de avY: H - avH (base alinhada ao fundo do cenário)
 *   - avatar local à esquerda (avX = 4% da metade)
 *   - avatar remoto à direita (avX = metade + 4% da metade)
 *   - mesma ordem de camadas: fundo → cadeira → avatar → mesa
 */
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
            // 1: cadeira local
            Image(painterResource(R.drawable.cadeira), null,
                contentScale = ContentScale.FillBounds)
            // 2: avatar local
            AvatarView(config = avatarConfigLocal, estado = estadoLocal)
            // 3: mesa esquerda
            Image(painterResource(R.drawable.mesa_pc_crop), null,
                contentScale = ContentScale.FillBounds)
            // 4: cadeira remota
            Image(painterResource(R.drawable.cadeira), null,
                contentScale = ContentScale.FillBounds)
            // 5: avatar remoto
            AvatarView(config = avatarConfigRemoto, estado = estadoRemoto)
            // 6: mesa direita
            Image(painterResource(R.drawable.mesa_pc_crop), null,
                contentScale = ContentScale.FillBounds)
        }
    ) { measurables, constraints ->
        val W = constraints.maxWidth
        val H = constraints.maxHeight

        // Cada lado ocupa metade da largura total
        val metade = W / 2

        // Mesa: 100% da metade, proporção 2500x1240 — igual ao single player
        val mesaW = metade
        val mesaH = (mesaW * 1240f / 2500f).toInt()
        val mesaY = H - mesaH

        // Avatar+cadeira: 60% da metade, proporção 2500x2400 — igual ao single player
        val avW = (metade * 0.60f).toInt()
        val avH = (avW * 2400f / 2500f).toInt()
        val avY = H - avH  // base alinhada ao fundo, IGUAL ao single player

        // Posição X local: 4% da metade do lado esquerdo
        val avLocalX = (metade * 0.04f).toInt()

        // Posição X remoto: metade + 4% da metade (espelho do local)
        val avRemotoX = metade + (metade * 0.04f).toInt()

        // Cadeira: mesmo X e Y do avatar (atrás do personagem)
        val cadLocalX  = avLocalX
        val cadRemotoX = avRemotoX

        val fundo     = measurables[0].measure(Constraints.fixed(W, H))
        val cadLocal  = measurables[1].measure(Constraints.fixed(avW, avH))
        val avLocal   = measurables[2].measure(Constraints.fixed(avW, avH))
        val mesaEsq   = measurables[3].measure(Constraints.fixed(mesaW, mesaH))
        val cadRemoto = measurables[4].measure(Constraints.fixed(avW, avH))
        val avRemoto  = measurables[5].measure(Constraints.fixed(avW, avH))
        val mesaDir   = measurables[6].measure(Constraints.fixed(mesaW, mesaH))

        layout(W, H) {
            fundo.place(0, 0)
            // Lado esquerdo: cadeira → avatar → mesa
            cadLocal.place(cadLocalX, avY)
            avLocal.place(avLocalX,   avY)
            mesaEsq.place(0,          mesaY)
            // Lado direito: cadeira → avatar → mesa
            cadRemoto.place(cadRemotoX, avY)
            avRemoto.place(avRemotoX,   avY)
            mesaDir.place(metade,       mesaY)
        }
    }
}