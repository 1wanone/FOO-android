package playfoo.com.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import playfoo.com.domain.PalavraFrequencia
import playfoo.com.ui.theme.AzulCinza
import playfoo.com.ui.theme.Ciano
import playfoo.com.ui.theme.Pink
import playfoo.com.ui.theme.Rosa
import kotlin.random.Random

@Composable
fun NuvemPalavras(
    palavras: List<PalavraFrequencia>,
    modifier: Modifier = Modifier
) {
    if (palavras.isEmpty()) return

    val maxFreq = palavras.maxOf { it.frequencia }
    val minFreq = palavras.minOf { it.frequencia }
    val cores = listOf(Rosa, Pink, Ciano, AzulCinza, Color(0xFFFFA726))

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        palavras.shuffled(Random(palavras.size)).forEach { item ->
            val proporcao = if (maxFreq == minFreq) 0.5f
                else (item.frequencia - minFreq).toFloat() / (maxFreq - minFreq)
            val tamanhoFonte = (14 + proporcao * 22).sp
            val cor = cores[item.palavra.hashCode().let { if (it < 0) -it else it } % cores.size]
            val peso = when {
                proporcao > 0.6f -> FontWeight.ExtraBold
                proporcao > 0.3f -> FontWeight.Bold
                else             -> FontWeight.Medium
            }
            BasicText(
                text = item.palavra.uppercase(),
                style = TextStyle(
                    fontSize   = tamanhoFonte,
                    fontWeight = peso,
                    color      = cor
                )
            )
        }
    }
}