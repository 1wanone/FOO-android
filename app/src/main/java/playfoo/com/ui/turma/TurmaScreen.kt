package playfoo.com.ui.turma

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.domain.TipoUsuario
import playfoo.com.ui.components.*
import playfoo.com.ui.theme.*
import playfoo.com.domain.RankingJogador
import playfoo.com.viewmodel.TelaTurma
import playfoo.com.viewmodel.TurmaUiState
import com.google.firebase.auth.FirebaseAuth
import playfoo.com.viewmodel.TurmaViewModel

@Composable
fun TurmaScreen(
    onVoltar: () -> Unit = {},
    onNavDashboard: (turmaId: String) -> Unit = {},
    viewModel: TurmaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConvidado = remember { FirebaseAuth.getInstance().currentUser == null }

    BackHandler(enabled = uiState.telaTurma != TelaTurma.INICIAL) {
        viewModel.irPara(TelaTurma.INICIAL)
    }

    FundoTela(tipo = TipoFundo.TURMA) {
        if (isConvidado) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderFoo(titulo = "Turmas", onVoltar = onVoltar)
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CardCartoon(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FooIcone(FooIcones.Turmas, cor = AzulCinza, tamanho = 52.dp)
                            Text(
                                text = "Recurso exclusivo para usuários cadastrados",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Crie uma conta para entrar em turmas, salvar seu progresso e ver suas estatísticas.",
                                color = Color.White.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(4.dp))
                            BotaoCartoon(
                                texto   = "Voltar",
                                onClick = onVoltar,
                                tipo    = BotaoCartoonTipo.NEUTRO,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            return@FundoTela
        }
        AnimatedContent(
            targetState = uiState.telaTurma,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "turma_transition"
        ) { tela ->
            when (tela) {
                TelaTurma.INICIAL -> TelaInicialTurma(
                    uiState        = uiState,
                    onVoltar       = onVoltar,
                    onCriar        = { viewModel.irPara(TelaTurma.CRIAR) },
                    onEntrar       = { viewModel.irPara(TelaTurma.ENTRAR) },
                    onSairDaTurma  = { viewModel.sairDaTurma() },
                    onNavDashboard = onNavDashboard,
                    onLimpar       = { viewModel.limparMensagens() },
                    onRemoverAluno = { turmaId, alunoId -> viewModel.removerAluno(turmaId, alunoId) }
                )
                TelaTurma.CRIAR -> TelaCriarTurma(
                    uiState  = uiState,
                    onVoltar = { viewModel.irPara(TelaTurma.INICIAL) },
                    onCriar  = { nome -> viewModel.criarTurma(nome) }
                )
                TelaTurma.ENTRAR -> TelaEntrarTurma(
                    uiState  = uiState,
                    onVoltar = { viewModel.irPara(TelaTurma.INICIAL) },
                    onEntrar = { codigo -> viewModel.entrarNaTurma(codigo) }
                )
                TelaTurma.DETALHES -> TelaInicialTurma(
                    uiState        = uiState,
                    onVoltar       = { viewModel.irPara(TelaTurma.INICIAL) },
                    onCriar        = {},
                    onEntrar       = {},
                    onSairDaTurma  = { viewModel.sairDaTurma() },
                    onNavDashboard = onNavDashboard,
                    onLimpar       = { viewModel.limparMensagens() },
                    onRemoverAluno = { turmaId, alunoId -> viewModel.removerAluno(turmaId, alunoId) }
                )
            }
        }
    }
}

@Composable
private fun TelaInicialTurma(
    uiState: TurmaUiState,
    onVoltar: () -> Unit,
    onCriar: () -> Unit,
    onEntrar: () -> Unit,
    onSairDaTurma: () -> Unit,
    onNavDashboard: (String) -> Unit,
    onLimpar: () -> Unit,
    onRemoverAluno: (turmaId: String, alunoId: String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Turmas", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.sucesso?.let { msg ->
                CardCartoon(corBorda = Ciano) {
                    Text(
                        text      = msg,
                        color     = Ciano,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(4000)
                    onLimpar()
                }
            }

            uiState.erro?.let { erro ->
                CardCartoon(corBorda = ErroVermelho) {
                    Text(
                        text      = erro,
                        color     = ErroVermelho,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.tipoUsuario == TipoUsuario.GESTOR) {
                ConteudoGestor(
                    uiState        = uiState,
                    onCriar        = onCriar,
                    onNavDashboard = onNavDashboard,
                    onRemoverAluno = onRemoverAluno
                )
            } else {
                ConteudoAluno(
                    uiState       = uiState,
                    onEntrar      = onEntrar,
                    onSairDaTurma = onSairDaTurma
                )
            }
        }
    }
}

// ── GESTOR ───────────────────────────────────────────────────────────────────

@Composable
private fun ConteudoGestor(
    uiState: TurmaUiState,
    onCriar: () -> Unit,
    onNavDashboard: (String) -> Unit,
    onRemoverAluno: (turmaId: String, alunoId: String) -> Unit
) {
    val context = LocalContext.current

    if (uiState.turmasDoGestor.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FooIcone(FooIcones.Turmas, cor = AzulCinza, tamanho = 64.dp)
                Text(
                    text      = "Nenhuma turma criada",
                    color     = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize  = 18.sp
                )
                Text(
                    text      = "Crie sua primeira turma para começar",
                    color     = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        uiState.turmasDoGestor.forEach { turma ->
            val turmaId = turma["id"]?.toString() ?: ""
            val nome    = turma["nome"]?.toString() ?: "Turma"
            val codigo  = turma["codigo"]?.toString() ?: ""
            val membros = (turma["membros"] as? List<*>) ?: emptyList<Any>()

            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Info da turma
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nome, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                "${membros.size} aluno(s)",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                        IconButton(onClick = { onNavDashboard(turmaId) }) {
                            FooIcone(FooIcones.Grafico, cor = Rosa, tamanho = 24.dp)
                        }
                    }

                    // Código com copiar e compartilhar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoxoEscuro, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Código da turma", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text(codigo, color = Ciano, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 4.sp)
                        }
                        Row {
                            val clipboardManager = LocalClipboardManager.current
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(codigo)) }) {
                                FooIcone(FooIcones.Copiar, cor = AzulCinza, tamanho = 22.dp)
                            }
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, "Código da turma FOO: $codigo")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartilhar código"))
                            }) {
                                FooIcone(FooIcones.Compartilhar, cor = AzulCinza, tamanho = 22.dp)
                            }
                        }
                    }

                    // Lista de alunos
                    if (membros.isNotEmpty()) {
                        Text("Alunos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        membros.filterIsInstance<String>().forEach { alunoId ->
                            AlunoItem(
                                nome = uiState.nomes[alunoId] ?: alunoId.take(8),
                                onRemover = { onRemoverAluno(turmaId, alunoId) }
                            )
                        }
                    }

                    // Ranking da turma
                    val rankingTurma = uiState.rankingPorTurma[turmaId] ?: emptyList()
                    if (rankingTurma.isNotEmpty()) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FooIcone(FooIcones.Trofeu, cor = Color(0xFFFFD700), tamanho = 16.dp)
                            Text("Ranking", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        rankingTurma.take(5).forEachIndexed { index, jogador ->
                            RankingItem(posicao = index + 1, jogador = jogador)
                        }
                    }
                }
            }
        }
    }

    BotaoCartoon(
        texto    = "Criar nova turma",
        icone    = FooIcones.Adicionar,
        onClick  = onCriar,
        tipo     = BotaoCartoonTipo.SECUNDARIO,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AlunoItem(nome: String, onRemover: () -> Unit) {
    var confirmar by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FundoCard, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(RoxoMedio, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FooIcone(FooIcones.Perfil, cor = Color.White, tamanho = 18.dp)
            }
            Text(nome, color = Color.White, fontSize = 13.sp)
        }
        IconButton(onClick = { confirmar = true }) {
            FooIcone(FooIcones.RemoverPessoa, cor = ErroVermelho, tamanho = 20.dp)
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            containerColor   = FundoCard,
            title            = { Text("Remover aluno?", color = Color.White, fontWeight = FontWeight.Bold) },
            text             = { Text("Esta ação remove o aluno da turma.", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton    = {
                BotaoCartoon(
                    texto   = "Remover",
                    onClick = { onRemover(); confirmar = false },
                    tipo    = BotaoCartoonTipo.PERIGO
                )
            },
            dismissButton = {
                BotaoCartoon(
                    texto   = "Cancelar",
                    onClick = { confirmar = false },
                    tipo    = BotaoCartoonTipo.NEUTRO
                )
            }
        )
    }
}

// ── ALUNO ────────────────────────────────────────────────────────────────────

@Composable
private fun ConteudoAluno(
    uiState: TurmaUiState,
    onEntrar: () -> Unit,
    onSairDaTurma: () -> Unit
) {
    if (uiState.turmaAtual != null) {
        val turma  = uiState.turmaAtual
        val codigo = turma["codigo"]?.toString() ?: ""
        var showSairDialog by remember { mutableStateOf(false) }
        val clipboardManager = LocalClipboardManager.current

        // Seção 1: Info da turma
        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FooIcone(FooIcones.Turmas, cor = Rosa, tamanho = 22.dp)
                    Text(
                        text       = turma["nome"]?.toString() ?: "Turma",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoxoEscuro, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Código da turma", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text(codigo, color = Ciano, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 4.sp)
                    }
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(codigo)) }) {
                        FooIcone(FooIcones.Copiar, cor = AzulCinza, tamanho = 22.dp)
                    }
                }

                if (uiState.nomeProfessor.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FooIcone(FooIcones.Perfil, cor = AzulCinza, tamanho = 14.dp)
                        Text("Professor: ${uiState.nomeProfessor}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }
        }

        // Seção 2: Ranking
        if (uiState.ranking.isNotEmpty()) {
            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FooIcone(FooIcones.Trofeu, cor = Color(0xFFFFD700), tamanho = 20.dp)
                        Text("Ranking da Turma", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    uiState.ranking.take(5).forEachIndexed { index, jogador ->
                        RankingItem(posicao = index + 1, jogador = jogador)
                    }
                }
            }
        }

        // Seção 3: Ação
        if (uiState.carregando) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ErroVermelho)
            }
        } else {
            BotaoCartoon(
                texto    = "Sair da Turma",
                icone    = FooIcones.Sair,
                onClick  = { showSairDialog = true },
                tipo     = BotaoCartoonTipo.PERIGO,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showSairDialog) {
            AlertDialog(
                onDismissRequest = { showSairDialog = false },
                containerColor   = FundoCard,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FooIcone(FooIcones.Aviso, cor = ErroVermelho, tamanho = 22.dp)
                        Text("Sair da Turma?", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "Tem certeza que deseja sair da turma? Você precisará de um novo código para entrar novamente.",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    BotaoCartoon(
                        texto   = "Sair",
                        icone   = FooIcones.Sair,
                        onClick = { onSairDaTurma(); showSairDialog = false },
                        tipo    = BotaoCartoonTipo.PERIGO
                    )
                },
                dismissButton = {
                    BotaoCartoon(
                        texto   = "Cancelar",
                        onClick = { showSairDialog = false },
                        tipo    = BotaoCartoonTipo.NEUTRO
                    )
                }
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FooIcone(FooIcones.Encapsulamento, cor = AzulCinza, tamanho = 72.dp)
                Text("Você não está em uma turma", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                Text(
                    "Entre em uma turma usando o código fornecido pelo professor",
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                BotaoCartoon(
                    texto    = "Entrar com código",
                    icone    = FooIcones.Adicionar,
                    onClick  = onEntrar,
                    tipo     = BotaoCartoonTipo.SECUNDARIO,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RankingItem(posicao: Int, jogador: RankingJogador) {
    val corPosicao = when (posicao) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.White.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FundoCard.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(corPosicao.copy(alpha = 0.2f))
                    .border(1.dp, corPosicao, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$posicao", color = corPosicao, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
            Text(jogador.nome, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${jogador.vitorias} vitórias", color = Ciano, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("${jogador.taxaVitoria.toInt()}% taxa", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}

// ── CRIAR ────────────────────────────────────────────────────────────────────

@Composable
private fun TelaCriarTurma(
    uiState: TurmaUiState,
    onVoltar: () -> Unit,
    onCriar: (String) -> Unit
) {
    var nomeTurma by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Criar Turma", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FooIcone(FooIcones.Turmas, cor = Rosa, tamanho = 64.dp)

            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Nome da turma", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value         = nomeTurma,
                        onValueChange = { nomeTurma = it },
                        placeholder   = { Text("Ex: POO - 2026.1", color = Color.White.copy(alpha = 0.5f)) },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = Color.White,
                            focusedBorderColor   = Rosa,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    uiState.erro?.let {
                        Text(it, color = ErroVermelho)
                    }

                    if (uiState.carregando) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color    = Rosa
                        )
                    } else {
                        BotaoCartoon(
                            texto    = "Criar turma",
                            icone    = FooIcones.Adicionar,
                            onClick  = { onCriar(nomeTurma) },
                            tipo     = BotaoCartoonTipo.PRIMARIO,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Text(
                text      = "Um código único será gerado automaticamente para compartilhar com os alunos",
                style     = MaterialTheme.typography.bodySmall,
                color     = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── ENTRAR ───────────────────────────────────────────────────────────────────

@Composable
private fun TelaEntrarTurma(
    uiState: TurmaUiState,
    onVoltar: () -> Unit,
    onEntrar: (String) -> Unit
) {
    var codigo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Entrar em Turma", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FooIcone(FooIcones.Encapsulamento, cor = Ciano, tamanho = 64.dp)

            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Código da turma", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value         = codigo,
                        onValueChange = { codigo = it.uppercase().take(6) },
                        placeholder   = { Text("Ex: ABC123", color = Color.White.copy(alpha = 0.5f)) },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = Color.White,
                            focusedBorderColor   = Ciano,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    uiState.erro?.let {
                        Text(it, color = ErroVermelho)
                    }

                    if (uiState.carregando) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color    = Ciano
                        )
                    } else {
                        BotaoCartoon(
                            texto    = "Entrar na turma",
                            icone    = FooIcones.AdicionarPessoa,
                            onClick  = { onEntrar(codigo) },
                            tipo     = BotaoCartoonTipo.SECUNDARIO,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Text(
                text      = "Peça o código ao seu professor",
                style     = MaterialTheme.typography.bodySmall,
                color     = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}