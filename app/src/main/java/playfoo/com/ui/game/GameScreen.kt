package playfoo.com.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.game.components.AvatarView
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

    GameBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header compacto: tema + timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2A3A).copy(alpha = 0.85f))
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
                        val minutos = segundos / 60
                        val segs = segundos % 60
                        val corTimer = if (segundos <= 10) Color(0xFFE53935) else Color(0xFF4CAF50)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⏱", fontSize = 14.sp)
                            Text(
                                text = "%02d:%02d".format(minutos, segs),
                                color = corTimer,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Corações / tentativas
                ContadorTentativas(
                    tentativasRestantes = uiState.tentativasRestantes,
                    tentativasMaximas   = uiState.tentativasMaximas
                )

                // Progresso da palavra em destaque
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2A3A).copy(alpha = 0.85f))
                        .padding(vertical = 20.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressoPalavra(progresso = uiState.progresso)
                }

                Spacer(Modifier.weight(1f))

                TecladoLetras(
                    letrasCorretas = uiState.letrasCorretas,
                    letrasErradas  = uiState.letrasErradas,
                    onLetraClick   = { viewModel.tentarLetra(it) },
                    modifier       = Modifier.fillMaxWidth()
                )
            }

            // Avatar na frente da mesa (camada sobre o fundo)
            AvatarView(
                config = AvatarConfig(),
                estado = when (uiState.estadoAvatar) {
                    "VITORIA" -> EstadoAvatar.VITORIA
                    "DERROTA" -> EstadoAvatar.DERROTA
                    "ACERTOU" -> EstadoAvatar.ACERTOU
                    "ERROU"   -> EstadoAvatar.ERROU
                    else      -> EstadoAvatar.NEUTRO
                },
                modifier = Modifier
                    .fillMaxHeight(0.42f)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-80).dp)
            )

            // Dialog de resultado
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
                                texto = "🔄  Jogar novamente",
                                onClick = { viewModel.reiniciar() },
                                tipo = BotaoCartoonTipo.PRIMARIO,
                                modifier = Modifier.fillMaxWidth()
                            )
                            BotaoCartoon(
                                texto = "← Voltar",
                                onClick = onVoltar,
                                tipo = BotaoCartoonTipo.PERIGO,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            }
        }
    }
}