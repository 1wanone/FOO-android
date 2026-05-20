package playfoo.com.ui.multiplayer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.domain.AvatarConfig
import playfoo.com.domain.Dificuldade
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.CardCartoon
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.game.components.ProgressoPalavra
import playfoo.com.ui.game.components.TecladoLetras
import playfoo.com.viewmodel.MultiplayerUiState
import playfoo.com.viewmodel.MultiplayerViewModel
import playfoo.com.viewmodel.TelaMultiplayer

@Composable
fun MultiplayerScreen(
    onVoltar: () -> Unit = {},
    viewModel: MultiplayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.tela == TelaMultiplayer.CRIAR || uiState.tela == TelaMultiplayer.ENTRAR) {
        viewModel.irPara(TelaMultiplayer.INICIAL)
    }

    FundoTela(tipo = TipoFundo.MULTIPLAYER) {
        AnimatedContent(
            targetState = uiState.tela,
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it }) togetherWith
                    (fadeOut() + slideOutHorizontally { -it })
            },
            label = "transicao_multiplayer"
        ) { tela ->
            when (tela) {
                TelaMultiplayer.INICIAL  -> TelaInicial(
                    onVoltar = onVoltar,
                    onCriar  = { viewModel.irPara(TelaMultiplayer.CRIAR) },
                    onEntrar = { viewModel.irPara(TelaMultiplayer.ENTRAR) }
                )
                TelaMultiplayer.CRIAR    -> TelaCriar(
                    carregando = uiState.carregando,
                    erro       = uiState.erro,
                    onCriar    = viewModel::criarSala,
                    onVoltar   = { viewModel.irPara(TelaMultiplayer.INICIAL) }
                )
                TelaMultiplayer.AGUARDAR -> TelaAguardar(uiState = uiState)
                TelaMultiplayer.ENTRAR   -> TelaEntrar(
                    carregando  = uiState.carregando,
                    erro        = uiState.erro,
                    onEntrar    = viewModel::entrarNaSala,
                    onVoltar    = { viewModel.irPara(TelaMultiplayer.INICIAL) },
                    onLimparErro = viewModel::limparErro
                )
                TelaMultiplayer.JOGAR    -> TelaJogar(
                    uiState  = uiState,
                    onLetra  = viewModel::tentarLetra
                )
                TelaMultiplayer.RESULTADO -> TelaResultado(
                    uiState          = uiState,
                    onJogarNovamente = viewModel::reiniciar,
                    onVoltar         = onVoltar
                )
            }
        }
    }
}

// ── TELA INICIAL ─────────────────────────────────────────────────────────────

