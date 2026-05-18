package playfoo.com.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BotaoLetra(
    letra: Char,
    estado: EstadoLetra,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (estado) {
        EstadoLetra.DISPONIVEL -> MaterialTheme.colorScheme.primaryContainer
        EstadoLetra.CORRETA    -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoLetra.ERRADA     -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (estado) {
        EstadoLetra.DISPONIVEL -> MaterialTheme.colorScheme.onPrimaryContainer
        EstadoLetra.CORRETA    -> MaterialTheme.colorScheme.onTertiaryContainer
        EstadoLetra.ERRADA     -> MaterialTheme.colorScheme.onErrorContainer
    }
    Button(
        onClick = onClick,
        enabled = estado == EstadoLetra.DISPONIVEL,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.size(40.dp)
    ) {
        Text(text = letra.toString(), fontSize = 16.sp)
    }
}

enum class EstadoLetra { DISPONIVEL, CORRETA, ERRADA }
