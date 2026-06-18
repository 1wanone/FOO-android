package playfoo.com.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.game.components.ContadorTentativas
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.game.components.ProgressoPalavra
import playfoo.com.ui.game.components.TecladoLetras
import playfoo.com.viewmodel.GameViewModel
import playfoo.com.viewmodel.ResultadoJogo

@Composable
fun GameScreen(
    onVoltar: () -> Unit = {},
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val estadoAvatar = when (uiState.estadoAvatar) {
        "VITORIA" -> EstadoAvatar.VITORIA
        "DERROTA" -> EstadoAvatar.DERROTA
        "ACERTOU" -> EstadoAvatar.ACERTOU
        "ERROU"   -> EstadoAvatar.ERROU
        else      -> EstadoAvatar.NEUTRO
    }

    // A Column divide a tela em 3 faixas fixas:
    // [HEADER] [CENÁRIO com personagem - weight(1f)] [TECLADO]
    // O GameBackground fica APENAS na faixa do cenário (weight 1f)
    // Assim o BottomCenter do GameBackground = topo do teclado, não fundo da tela
    Column(modifier = Modifier.fillMaxSize()) {

        // ── HEADER ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1117).copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.tema,
                color = Color(0xFF6C63FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            uiState.timerSegundos?.let { segundos ->
                val corTimer = if (segundos <= 10) Color(0xFFE53935) else Color(0xFF4CAF50)
                Text(
                    text = "⏱ %02d:%02d".format(segundos / 60, segundos % 60),
                    color = corTimer,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }

        // ── CENÁRIO: ocupa todo espaço entre header e teclado ───────
        // Box permite sobrepor o card de progresso sobre o cenário
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  // <-- expande para preencher o espaço disponível
        ) {
            // Fundo + cadeira + avatar + mesa — BottomCenter aqui = topo do teclado ✓
            GameBackground(
                avatarConfig = uiState.avatarConfig,
                estado = estadoAvatar,
                modifier = Modifier.fillMaxSize()
            )

            // Card de progresso flutuando no topo do cenário
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E2A3A).copy(alpha = 0.90f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProgressoPalavra(progresso = uiState.progresso)
                ContadorTentativas(
                    tentativasRestantes = uiState.tentativasRestantes,
                    tentativasMaximas   = uiState.tentativasMaximas
                )
            }
        }

        // ── TECLADO ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1117).copy(alpha = 0.88f))
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            TecladoLetras(
                letrasCorretas = uiState.letrasCorretas,
                letrasErradas  = uiState.letrasErradas,
                onLetraClick   = { viewModel.tentarLetra(it) },
                modifier       = Modifier.fillMaxWidth()
            )
        }
    }

    // Dialog de resultado (sobre tudo)
    if (uiState.resultado != ResultadoJogo.EM_ANDAMENTO) {
        val venceu = uiState.resultado == ResultadoJogo.VITORIA
        AlertDialog(
            onDismissRequest = {},
            containerColor = if (venceu) Color(0xFF1B4E2D) else Color(0xFF2E1A1A),
            title = {
                Text(
                    text = if (venceu) "🏆 Você venceu!" else "💀 Game Over",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = if (venceu) Color(0xFF4CAF50) else Color(0xFFE53935)
                )
            },
            text = {
                Text(
                    text = if (venceu) "Parabéns! Você acertou a palavra!"
                    else        "Não foi dessa vez. Tente novamente!",
                    color = Color.White.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotaoCartoon(
                        texto    = "🔄  Jogar novamente",
                        onClick  = { viewModel.reiniciar() },
                        tipo     = BotaoCartoonTipo.PRIMARIO,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotaoCartoon(
                        texto    = "← Voltar",
                        onClick  = onVoltar,
                        tipo     = BotaoCartoonTipo.PERIGO,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}