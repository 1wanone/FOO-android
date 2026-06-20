package playfoo.com.ui.multiplayer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import playfoo.com.data.TemaDataSource
import playfoo.com.domain.Dificuldade
import playfoo.com.ui.components.*
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
                    onLetra = viewModel::tentarLetra
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
private fun TelaAguardar(uiState: MultiplayerUiState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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
    val maxTentativas      = uiState.dificuldade.tentativasMaximas
    val todasLetrasErradas = uiState.letrasErradasEu + uiState.letrasErradasOponente

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header: tema + dificuldade
        CardCartoon(modifier = Modifier.fillMaxWidth(), padding = 10.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = uiState.tema,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (uiState.dificuldade) {
                        Dificuldade.FACIL   -> "Fácil"
                        Dificuldade.NORMAL  -> "Normal"
                        Dificuldade.DIFICIL -> "Difícil"
                    },
                    color      = Rosa,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Indicador de turno
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (uiState.meuTurno) {
                Text("Seu turno!", color = Ciano, fontWeight = FontWeight.Bold)
            } else {
                Text("Vez do oponente...", color = Color.White.copy(alpha = 0.6f))
            }
        }

        // Timer visual
        if (uiState.meuTurno && uiState.timerAtivo) {
            val corTimer = when {
                uiState.timerSegundos <= 5  -> ErroVermelho
                uiState.timerSegundos <= 10 -> Color(0xFFFF9800)
                else                        -> Ciano
            }
            CardCartoon(modifier = Modifier.fillMaxWidth(), padding = 8.dp) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    FooIcone(FooIcones.Tempo, cor = corTimer, tamanho = 16.dp)
                    Text(
                        " ${uiState.timerSegundos}s",
                        color      = corTimer,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp
                    )
                }
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
                    modifier   = Modifier.fillMaxWidth().height(6.dp),
                    color      = corTimer,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }

        // Placar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardCartoon(modifier = Modifier.weight(1f), corBorda = AzulCinza, padding = 10.dp) {
                Text(
                    text      = if (uiState.jogadorNumero == 1) uiState.jogador1Nome else uiState.jogador2Nome,
                    color     = AzulCinza,
                    fontWeight = FontWeight.Bold,
                    fontSize  = 13.sp,
                    maxLines  = 1
                )
                Text("Você", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                CoracoesRow(restantes = uiState.tentativasEu, maximas = maxTentativas)
            }
            CardCartoon(modifier = Modifier.weight(1f), corBorda = ErroVermelho, padding = 10.dp) {
                Text(
                    text      = if (uiState.jogadorNumero == 1) uiState.jogador2Nome else uiState.jogador1Nome,
                    color     = ErroVermelho,
                    fontWeight = FontWeight.Bold,
                    fontSize  = 13.sp,
                    maxLines  = 1
                )
                Text("Oponente", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                CoracoesRow(restantes = uiState.tentativasOponente, maximas = maxTentativas)
            }
        }

        // Palavra compartilhada
        CardCartoon(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
            if (uiState.progresso.isNotBlank()) {
                ProgressoPalavra(progresso = uiState.progresso)
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Rosa, modifier = Modifier.size(24.dp))
                }
            }
        }

        // Sem tentativas
        if (uiState.terminei) {
            CardCartoon(modifier = Modifier.fillMaxWidth(), corBorda = Color(0xFFFF9800)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FooIcone(FooIcones.Caveira, cor = Color(0xFFFF9800), tamanho = 18.dp)
                        Text(
                            text       = "Sem tentativas!",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = "Aguardando resultado final...",
                        color    = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        TecladoLetras(
            letrasCorretas = uiState.letrasReveladas,
            letrasErradas  = todasLetrasErradas,
            onLetraClick   = { if (uiState.meuTurno) onLetra(it) },
            habilitado     = uiState.meuTurno && !uiState.terminei,
            modifier       = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CoracoesRow(restantes: Int, maximas: Int) {
    val usados = maximas - restantes
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(restantes.coerceAtLeast(0)) {
            Icon(Icons.Rounded.Favorite, null, tint = Rosa, modifier = Modifier.size(14.dp))
        }
        repeat(usados.coerceAtLeast(0)) {
            Icon(Icons.Rounded.FavoriteBorder, null, tint = AzulCinza.copy(0.4f), modifier = Modifier.size(14.dp))
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
            icone   = if (euVenci) FooIcones.Trofeu else FooIcones.Derrota,
            cor     = if (euVenci) Color(0xFFFFD700) else ErroVermelho,
            tamanho = 80.dp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text       = if (euVenci) "Você venceu!" else "Você perdeu!",
            style      = MaterialTheme.typography.headlineLarge,
            color      = if (euVenci) Color(0xFFFFD700) else ErroVermelho,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text("A palavra era:", color = Color.White.copy(alpha = 0.7f))
        Text(
            text          = palavraFinal,
            color         = Color.White,
            fontWeight    = FontWeight.Bold,
            fontSize      = 24.sp,
            letterSpacing = 4.sp
        )
        Text(
            text  = "Tema: $tema",
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
                BotaoCartoon(
                    texto    = "Mesmo tema — $tema",
                    icone    = FooIcones.Jogar,
                    onClick  = onJogarMesmoTema,
                    tipo     = BotaoCartoonTipo.PRIMARIO,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoCartoon(
                    texto    = "Escolher outro tema",
                    icone    = FooIcones.Multi,
                    onClick  = onEscolherTema,
                    tipo     = BotaoCartoonTipo.SECUNDARIO,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick  = onVoltar,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                    border   = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Text("Sair do multiplayer")
                }
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