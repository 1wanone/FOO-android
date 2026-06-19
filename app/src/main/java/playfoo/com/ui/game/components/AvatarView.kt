package playfoo.com.ui.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import playfoo.com.R
import playfoo.com.domain.AvatarConfig

@Composable
fun AvatarView(
    config: AvatarConfig,
    estado: EstadoAvatar = EstadoAvatar.NEUTRO,
    mostrarMao: Boolean = true, // Flag para controlar a visibilidade da mão
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        // CAMADA 0 — Tom de pele
        val peleRes = when (config.tonDePele) {
            "muito_claro", "claro" -> R.drawable.pele_branca
            "medio_claro" -> R.drawable.base_rosa
            "medio", "medio_escuro" -> R.drawable.pele_parda
            "escuro" -> R.drawable.pele_negra
            else -> R.drawable.pele_parda
        }

        Image(
            painter = painterResource(peleRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // CAMADA 1 — Mão (Desenha apenas se mostrarMao for true)
        if (mostrarMao) {
            val maoRes = when (config.tonDePele) {
                "muito_claro", "claro" -> R.drawable.mao_branca
                "medio_claro" -> R.drawable.mao_rosa
                "medio", "medio_escuro" -> R.drawable.mao_parda
                "escuro" -> R.drawable.mao_negra
                else -> R.drawable.mao_parda
            }

            Image(
                painter = painterResource(maoRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // CAMADA 2 — Camisa
        val camisaRes = when (config.camisa) {
            "divas" -> R.drawable.camisa_divas
            else -> R.drawable.camisa_maniva
        }

        Image(
            painter = painterResource(camisaRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // CAMADA 3 — Cabelo
        val cabeloKey = "${config.cabelo}_${config.corCabelo}"

        val cabeloRes = when (cabeloKey) {
            "curto_preto" -> R.drawable.cabelo_curto_preto
            "curto_loiro" -> R.drawable.cabelo_curto_loiro
            "curto_castanho" -> R.drawable.cabelo_curto_marrom
            "curto_ruivo" -> R.drawable.cabelo_curto_ruivo

            "liso_preto", "longo_preto" -> R.drawable.cabelo_liso_preto
            "liso_loiro", "longo_loiro" -> R.drawable.cabelo_liso_loiro
            "liso_castanho", "longo_castanho" -> R.drawable.cabelo_liso_marrom
            "liso_ruivo", "longo_ruivo" -> R.drawable.cabelo_liso_ruivo

            "cacheado_preto", "afro_preto" -> R.drawable.cabelo_afro_preto
            "cacheado_loiro", "afro_loiro" -> R.drawable.cabelo_afro_loiro
            "cacheado_castanho", "afro_castanho" -> R.drawable.cabelo_afro_marrom
            "cacheado_ruivo", "afro_ruivo" -> R.drawable.cabelo_afro_ruivo

            "raspado_preto", "fade_preto" -> R.drawable.cabelo_fade_preto
            "raspado_loiro", "fade_loiro" -> R.drawable.cabelo_fade_loiro
            "raspado_castanho", "fade_castanho" -> R.drawable.cabelo_fade_marrom
            "raspado_ruivo", "fade_ruivo" -> R.drawable.cabelo_fade_ruivo

            else -> R.drawable.cabelo_curto_preto
        }

        Image(
            painter = painterResource(cabeloRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // CAMADA 4 — Expressão
        val faceRes = when (estado) {
            EstadoAvatar.NEUTRO -> R.drawable.face_neutro
            EstadoAvatar.ACERTOU -> R.drawable.face_feliz
            EstadoAvatar.VITORIA -> R.drawable.face_feliz
            EstadoAvatar.ERROU -> R.drawable.face_surpresa
            EstadoAvatar.DERROTA -> R.drawable.face_triste
        }

        Image(
            painter = painterResource(faceRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

// NOVO COMPONENTE: Desenha apenas a mão para colocarmos por cima da mesa
@Composable
fun AvatarHandOnly(
    config: AvatarConfig,
    modifier: Modifier = Modifier
) {
    val maoRes = when (config.tonDePele) {
        "muito_claro", "claro" -> R.drawable.mao_branca
        "medio_claro" -> R.drawable.mao_rosa
        "medio", "medio_escuro" -> R.drawable.mao_parda
        "escuro" -> R.drawable.mao_negra
        else -> R.drawable.mao_parda
    }

    Image(
        painter = painterResource(maoRes),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}