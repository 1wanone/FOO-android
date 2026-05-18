package playfoo.com.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val LINHAS_TECLADO = listOf(
    listOf('Q','W','E','R','T','Y','U','I','O','P'),
    listOf('A','S','D','F','G','H','J','K','L'),
    listOf('Z','X','C','V','B','N','M')
)

@Composable
fun TecladoLetras(
    letrasCorretas: Set<Char>,
    letrasErradas: Set<Char>,
    onLetraClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LINHAS_TECLADO.forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                linha.forEach { letra ->
                    val estado = when (letra) {
                        in letrasCorretas -> EstadoLetra.CORRETA
                        in letrasErradas  -> EstadoLetra.ERRADA
                        else              -> EstadoLetra.DISPONIVEL
                    }
                    BotaoLetra(
                        letra = letra,
                        estado = estado,
                        onClick = { onLetraClick(letra) }
                    )
                }
            }
        }
    }
}
