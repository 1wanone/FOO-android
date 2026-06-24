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

            // Fundo
            Image(
                painter = painterResource(R.drawable.fundo_jogo),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            // ===== JOGADOR LOCAL =====

            // Cadeira
            Image(
                painter = painterResource(R.drawable.cadeira),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // Corpo sem mão
            AvatarView(
                config = avatarConfigLocal,
                estado = estadoLocal,
                mostrarMao = false
            )

            // Mesa
            Image(
                painter = painterResource(R.drawable.mesa_pc_crop),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // Mão
            AvatarHandOnly(
                config = avatarConfigLocal
            )

            // ===== JOGADOR REMOTO =====

            // Cadeira
            Image(
                painter = painterResource(R.drawable.cadeira),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // Corpo sem mão
            AvatarView(
                config = avatarConfigRemoto,
                estado = estadoRemoto,
                mostrarMao = false
            )

            // Mesa
            Image(
                painter = painterResource(R.drawable.mesa_pc_crop),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            // Mão
            AvatarHandOnly(
                config = avatarConfigRemoto
            )
        }
    ) { measurables, constraints ->

        val W = constraints.maxWidth
        val H = constraints.maxHeight
        val half = W / 2

        // ===== MESA =====

        val mesaW = half
        val mesaH = (half * 1240f / 2500f).toInt()
        val mesaY = H - mesaH

        // ===== AVATAR =====
        // Mesmos cálculos do single player

        val avW = (half * 0.70f).toInt()
        val avH = (avW * 2400f / 2500f).toInt()

        val avX = (half * 0.075f).toInt()
        val avY = mesaY - (avH * 0.43f).toInt()

        val cadeiraX = avX + (half * 0.03f).toInt()

        // ===== MEDIÇÕES =====

        val fundo = measurables[0].measure(
            Constraints.fixed(W, H)
        )

        // Local
        val cadeiraLocal = measurables[1].measure(
            Constraints.fixed(avW, avH)
        )

        val avatarLocal = measurables[2].measure(
            Constraints.fixed(avW, avH)
        )

        val mesaLocal = measurables[3].measure(
            Constraints.fixed(mesaW, mesaH)
        )

        val maoLocal = measurables[4].measure(
            Constraints.fixed(avW, avH)
        )

        // Remoto
        val cadeiraRemota = measurables[5].measure(
            Constraints.fixed(avW, avH)
        )

        val avatarRemoto = measurables[6].measure(
            Constraints.fixed(avW, avH)
        )

        val mesaRemota = measurables[7].measure(
            Constraints.fixed(mesaW, mesaH)
        )

        val maoRemota = measurables[8].measure(
            Constraints.fixed(avW, avH)
        )

        layout(W, H) {

            fundo.place(0, 0)

            // =====================
            // JOGADOR LOCAL
            // =====================

            cadeiraLocal.place(
                cadeiraX,
                avY
            )

            avatarLocal.place(
                avX,
                avY
            )

            mesaLocal.place(
                0,
                mesaY
            )

            maoLocal.place(
                avX,
                avY
            )

            // =====================
            // JOGADOR REMOTO
            // =====================

            cadeiraRemota.place(
                half + cadeiraX,
                avY
            )

            avatarRemoto.place(
                half + avX,
                avY
            )

            mesaRemota.place(
                half,
                mesaY
            )

            maoRemota.place(
                half + avX,
                avY
            )
        }
    }
}