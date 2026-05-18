package playfoo.com.ui.perfil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class TomDePele(val id: String, val cor: Color)

val TONS_DE_PELE = listOf(
    TomDePele("muito_claro",  Color(0xFFFFDBAC)),
    TomDePele("claro",        Color(0xFFF1C27D)),
    TomDePele("medio_claro",  Color(0xFFE0AC69)),
    TomDePele("medio",        Color(0xFFC68642)),
    TomDePele("medio_escuro", Color(0xFF8D5524)),
    TomDePele("escuro",       Color(0xFF4A2912))
)

@Composable
fun SeletorTomDePele(
    tomSelecionado: String,
    onSelecionar: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TONS_DE_PELE.forEach { tom ->
            val selecionado = tomSelecionado == tom.id
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tom.cor)
                    .then(
                        if (selecionado)
                            Modifier
                                .border(3.dp, Color.White, CircleShape)
                                .border(5.dp, Color(0xFF333333), CircleShape)
                        else
                            Modifier.border(2.dp, Color(0x44000000), CircleShape)
                    )
                    .clickable { onSelecionar(tom.id) }
            )
        }
    }
}
