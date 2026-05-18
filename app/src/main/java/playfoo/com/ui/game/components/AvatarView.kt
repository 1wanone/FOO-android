package playfoo.com.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import playfoo.com.domain.AvatarConfig

@Composable
fun AvatarView(
    config: AvatarConfig,
    estado: EstadoAvatar = EstadoAvatar.NEUTRO,
    modifier: Modifier = Modifier
) {
    // TODO: substituir pelos assets
    // NEUTRO  -> avatar digitando normalmente
    // ACERTOU -> avatar feliz
    // ERROU   -> avatar bravo, parou de digitar
    // VITORIA -> avatar com estrelas na cabeça
    // DERROTA -> avatar triste
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "[$estado]",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
