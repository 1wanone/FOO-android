package playfoo.com.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContadorTentativas(
    tentativasRestantes: Int,
    tentativasMaximas: Int,
    modifier: Modifier = Modifier
) {
    val cor = when {
        tentativasRestantes > tentativasMaximas / 2 -> MaterialTheme.colorScheme.tertiary
        tentativasRestantes > 1                     -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else                                        -> MaterialTheme.colorScheme.error
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tentativas restantes",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(tentativasMaximas) { i ->
                Text(
                    text = if (i < tentativasRestantes) "❤️" else "🖤",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
