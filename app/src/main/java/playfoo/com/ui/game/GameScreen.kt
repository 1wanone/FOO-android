package playfoo.com.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.domain.*
import playfoo.com.ui.game.components.*
import playfoo.com.viewmodel.GameViewModel
import playfoo.com.viewmodel.ResultadoJogo

@Composable
fun GameScreen(
    onVoltar: () -> Unit = {},
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tema: ${uiState.tema}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Timer — visível apenas em NORMAL e DIFICIL
            uiState.timerSegundos?.let { segundos ->
                val minutos = segundos / 60
                val segs = segundos % 60
                val corTimer = if (segundos <= 10) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                Text(
                    text = "%02d:%02d".format(minutos, segs),
                    style = MaterialTheme.typography.headlineSmall,
                    color = corTimer,
                    fontWeight = FontWeight.Bold
                )
            }

            AvatarView(
                config = AvatarConfig(),
                estado = when (uiState.estadoAvatar) {
                    "VITORIA" -> EstadoAvatar.VITORIA
                    "DERROTA" -> EstadoAvatar.DERROTA
                    "ACERTOU" -> EstadoAvatar.ACERTOU
                    "ERROU"   -> EstadoAvatar.ERROU
                    else      -> EstadoAvatar.NEUTRO
                },
                modifier = Modifier.height(160.dp)
            )

            ContadorTentativas(
                tentativasRestantes = uiState.tentativasRestantes,
                tentativasMaximas   = uiState.tentativasMaximas
            )

            ProgressoPalavra(progresso = uiState.progresso)

            Spacer(Modifier.weight(1f))

            TecladoLetras(
                letrasCorretas = uiState.letrasCorretas,
                letrasErradas  = uiState.letrasErradas,
                onLetraClick   = { viewModel.tentarLetra(it) },
                modifier       = Modifier.fillMaxWidth()
            )
        }

        if (uiState.resultado != ResultadoJogo.EM_ANDAMENTO) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = if (uiState.resultado == ResultadoJogo.VITORIA) "Você venceu!" else "Game over",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        if (uiState.resultado == ResultadoJogo.VITORIA)
                            "Parabéns! Você acertou a palavra!"
                        else
                            "Não foi dessa vez. Tente novamente!"
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.reiniciar() }) { Text("Jogar novamente") }
                },
                dismissButton = {
                    OutlinedButton(onClick = onVoltar) { Text("Voltar") }
                }
            )
        }
    }
}
