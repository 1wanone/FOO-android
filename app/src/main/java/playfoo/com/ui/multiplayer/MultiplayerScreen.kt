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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.data.TemaDataSource
import playfoo.com.domain.Dificuldade
import playfoo.com.ui.components.*
import playfoo.com.ui.game.GameBackgroundMultiplayer
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.game.components.ProgressoPalavra
import playfoo.com.ui.game.components.TecladoLetras
import playfoo.com.ui.theme.*
import playfoo.com.viewmodel.MultiplayerUiState
import playfoo.com.viewmodel.MultiplayerViewModel
import playfoo.com.viewmodel.TelaMultiplayer

@Composable
fun MultiplayerScreen(
    onVoltar: () -> Unit = {},
    viewModel: MultiplayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                TelaMultiplayer.AGUARDAR -> TelaAguardar(
                    uiState   = uiState,
                    onCancelar = {
                        viewModel.cancelarSala()
                        onVoltar()
                    }
                )
                TelaMultiplayer.ENTRAR   -> TelaEntrar(
                    carregando   = uiState.carregando,
                    erro         = uiState.erro,
                    onEntrar     = viewModel::entrarNaSala,
                    onVoltar     = { viewModel.irPara(TelaMultiplayer.INICIAL) },
                    onLimparErro = viewModel::limparErro
                )
                TelaMultiplayer.JOGAR    -> TelaJogar(
                    uiState = uiState,
                    onLetra = viewModel::tentarLetra
                )
                TelaMultiplayer.RESULTADO -> TelaResultado(
                    uiState           = uiState,
                    onAceitarRevanche = { viewModel.aceitarRevanche() },
                    onEscolherTema    = { viewModel.irPara(TelaMultiplayer.ESCOLHER_TEMA) },
                    onVoltar          = { viewModel.reiniciar(); onVoltar() }
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
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Multiplayer", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            FooIcone(FooIcones.Multi, cor = Rosa, tamanho = 80.dp)

            Text(
                text       = "Desafie um amigo!",
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 22.sp,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = "Ambos jogam a MESMA palavra em turnos.\nQuem revelar a palavra completa primeiro vence!",
                color     = Color.White.copy(alpha = 0.7f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            BotaoCartoon(
                texto    = "Criar Sala",
                icone    = FooIcones.Adicionar,
                onClick  = onCriar,
                tipo     = BotaoCartoonTipo.PRIMARIO,
                modifier = Modifier.fillMaxWidth()
            )
            BotaoCartoon(
                texto    = "Entrar com Código",
                icone    = FooIcones.AdicionarPessoa,
                onClick  = onEntrar,
                tipo     = BotaoCartoonTipo.SECUNDARIO,
                modifier = Modifier.fillMaxWidth()
            )
        }
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

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Criar Sala", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text  = "Escolha a dificuldade",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                Dificuldade.entries.forEach { dif ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = dificuldadeSelecionada == dif,
                            onClick  = { dificuldadeSelecionada = dif },
                            colors   = RadioButtonDefaults.colors(selectedColor = Rosa)
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
                Text(erro, color = ErroVermelho, style = MaterialTheme.typography.bodySmall)
            }

            BotaoCartoon(
                texto      = if (carregando) "Criando..." else "Criar Sala",
                icone      = FooIcones.Multi,
                onClick    = { onCriar(dificuldadeSelecionada) },
                habilitado = !carregando,
                tipo       = BotaoCartoonTipo.PRIMARIO,
                modifier   = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── TELA AGUARDAR ────────────────────────────────────────────────────────────

@Composable
private fun TelaAguardar(
    uiState: MultiplayerUiState,
    onCancelar: () -> Unit
) {
    var mostrarConfirmacaoSair by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo(titulo = "", onVoltar = { mostrarConfirmacaoSair = true })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    FooIcone(FooIcones.Multi, cor = Ciano, tamanho = 40.dp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Código da Sala",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text          = uiState.codigoSala,
                        color         = Ciano,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 48.sp,
                        letterSpacing = 8.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = "Compartilhe este código com seu amigo",
                        color     = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(24.dp))
                    CircularProgressIndicator(color = Ciano, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Aguardando oponente entrar...",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

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
                FooIcone(FooIcones.Aviso, cor = ErroVermelho, tamanho = 48.dp)
                Text(
                    "Cancelar partida?",
                    color = ErroVermelho,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    "A sala será encerrada e o oponente não poderá entrar.",
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotaoCartoon(
                        texto    = "Continuar",
                        onClick  = { mostrarConfirmacaoSair = false },
                        tipo     = BotaoCartoonTipo.PRIMARIO,
                        modifier = Modifier.weight(1f)
                    )
                    BotaoCartoon(
                        texto    = "Sair",
                        onClick  = {
                            mostrarConfirmacaoSair = false
                            onCancelar()
                        },
                        tipo     = BotaoCartoonTipo.PERIGO,
                        modifier = Modifier.weight(1f)
                    )
                }
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

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Entrar na Sala", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text       = "Digite o código da sala",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value         = codigo,
                    onValueChange = {
                        if (it.length <= 6) { codigo = it.uppercase(); onLimparErro() }
                    },
                    modifier  = Modifier.fillMaxWidth(),
                    label     = { Text("Código de 6 dígitos", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType   = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        focusedBorderColor   = Rosa,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor          = Rosa
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color         = Color.White
                    )
                )
                if (!erro.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(erro, color = ErroVermelho, style = MaterialTheme.typography.bodySmall)
                }
            }

            BotaoCartoon(
                texto      = if (carregando) "Entrando..." else "Entrar",
                icone      = FooIcones.AdicionarPessoa,
                onClick    = { if (codigo.length == 6) onEntrar(codigo) },
                habilitado = !carregando && codigo.length == 6,
                tipo       = BotaoCartoonTipo.PRIMARIO,
                modifier   = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── TELA JOGAR ───────────────────────────────────────────────────────────────

@Composable
private fun TelaJogar(
    uiState: MultiplayerUiState,
    onLetra: (Char) -> Unit
) {
    val maxSegundos = when (uiState.dificuldade) {
        Dificuldade.FACIL   -> 30f
        Dificuldade.NORMAL  -> 20f
        Dificuldade.DIFICIL -> 10f
    }
    val nomeLocal  = if (uiState.jogadorNumero == 1) uiState.jogador1Nome else uiState.jogador2Nome
    val nomeRemoto = if (uiState.jogadorNumero == 1) uiState.jogador2Nome else uiState.jogador1Nome
    val todasLetrasErradas = uiState.letrasErradasEu + uiState.letrasErradasOponente

    val estadoAvatarLocal = when (uiState.estadoAvatarLocal) {
        "ACERTOU" -> EstadoAvatar.ACERTOU
        "ERROU"   -> EstadoAvatar.ERROU
        "VITORIA" -> EstadoAvatar.VITORIA
        "DERROTA" -> EstadoAvatar.DERROTA
        else      -> EstadoAvatar.NEUTRO
    }
    val estadoAvatarRemoto = when (uiState.estadoAvatarRemoto) {
        "ACERTOU" -> EstadoAvatar.ACERTOU
        "ERROU"   -> EstadoAvatar.ERROU
        "VITORIA" -> EstadoAvatar.VITORIA
        "DERROTA" -> EstadoAvatar.DERROTA
        else      -> EstadoAvatar.NEUTRO
    }

    val difLabel = when (uiState.dificuldade) {
        Dificuldade.FACIL   -> "Fácil"
        Dificuldade.NORMAL  -> "Normal"
        Dificuldade.DIFICIL -> "Difícil"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo(titulo = "${uiState.tema} — $difLabel")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GameBackgroundMultiplayer(
                avatarConfigLocal  = uiState.avatarConfigLocal,
                estadoLocal        = estadoAvatarLocal,
                avatarConfigRemoto = uiState.avatarConfigRemoto,
                estadoRemoto       = estadoAvatarRemoto,
                modifier           = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Timer
                if (uiState.timerAtivo) {
                    val corTimer = when {
                        uiState.timerSegundos <= 5  -> ErroVermelho
                        uiState.timerSegundos <= 10 -> Color(0xFFFF9800)
                        else                        -> Ciano
                    }
                    LinearProgressIndicator(
                        progress = { uiState.timerSegundos / maxSegundos },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color      = corTimer,
                        trackColor = RoxoMedio
                    )
                    Spacer(Modifier.height(6.dp))
                }

                // Cards dos dois jogadores lado a lado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardJogador(
                        nome          = nomeLocal,
                        progresso     = uiState.progresso,
                        tentativas    = uiState.tentativasEu,
                        maxTentativas = uiState.dificuldade.tentativasMaximas,
                        ehVez         = uiState.meuTurno,
                        modifier      = Modifier.weight(1f)
                    )
                    CardJogador(
                        nome          = nomeRemoto,
                        progresso     = uiState.progresso,
                        tentativas    = uiState.tentativasOponente,
                        maxTentativas = uiState.dificuldade.tentativasMaximas,
                        ehVez         = !uiState.meuTurno,
                        modifier      = Modifier.weight(1f)
                    )
                }

                // Palavra em progresso
                if (uiState.progresso.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    CardCartoon(modifier = Modifier.fillMaxWidth(), padding = 10.dp) {
                        ProgressoPalavra(progresso = uiState.progresso)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FundoTeclado)
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            TecladoLetras(
                letrasCorretas = uiState.letrasReveladas,
                letrasErradas  = todasLetrasErradas,
                onLetraClick   = { if (uiState.meuTurno) onLetra(it) },
                habilitado     = uiState.meuTurno && !uiState.terminei,
                modifier       = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CardJogador(
    nome: String,
    progresso: String,
    tentativas: Int,
    maxTentativas: Int,
    ehVez: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (ehVez) Rosa else RoxoMedio
    val nomeColor   = if (ehVez) Rosa else TextoSecundario

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(FundoCard)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (ehVez) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Rosa, CircleShape)
                )
            }
            Text(
                text       = nome,
                color      = nomeColor,
                fontWeight = if (ehVez) FontWeight.Bold else FontWeight.Normal,
                fontSize   = 12.sp,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
        Text(
            text       = if (ehVez) "Seu turno!" else "Oponente",
            color      = if (ehVez) Ciano else Color.Transparent,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(maxTentativas) { i ->
                Icon(
                    imageVector = if (i < tentativas) Icons.Rounded.Favorite
                                  else Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint     = if (i < tentativas) Rosa else RoxoMedio.copy(alpha = 0.5f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

// ── TELA RESULTADO ───────────────────────────────────────────────────────────

@Composable
private fun TelaResultado(
    uiState: MultiplayerUiState,
    onAceitarRevanche: () -> Unit,
    onEscolherTema: () -> Unit,
    onVoltar: () -> Unit
) {
    val souJogador1   = uiState.jogadorNumero == 1
    val meuAceite     = if (souJogador1) uiState.aceiteRevanche1 else uiState.aceiteRevanche2
    val oponenteAceitou = if (souJogador1) uiState.aceiteRevanche2 else uiState.aceiteRevanche1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FooIcone(
            icone   = if (uiState.euVenci) FooIcones.Trofeu else FooIcones.Derrota,
            cor     = if (uiState.euVenci) Color(0xFFFFD700) else ErroVermelho,
            tamanho = 80.dp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text       = if (uiState.euVenci) "Você venceu!" else "Você perdeu!",
            style      = MaterialTheme.typography.headlineLarge,
            color      = if (uiState.euVenci) Color(0xFFFFD700) else ErroVermelho,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text("A palavra era:", color = Color.White.copy(alpha = 0.7f))
        Text(
            text          = uiState.palavraFinal,
            color         = Color.White,
            fontWeight    = FontWeight.Bold,
            fontSize      = 24.sp,
            letterSpacing = 4.sp
        )
        Text(
            text  = "Tema: ${uiState.tema}",
            color = Rosa,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(32.dp))

        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Jogar novamente?",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )

                if (!meuAceite) {
                    BotaoCartoon(
                        texto    = "Revanche! Mesmo tema",
                        icone    = FooIcones.Jogar,
                        onClick  = onAceitarRevanche,
                        tipo     = BotaoCartoonTipo.PRIMARIO,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotaoCartoon(
                        texto    = "Escolher outro tema",
                        icone    = FooIcones.Turmas,
                        onClick  = onEscolherTema,
                        tipo     = BotaoCartoonTipo.SECUNDARIO,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CardCartoon(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color    = Ciano,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (oponenteAceitou) "Ambos aceitaram! Iniciando..."
                                       else "Aguardando oponente aceitar a revanche...",
                                color     = TextoSecundario,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

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

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Escolher Tema", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            temas.forEach { tema ->
                CardCartoon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTemaEscolhido(tema.id) },
                    corBorda = Rosa,
                    padding  = 16.dp
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tema.nome, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${tema.palavras.size} palavras", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                        FooIcone(FooIcones.Jogar, cor = Rosa, tamanho = 20.dp)
                    }
                }
            }
        }
    }
}