@Composable
private fun TelaInicial(
    onVoltar: () -> Unit,
    onCriar: () -> Unit,
    onEntrar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                text = "Multiplayer",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))

        Text("⚔️", fontSize = 80.sp, textAlign = TextAlign.Center)

        Text(
            text = "Desafie um amigo!",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Ambos jogam a mesma palavra ao mesmo tempo.\nQuem revelar primeiro vence!",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        BotaoCartoon(
            texto    = "Criar Sala",
            onClick  = onCriar,
            tipo     = BotaoCartoonTipo.PRIMARIO,
            modifier = Modifier.fillMaxWidth()
        )
        BotaoCartoon(
            texto    = "Entrar com Código",
            onClick  = onEntrar,
            tipo     = BotaoCartoonTipo.SECUNDARIO,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── TELA CRIAR ───────────────────────────────────────────────────────────────

@Composable
private fun TelaCriar(
    carregando: Boolean,
    erro: String?,
    onCriar: (Dificuldade) -> Unit,
    onVoltar: () -> Unit
) {
    var dificuldadeSelecionada by remember { mutableStateOf(Dificuldade.NORMAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text("Criar Sala", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Escolha a dificuldade",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            Dificuldade.entries.forEach { dif ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = dificuldadeSelecionada == dif,
                        onClick  = { dificuldadeSelecionada = dif },
                        colors   = RadioButtonDefaults.colors(selectedColor = Color(0xFF6C63FF))
                    )
                    Column {
                        Text(
                            text = when (dif) {
                                Dificuldade.FACIL   -> "Fácil  — ${dif.tentativasMaximas} tentativas, sem timer"
                                Dificuldade.NORMAL  -> "Normal — ${dif.tentativasMaximas} tentativas, 2 min"
                                Dificuldade.DIFICIL -> "Difícil — ${dif.tentativasMaximas} tentativas, 1 min"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (!erro.isNullOrBlank()) {
            Text(erro, color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
        }

        BotaoCartoon(
            texto      = if (carregando) "Criando..." else "Criar Sala",
            onClick    = { onCriar(dificuldadeSelecionada) },
            habilitado = !carregando,
            tipo       = BotaoCartoonTipo.PRIMARIO,
            modifier   = Modifier.fillMaxWidth()
        )
    }
}

// ── TELA AGUARDAR ────────────────────────────────────────────────────────────

@Composable
private fun TelaAguardar(uiState: MultiplayerUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔑", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Código da Sala",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = uiState.codigoSala,
                    color = Color(0xFF6C63FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    letterSpacing = 8.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Compartilhe este código com seu amigo",
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = Color(0xFF6C63FF),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Aguardando oponente entrar...",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ── TELA ENTRAR ──────────────────────────────────────────────────────────────

@Composable
private fun TelaEntrar(
    carregando: Boolean,
    erro: String?,
    onEntrar: (String) -> Unit,
    onVoltar: () -> Unit,
    onLimparErro: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text("Entrar na Sala", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(Modifier.height(32.dp))

        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Digite o código da sala",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = codigo,
                onValueChange = {
                    if (it.length <= 6) {
                        codigo = it.uppercase()
                        onLimparErro()
                    }
                },
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("Código de 6 dígitos", color = Color.White.copy(alpha = 0.7f)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType    = KeyboardType.Text,
                    capitalization  = KeyboardCapitalization.Characters
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor    = Color.White,
                    unfocusedTextColor  = Color.White,
                    focusedBorderColor  = Color(0xFF6C63FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor         = Color(0xFF6C63FF)
                ),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight   = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color        = Color.White
                )
            )
            if (!erro.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(erro, color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
            }
        }

        BotaoCartoon(
            texto      = if (carregando) "Entrando..." else "Entrar",
            onClick    = { if (codigo.length == 6) onEntrar(codigo) },
            habilitado = !carregando && codigo.length == 6,
            tipo       = BotaoCartoonTipo.PRIMARIO,
            modifier   = Modifier.fillMaxWidth()
        )
    }
}

// ── TELA JOGAR ───────────────────────────────────────────────────────────────

@Composable
private fun TelaJogar(
    uiState: MultiplayerUiState,
    onLetra: (Char) -> Unit
) {
    val maxTentativas = uiState.dificuldade.tentativasMaximas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        CardCartoon(modifier = Modifier.fillMaxWidth(), padding = 10.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.tema,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (uiState.dificuldade) {
                        Dificuldade.FACIL   -> "Fácil"
                        Dificuldade.NORMAL  -> "Normal"
                        Dificuldade.DIFICIL -> "Difícil"
                    },
                    color = Color(0xFF6C63FF),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Placar lado a lado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Eu
            CardCartoon(
                modifier = Modifier.weight(1f),
                corBorda = Color(0xFF2196F3),
                padding  = 10.dp
            ) {
                Text(
                    text = if (uiState.jogadorNumero == 1) uiState.jogador1Nome else uiState.jogador2Nome,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = "Você",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                CoracoesRow(restantes = uiState.tentativasRestantes, maximas = maxTentativas)
            }
            // Oponente
            CardCartoon(
                modifier = Modifier.weight(1f),
                corBorda = Color(0xFFE53935),
                padding  = 10.dp
            ) {
                Text(
                    text = if (uiState.jogadorNumero == 1) uiState.jogador2Nome else uiState.jogador1Nome,
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = "Oponente",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                CoracoesRow(restantes = uiState.tentativasOponente, maximas = maxTentativas)
            }
        }

        // Avatar
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val estadoAvatar = runCatching {
                EstadoAvatar.valueOf(uiState.estadoAvatar)
            }.getOrDefault(EstadoAvatar.NEUTRO)
            Text(
                text = when (estadoAvatar) {
                    EstadoAvatar.NEUTRO  -> "🤔"
                    EstadoAvatar.ACERTOU -> "😄"
                    EstadoAvatar.ERROU   -> "😬"
                    EstadoAvatar.VITORIA -> "🏆"
                    EstadoAvatar.DERROTA -> "😢"
                },
                fontSize = 64.sp
            )
        }

        // Meu progresso
        CardCartoon(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
            if (uiState.progresso.isNotBlank()) {
                ProgressoPalavra(progresso = uiState.progresso)
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6C63FF), modifier = Modifier.size(24.dp))
                }
            }
        }

        // Progresso do oponente (menor)
        if (uiState.progressoOponente.isNotBlank()) {
            CardCartoon(
                modifier = Modifier.fillMaxWidth(),
                corBorda = Color(0xFFE53935).copy(alpha = 0.5f),
                padding  = 8.dp
            ) {
                Text(
                    text = "Oponente:",
                    color = Color(0xFFE53935).copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    uiState.progressoOponente.split(" ").forEach { c ->
                        Text(
                            text = c,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp),
                            color = if (c == "_") Color.White.copy(alpha = 0.3f) else Color.White
                        )
                    }
                }
            }
        }

        // Aguardando resultado (se eu terminei mas jogo ainda não finalizou)
        if (uiState.terminei) {
            CardCartoon(modifier = Modifier.fillMaxWidth(), corBorda = Color(0xFFFF9800)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.venci) "🎉 Você revelou a palavra!" else "💀 Sem tentativas!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Aguardando resultado final...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Teclado
        TecladoLetras(
            letrasCorretas = uiState.letrasCorretas,
            letrasErradas  = uiState.letrasErradas,
            onLetraClick   = { if (!uiState.terminei) onLetra(it) },
            modifier       = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CoracoesRow(restantes: Int, maximas: Int) {
    val usados = maximas - restantes
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(restantes.coerceAtLeast(0)) {
            Text("❤️", fontSize = 14.sp)
        }
        repeat(usados.coerceAtLeast(0)) {
            Text("🖤", fontSize = 14.sp)
        }
    }
}

// ── TELA RESULTADO ───────────────────────────────────────────────────────────

@Composable
private fun TelaResultado(
    uiState: MultiplayerUiState,
    onJogarNovamente: () -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.euVenci) "🏆" else "😢",
                    fontSize = 80.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (uiState.euVenci) "Você venceu!" else "Você perdeu!",
                    color = if (uiState.euVenci) Color(0xFF4CAF50) else Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "A palavra era:",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = uiState.palavraFinal,
                    color = Color(0xFF6C63FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.height(24.dp))
                BotaoCartoon(
                    texto    = "Jogar Novamente",
                    onClick  = onJogarNovamente,
                    tipo     = BotaoCartoonTipo.PRIMARIO,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                BotaoCartoon(
                    texto    = "Voltar ao Menu",
                    onClick  = onVoltar,
                    tipo     = BotaoCartoonTipo.SECUNDARIO,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}