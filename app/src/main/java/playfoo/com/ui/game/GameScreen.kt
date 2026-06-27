package playfoo.com.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.FooIcone
import playfoo.com.ui.components.FooIcones
import playfoo.com.ui.game.components.ContadorTentativas
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.game.components.ProgressoPalavra
import playfoo.com.ui.game.components.TecladoLetras
import playfoo.com.viewmodel.GameViewModel
import playfoo.com.viewmodel.ResultadoJogo
import playfoo.com.ui.theme.*

@Composable
fun GameScreen(
    onVoltar: () -> Unit = {},
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarConfirmacaoSair by remember { mutableStateOf(false) }

    val audio = LocalAudioManager.current

    // Efeitos sonoros reativos ao estado
    val resultado = uiState.resultado
    val estadoAvatarStr = uiState.estadoAvatar
    LaunchedEffect(estadoAvatarStr) {
        when (estadoAvatarStr) {
            "ACERTOU" -> audio?.playCorrect()
            "ERROU"   -> audio?.playErro()
            "VITORIA" -> audio?.playVictory()
            "DERROTA" -> audio?.playDerrota()
        }
    }

    val estadoAvatar = when (uiState.estadoAvatar) {
        "VITORIA" -> EstadoAvatar.VITORIA
        "DERROTA" -> EstadoAvatar.DERROTA
        "ACERTOU" -> EstadoAvatar.ACERTOU
        "ERROU"   -> EstadoAvatar.ERROU
        else      -> EstadoAvatar.NEUTRO
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── HEADER ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RoxoEscuro.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { mostrarConfirmacaoSair = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Tema: ${uiState.tema}",
                    color = Rosa,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            uiState.timerSegundos?.let { segundos ->
                val corTimer = if (segundos <= 10) ErroVermelho else Ciano
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FooIcone(icone = FooIcones.Tempo, cor = corTimer, tamanho = 16.dp)
                    Text(
                        text = "%02d:%02d".format(segundos / 60, segundos % 60),
                        color = corTimer,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // ── CENÁRIO: ocupa todo espaço entre header e teclado ───────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GameBackground(
                avatarConfig = uiState.avatarConfig,
                estado = estadoAvatar,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, RoxoMedio, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(FundoCard)
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
                .background(FundoTeclado)
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            TecladoLetras(
                letrasCorretas = uiState.letrasCorretas,
                letrasErradas  = uiState.letrasErradas,
                onLetraClick   = { audio?.playClick(); viewModel.tentarLetra(it) },
                modifier       = Modifier.fillMaxWidth()
            )
        }
    }

    // Dialog de resultado
    if (uiState.resultado != ResultadoJogo.EM_ANDAMENTO) {
        val venceu = uiState.resultado == ResultadoJogo.VITORIA
        Dialog(onDismissRequest = {}) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (venceu) Color(0xFF003B3A) else Color(0xFF4A0020))
                    .border(
                        2.dp,
                        if (venceu) Ciano else ErroVermelho,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FooIcone(
                    icone   = if (venceu) FooIcones.Trofeu else FooIcones.Codigo,
                    cor     = if (venceu) Ciano else ErroVermelho,
                    tamanho = 56.dp
                )
                Text(
                    text = if (venceu) "Você venceu!" else "Game Over",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (venceu) Ciano else ErroVermelho
                )
                Text(
                    text = if (venceu) "Parabéns! Você acertou a palavra!"
                           else "Não foi dessa vez. Tente novamente!",
                    color = Color.White.copy(alpha = 0.80f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                BotaoCartoon(
                    texto    = "Jogar novamente",
                    icone    = FooIcones.Jogar,
                    onClick  = { viewModel.reiniciar() },
                    tipo     = BotaoCartoonTipo.PRIMARIO,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoCartoon(
                    texto    = "Voltar",
                    icone    = FooIcones.Voltar,
                    onClick  = onVoltar,
                    tipo     = BotaoCartoonTipo.PERIGO,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Dialog de confirmação para sair do jogo
    if (mostrarConfirmacaoSair) {
        Dialog(onDismissRequest = { mostrarConfirmacaoSair = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(FundoCard)
                    .border(2.dp, ErroVermelho, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FooIcone(icone = FooIcones.Aviso, cor = ErroVermelho, tamanho = 48.dp)
                Text(
                    text       = "Sair do jogo?",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White
                )
                Text(
                    text      = "Seu progresso atual será perdido.",
                    color     = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontSize  = 14.sp
                )
                BotaoCartoon(
                    texto    = "Continuar jogando",
                    onClick  = { mostrarConfirmacaoSair = false },
                    tipo     = BotaoCartoonTipo.NEUTRO,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoCartoon(
                    texto    = "Sair",
                    icone    = FooIcones.Sair,
                    onClick  = { mostrarConfirmacaoSair = false; onVoltar() },
                    tipo     = BotaoCartoonTipo.PERIGO,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}