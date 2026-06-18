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
 * Cenário do jogo dividido em duas faixas verticais:
 *
 *  ┌─────────────────┐  ← topo da tela
 *  │                 │
 *  │   fundo_jogo    │  faixa CENÁRIO (cenariaFraction da altura)
 *  │  (fundo livre)  │
 *  │                 │
 *  │ [cadeira][avtr] │  ← parte inferior do cenário
 *  │ [mesa_pc cover] │  ← mesa sobrepõe cintura do avatar
 *  ├─────────────────┤
 *  │   (teclado)     │  faixa TECLADO — GameBackground não renderiza aqui
 *  └─────────────────┘
 *
 *  cenariaFraction = 1f - tecladoFraction
 *  O truque: usamos Column com peso para separar as duas faixas.
 *  Dentro da faixa do cenário, usamos Box com align — sem offset.
 */
@Composable
fun GameBackground(
    avatarConfig: AvatarConfig = AvatarConfig(),
    estado: EstadoAvatar = EstadoAvatar.NEUTRO,
    // Fração da tela que o teclado ocupa (padrão ≈ 136dp em ~700dp = ~0.19)
    tecladoFraction: Float = 0.20f,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Fundo cobre TELA TODA (inclusive área do teclado)
        Image(
            painter = painterResource(R.drawable.fundo_jogo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Divide a tela em [cenário | espaço teclado]
        Column(modifier = Modifier.fillMaxSize()) {

            // ── FAIXA DO CENÁRIO ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f - tecladoFraction)
            ) {
                // Nada aqui — o fundo já está no Box pai
            }

            // ── FAIXA ONDE FICAM CENÁRIO + PERSONAGEM ────────────────────
            // Usa a faixa inferior do cenário (não a do teclado!)
            // As camadas do personagem ficam neste Box, alinhadas ao Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(tecladoFraction * 2.5f)  // ~50% da tela para mesa+personagem
            ) {
                // 1. Cadeira — atrás do personagem
                Image(
                    painter = painterResource(R.drawable.cadeira),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(0.95f)
                        .align(Alignment.BottomCenter),
                    contentScale = ContentScale.Fit
                )

                // 2. Avatar — na frente da cadeira
                AvatarView(
                    config = avatarConfig,
                    estado = estado,
                    modifier = Modifier
                        .fillMaxWidth(0.52f)
                        .fillMaxHeight()
                        .align(Alignment.BottomCenter)
                )

                // 3. Mesa — NA FRENTE do avatar (renderiza por cima)
                //    Cobre a parte inferior do corpo criando ilusão de sentar
                Image(
                    painter = painterResource(R.drawable.mesa_pc),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.42f)
                        .align(Alignment.BottomCenter),
                    contentScale = ContentScale.FillWidth
                )
            }

            // ── FAIXA DO TECLADO — apenas espaço reservado ───────────────
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(tecladoFraction)
            )
        }
    }
}