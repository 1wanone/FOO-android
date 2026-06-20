package playfoo.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import playfoo.com.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(
    val icone: ImageVector,
    val label: String,
    val rota: String
)

@Composable
fun BottomNavBar(
    rotaAtual: String,
    items: List<NavItem>,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RoxoEscuro)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val selecionado = rotaAtual == item.rota
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavegar(item.rota) }
                    .padding(vertical = 4.dp)
            ) {
                FooIcone(
                    icone   = item.icone,
                    cor     = if (selecionado) Rosa else AzulCinza,
                    tamanho = if (selecionado) 28.dp else 24.dp
                )
                Text(
                    text       = item.label,
                    fontSize   = 10.sp,
                    color      = if (selecionado) Rosa else AzulCinza,
                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
