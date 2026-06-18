package playfoo.com.ui.selecionar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import playfoo.com.domain.Dificuldade
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
import playfoo.com.viewmodel.SelecionarTemaViewModel

@Composable
fun SelecionarTemaScreen(
    navController: NavController,
    viewModel: SelecionarTemaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var temaIdSelecionado by remember { mutableStateOf<Int?>(null) }

    FundoTela(tipo = TipoFundo.JOGO) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Text(
                    text = "ESCOLHA O TEMA",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Grid 2 colunas de temas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(uiState.temas, key = { it.id }) { tema ->
                    CardTema(
                        emoji = emojiParaTema(tema.nome),
                        nome = tema.nome,
                        quantPalavras = tema.palavras.size,
                        selecionado = temaIdSelecionado == tema.id,
                        onClick = { temaIdSelecionado = tema.id }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Label dificuldade
            Text(
                text = "DIFICULDADE",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(8.dp))

            // Botões de dificuldade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotaoDificuldade(
                    emoji = "😊",
                    texto = "FÁCIL",
                    cor = Color(0xFF4CAF50),
                    selecionado = uiState.dificuldade == Dificuldade.FACIL,
                    onClick = { viewModel.selecionarDificuldade(Dificuldade.FACIL) },
                    modifier = Modifier.weight(1f)
                )
                BotaoDificuldade(
                    emoji = "😐",
                    texto = "NORMAL",
                    cor = Color(0xFFFF9800),
                    selecionado = uiState.dificuldade == Dificuldade.NORMAL,
                    onClick = { viewModel.selecionarDificuldade(Dificuldade.NORMAL) },
                    modifier = Modifier.weight(1f)
                )
                BotaoDificuldade(
                    emoji = "😤",
                    texto = "DIFÍCIL",
                    cor = Color(0xFFE53935),
                    selecionado = uiState.dificuldade == Dificuldade.DIFICIL,
                    onClick = { viewModel.selecionarDificuldade(Dificuldade.DIFICIL) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            BotaoCartoon(
                texto = "▶  JOGAR",
                onClick = {
                    temaIdSelecionado?.let { id ->
                        navController.navigate("jogo/$id/${uiState.dificuldade.name}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                tipo = BotaoCartoonTipo.PRIMARIO,
                habilitado = temaIdSelecionado != null,
                altura = 64.dp,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CardTema(
    emoji: String,
    nome: String,
    quantPalavras: Int,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selecionado) 1.04f else 1f,
        label = "scale_tema"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .shadow(if (selecionado) 12.dp else 4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selecionado) Color(0xFF6C63FF).copy(alpha = 0.28f)
                else Color(0xFF1E2A3A).copy(alpha = 0.9f)
            )
            .border(
                width = if (selecionado) 2.dp else 1.dp,
                color = if (selecionado) Color(0xFF6C63FF) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Text(
                text = nome,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = "$quantPalavras palavras",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun BotaoDificuldade(
    emoji: String,
    texto: String,
    cor: Color,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selecionado) 1.05f else 1f,
        label = "scale_dif"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(64.dp)
            .shadow(if (selecionado) 8.dp else 3.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (selecionado) cor else cor.copy(alpha = 0.6f))
            .then(
                if (selecionado)
                    Modifier.border(2.dp, Color.White, RoundedCornerShape(16.dp))
                else
                    Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = texto,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private fun emojiParaTema(nome: String): String {
    val n = nome.lowercase()
    return when {
        "exceç" in n || "excecao" in n       -> "🚨"
        "herança" in n || "heranca" in n      -> "🧬"
        "polimorf" in n                        -> "🔄"
        "encapsulamento" in n                  -> "🔒"
        "introduç" in n || "introduca" in n   -> "📖"
        "relacionamento" in n                  -> "🔗"
        "interface gráfica" in n
            || "interface grafica" in n        -> "🖥️"
        "interface" in n                       -> "🤝"
        "coleç" in n || "colecao" in n         -> "📦"
        else                                   -> "📚"
    }
}
