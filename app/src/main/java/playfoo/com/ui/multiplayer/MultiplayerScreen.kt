package playfoo.com.ui.multiplayer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.data.TemaDataSource
import playfoo.com.domain.Dificuldade
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.CardCartoon
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
import playfoo.com.ui.game.AudioManager
import playfoo.com.ui.game.LocalAudioManager
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

    val audio = LocalAudioManager.current

    BackHandler(
        enabled = uiState.tela == TelaMultiplayer.CRIAR ||
                uiState.tela == TelaMultiplayer.ENTRAR ||
                uiState.tela == TelaMultiplayer.ESCOLHER_TEMA
    ) {
        viewModel.irPara(
            when (uiState.tela) {
                TelaMultiplayer.ESCOLHER_TEMA -> TelaMultiplayer.RESULTADO
                else -> TelaMultiplayer.INICIAL
            }
        )
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
                    carregando   = uiState.carregando,
                    erro         = uiState.erro,
                    onEntrar     = viewModel::entrarNaSala,
                    onVoltar     = { viewModel.irPara(TelaMultiplayer.INICIAL) },
                    onLimparErro = viewModel::limparErro
                )
                TelaMultiplayer.JOGAR    -> TelaJogar(
                    uiState = uiState,
                    onLetra = viewModel::tentarLetra,
                    audio   = audio
                )
                TelaMultiplayer.RESULTADO -> TelaResultado(
                    euVenci          = uiState.euVenci,
                    palavraFinal     = uiState.palavraFinal,
                    tema             = uiState.tema,
                    onJogarMesmoTema = { viewModel.jogarNovamenteMesmoTema() },
                    onEscolherTema   = { viewModel.irPara(TelaMultiplayer.ESCOLHER_TEMA) },
                    onVoltar         = { viewModel.reiniciar(); onVoltar() }
                )
                TelaMultiplayer.ESCOLHER_TEMA -> TelaEscolherTema(
                    onTemaEscolhido = { temaId -> viewModel.escolherTema(temaId) },
                    onVoltar        = { viewModel.irPara(TelaMultiplayer.RESULTADO) }
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
            text = "Ambos jogam a MESMA palavra em turnos.\nQuem revelar a palavra completa primeiro vence!",
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
                    Text(
                        text = when (dif) {
                            Dificuldade.FACIL   -> "Fácil  — ${dif.tentativasMaximas} tentativas, 30s/turno"
                            Dificuldade.NORMAL  -> "Normal — ${dif.tentativasMaximas} tentativas, 20s/turno"
                            Dificuldade.DIFICIL -> "Difícil — ${dif.tentativasMaximas} tentativas, 10s/turno"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                    if (it.length <= 6) { codigo = it.uppercase(); onLimparErro() }
                },
                modifier      = Modifier.fillMaxWidth(),
                label         = { Text("Código de 6 dígitos", color = Color.White.copy(alpha = 0.7f)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType   = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor     = Color.White,
                    unfocusedTextColor   = Color.White,
                    focusedBorderColor   = Color(0xFF6C63FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor          = Color(0xFF6C63FF)
                ),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color         = Color.White
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
    onLetra: (Char) -> Unit,
    audio: AudioManager?
) {
    val maxTentativas = uiState.dificuldade.tentativasMaximas

    val estadoLocal = when (uiState.estadoAvatarLocal) {
        "ACERTOU" -> playfoo.com.ui.game.components.EstadoAvatar.ACERTOU
        "ERROU"   -> playfoo.com.ui.game.components.EstadoAvatar.ERROU
        "VITORIA" -> playfoo.com.ui.game.components.EstadoAvatar.VITORIA
        "DERROTA" -> playfoo.com.ui.game.components.EstadoAvatar.DERROTA
        else      -> playfoo.com.ui.game.components.EstadoAvatar.NEUTRO
    }
    val estadoRemoto = when (uiState.estadoAvatarRemoto) {
        "ACERTOU" -> playfoo.com.ui.game.components.EstadoAvatar.ACERTOU
        "ERROU"   -> playfoo.com.ui.game.components.EstadoAvatar.ERROU
        "VITORIA" -> playfoo.com.ui.game.components.EstadoAvatar.VITORIA
        "DERROTA" -> playfoo.com.ui.game.components.EstadoAvatar.DERROTA
        else      -> playfoo.com.ui.game.components.EstadoAvatar.NEUTRO
    }

    val nomeLocal  = if (uiState.jogadorNumero == 1) uiState.jogador1Nome else uiState.jogador2Nome
    val nomeRemoto = if (uiState.jogadorNumero == 1) uiState.jogador2Nome else uiState.jogador1Nome
    val todasLetrasErradas = uiState.letrasErradasEu + uiState.letrasErradasOponente

    LaunchedEffect(uiState.estadoAvatarLocal) {
        when (uiState.estadoAvatarLocal) {
            "ACERTOU" -> audio?.playCorrect()
            "ERROU"   -> audio?.playErro()
            "VITORIA" -> audio?.playVictory()
            "DERROTA" -> audio?.playDerrota()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Header: tema + dificuldade + timer
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(playfoo.com.ui.theme.RoxoEscuro.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            androidx.compose.foundation.layout.Column {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.tema} — ${when (uiState.dificuldade) {
                            Dificuldade.FACIL   -> "Fácil"
                            Dificuldade.NORMAL  -> "Normal"
                            Dificuldade.DIFICIL -> "Difícil"
                        }}",
                        color = playfoo.com.ui.theme.Rosa,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (uiState.meuTurno && uiState.timerAtivo) {
                        val corTimer = when {
                            uiState.timerSegundos <= 5  -> playfoo.com.ui.theme.ErroVermelho
                            uiState.timerSegundos <= 10 -> Color(0xFFFF9800)
                            else -> playfoo.com.ui.theme.Ciano
                        }
                        Text(
                            text = "⏱ ${uiState.timerSegundos}s",
                            color = corTimer,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
                if (uiState.meuTurno && uiState.timerAtivo) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = {
                            val max = when (uiState.dificuldade) {
                                Dificuldade.FACIL   -> 30f
                                Dificuldade.NORMAL  -> 20f
                                Dificuldade.DIFICIL -> 10f
                            }
                            uiState.timerSegundos / max
                        },
                        modifier   = Modifier.fillMaxWidth().height(4.dp),
                        color      = playfoo.com.ui.theme.Ciano,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Cards dos jogadores lado a lado — tamanho FIXO, só cor muda
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardJogadorMulti(
                nome          = nomeLocal,
                tentativas    = uiState.tentativasEu,
                maxTentativas = maxTentativas,
                ehVez         = uiState.meuTurno,
                modifier      = Modifier.weight(1f)
            )
            CardJogadorMulti(
                nome          = nomeRemoto,
                tentativas    = uiState.tentativasOponente,
                maxTentativas = maxTentativas,
                ehVez         = !uiState.meuTurno,
                modifier      = Modifier.weight(1f)
            )
        }

        // Área do cenário com dois avatares — ocupa todo espaço restante
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            playfoo.com.ui.game.GameBackgroundMultiplayer(
                avatarConfigLocal  = uiState.avatarConfigLocal,
                estadoLocal        = estadoLocal,
                avatarConfigRemoto = uiState.avatarConfigRemoto,
                estadoRemoto       = estadoRemoto,
                modifier           = Modifier.fillMaxSize()
            )

            // Progresso da palavra centralizado no topo do cenário
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(playfoo.com.ui.theme.FundoCard)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayProgresso = uiState.progresso.ifBlank {
                    uiState.palavra.map { '_' }.joinToString(" ")
                }
                ProgressoPalavra(progresso = displayProgresso)
                if (uiState.terminei) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Aguardando resultado...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Teclado no rodapé
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(playfoo.com.ui.theme.FundoTeclado)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            TecladoLetras(
                letrasCorretas = uiState.letrasReveladas,
                letrasErradas  = todasLetrasErradas,
                onLetraClick   = { if (uiState.meuTurno) { audio?.playClick(); onLetra(it) } },
                habilitado     = uiState.meuTurno && !uiState.terminei,
                modifier       = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CardJogadorMulti(
    nome: String,
    tentativas: Int,
    maxTentativas: Int,
    ehVez: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (ehVez) playfoo.com.ui.theme.Rosa else playfoo.com.ui.theme.RoxoMedio
    val nomeColor   = if (ehVez) playfoo.com.ui.theme.Rosa else Color.White.copy(alpha = 0.7f)

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .background(playfoo.com.ui.theme.FundoCard)
            .border(2.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (ehVez) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            playfoo.com.ui.theme.Rosa,
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            Text(
                text = nome,
                color = nomeColor,
                fontWeight = if (ehVez) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Text(
            text = if (ehVez) "Seu turno!" else " ",
            color = playfoo.com.ui.theme.Ciano,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(maxTentativas) { i ->
                Icon(
                    imageVector = if (i < tentativas)
                        Icons.Rounded.Favorite
                    else
                        Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = if (i < tentativas) playfoo.com.ui.theme.Rosa
                    else playfoo.com.ui.theme.RoxoMedio.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ── TELA RESULTADO ───────────────────────────────────────────────────────────

@Composable
private fun TelaResultado(
    euVenci: Boolean,
    palavraFinal: String,
    tema: String,
    onJogarMesmoTema: () -> Unit,
    onEscolherTema: () -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FooIcone(
            icone   = if (euVenci) FooIcones.Trofeu else FooIcones.Turmas,
            cor     = if (euVenci) Color(0xFFFFD700) else Color(0xFFE53935),
            tamanho = 72.dp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (euVenci) "Você venceu!" else "Você perdeu!",
            style = MaterialTheme.typography.headlineLarge,
            color = if (euVenci) Color(0xFFFFD700) else Color(0xFFE53935),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text("A palavra era:", color = Color.White.copy(alpha = 0.7f))
        Text(
            text = palavraFinal,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = 4.sp
        )
        Text(
            text = "Tema: $tema",
            color = Color(0xFF6C63FF),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(32.dp))

        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Jogar novamente?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                BotaoCartoon(
                    texto    = "Mesmo tema",
                    onClick  = onJogarMesmoTema,
                    tipo     = BotaoCartoonTipo.PRIMARIO,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoCartoon(
                    texto    = "Escolher outro tema",
                    onClick  = onEscolherTema,
                    tipo     = BotaoCartoonTipo.SECUNDARIO,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoCartoon(
                    texto    = "Sair do multiplayer",
                    onClick  = onVoltar,
                    tipo     = BotaoCartoonTipo.NEUTRO,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── TELA ESCOLHER TEMA ────────────────────────────────────────────────────────

@Composable
private fun TelaEscolherTema(
    onTemaEscolhido: (temaId: Int) -> Unit,
    onVoltar: () -> Unit
) {
    val temas = TemaDataSource.temas
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                "Escolher Tema",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        temas.forEach { tema ->
            CardCartoon(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTemaEscolhido(tema.id) },
                corBorda = Color(0xFF6C63FF),
                padding  = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            tema.nome,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "${tema.palavras.size} palavras",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    Text("→", color = Color(0xFF6C63FF), fontSize = 20.sp)
                }
            }
        }
    }
}