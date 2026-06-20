package playfoo.com.ui.perfil

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import playfoo.com.domain.AvatarConfig
import playfoo.com.domain.TipoUsuario
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.BottomNavFoo
import playfoo.com.ui.components.CardCartoon
import playfoo.com.ui.components.FooIcone
import playfoo.com.ui.components.FooIcones
import playfoo.com.ui.components.AvatarCirculo
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.HeaderFoo
import playfoo.com.ui.components.TipoFundo
import playfoo.com.ui.theme.*
import playfoo.com.viewmodel.PerfilUiState
import playfoo.com.viewmodel.PerfilViewModel

private enum class TelaPerfil { PERFIL, EDITOR_AVATAR }

@Composable
fun PerfilScreen(
    navController: NavController,
    onGerenciarTurmas: () -> Unit = {},
    onVerDashboard: (turmaId: String) -> Unit = {},
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val telaPerfil = if (uiState.modoEdicao) TelaPerfil.EDITOR_AVATAR else TelaPerfil.PERFIL

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.carregar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = telaPerfil == TelaPerfil.EDITOR_AVATAR) {
        viewModel.fecharEditor()
    }

    FundoTela(tipo = TipoFundo.PERFIL) {
        AnimatedContent(
            targetState = telaPerfil,
            transitionSpec = {
                val avancando = targetState.ordinal > initialState.ordinal
                val entrar = if (avancando) slideInHorizontally { it } + fadeIn()
                             else           slideInHorizontally { -it } + fadeIn()
                val sair   = if (avancando) slideOutHorizontally { -it } + fadeOut()
                             else           slideOutHorizontally { it } + fadeOut()
                entrar togetherWith sair
            },
            label = "transicao_perfil"
        ) { tela ->
            when (tela) {
                TelaPerfil.PERFIL -> Box(modifier = Modifier.fillMaxSize()) {
                    ConteudoPerfil(
                        uiState           = uiState,
                        onPersonalizar    = { viewModel.abrirEditor() },
                        onFazerLogin      = { navController.navigate("login") },
                        onEntrarTurma     = { navController.navigate("turmas") },
                        onVoltar          = { navController.navigateUp() },
                        onGerenciarTurmas = onGerenciarTurmas,
                        onVerDashboard    = onVerDashboard,
                        modifier          = Modifier.padding(bottom = 80.dp)
                    )
                    BottomNavFoo(
                        currentRoute = "perfil",
                        onInicio     = { navController.navigate("menu") },
                        onTurma      = { navController.navigate("turmas") },
                        onPerfil     = {},
                        onOpcoes     = { navController.navigate("opcoes") },
                        modifier     = Modifier.align(Alignment.BottomCenter)
                    )
                }
                TelaPerfil.EDITOR_AVATAR ->
                    ConteudoEditorAvatar(
                        avatarAtual = uiState.avatarConfig,
                        onSalvar    = { viewModel.salvarAvatar(it) },
                        onVoltar    = { viewModel.fecharEditor() }
                    )
            }
        }
    }
}

@Composable
private fun ConteudoPerfil(
    uiState: PerfilUiState,
    onPersonalizar: () -> Unit,
    onFazerLogin: () -> Unit,
    onEntrarTurma: () -> Unit,
    onVoltar: () -> Unit,
    onGerenciarTurmas: () -> Unit,
    onVerDashboard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HeaderFoo("Meu Perfil", onVoltar = onVoltar)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(Modifier.height(8.dp))

        // Seção do avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(corNivel(uiState.nivel).copy(alpha = 0.18f), Color.Transparent)
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarCirculo(
                    config   = uiState.avatarConfig,
                    tamanho  = 90.dp,
                    bordaCor = Pink
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = uiState.nomeUsuario.ifBlank { "Jogador" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                if (uiState.emailUsuario.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = uiState.emailUsuario,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .background(Pink.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                        .border(1.5.dp, Pink, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FooIcone(icone = FooIcones.Estrela, cor = Pink, tamanho = 16.dp)
                        Text(
                            text = uiState.nivel,
                            color = Pink,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                BotaoCartoon(
                    texto    = "Personalizar Avatar",
                    icone    = FooIcones.Avatar,
                    onClick  = onPersonalizar,
                    tipo     = BotaoCartoonTipo.PRIMARIO,
                    modifier = Modifier.fillMaxWidth(0.78f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Turma
        if (uiState.tipoUsuario == TipoUsuario.GESTOR) {
            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FooIcone(icone = FooIcones.Turmas, cor = Rosa, tamanho = 18.dp)
                            Text(
                                text = "Minhas Turmas (${uiState.turmasDoGestor.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        IconButton(onClick = onGerenciarTurmas) {
                            Icon(imageVector = FooIcones.Turma, contentDescription = "Gerenciar turmas", tint = Rosa)
                        }
                    }

                    if (uiState.turmasDoGestor.isEmpty()) {
                        Text(
                            text = "Nenhuma turma criada ainda",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        BotaoCartoon(
                            texto    = "Criar turma",
                            icone    = FooIcones.Adicionar,
                            onClick  = onGerenciarTurmas,
                            tipo     = BotaoCartoonTipo.SECUNDARIO,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        uiState.turmasDoGestor.forEach { turma ->
                            CardCartoon(modifier = Modifier.fillMaxWidth(), corBorda = Rosa, padding = 12.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = turma["nome"].toString(), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Código: ${turma["codigo"]}", color = Rosa, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        val membros = (turma["membros"] as? List<*>)?.size ?: 0
                                        Text(text = "$membros aluno(s)", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { onVerDashboard(turma["id"].toString()) }) {
                                        Icon(imageVector = FooIcones.Grafico, contentDescription = "Ver dashboard", tint = Rosa)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Turma", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (uiState.turmaAluno != null) {
                    val turma = uiState.turmaAluno
                    val nome = turma["nome"]?.toString() ?: ""
                    if (nome.isNotBlank()) {
                        Text(text = nome, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Código", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                        Surface(color = Rosa.copy(alpha = 0.25f), shape = MaterialTheme.shapes.small) {
                            Text(
                                text = turma["codigo"]?.toString() ?: "",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextoSecundario,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(text = "Você não está em nenhuma turma.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                    Spacer(Modifier.height(12.dp))
                    BotaoCartoon(texto = "Entrar em uma turma", onClick = onEntrarTurma, tipo = BotaoCartoonTipo.SECUNDARIO, modifier = Modifier.fillMaxWidth(), fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Estatísticas
        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FooIcone(icone = FooIcones.Grafico, cor = Rosa, tamanho = 18.dp)
                Text(text = "Minhas Estatísticas", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Nível:", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                Surface(color = Pink, shape = MaterialTheme.shapes.medium) {
                    Text(text = uiState.nivel, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniCard(FooIcones.Jogador, uiState.totalPartidas.toString(), "Partidas", Modifier.weight(1f))
                MiniCard(FooIcones.Trofeu,  uiState.totalVitorias.toString(), "Vitórias", Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniCard(FooIcones.Derrota, uiState.totalDerrotas.toString(), "Derrotas", Modifier.weight(1f))
                MiniCard(FooIcones.Grafico, "${uiState.taxaVitoria.toInt()}%", "Taxa",    Modifier.weight(1f))
            }

            if (uiState.temaFavorito.isNotBlank() && uiState.temaFavorito != "Nenhum") {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tema favorito:", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                    Box(
                        modifier = Modifier
                            .background(Pink, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = uiState.temaFavorito, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (uiState.palavrasErradas.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(text = "Palavras difíceis:", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                val maxErros = uiState.palavrasErradas.values.maxOrNull()?.toFloat() ?: 1f
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.palavrasErradas.entries.forEach { (palavra, erros) ->
                        val chipAlpha = 0.5f + (erros.toFloat() / maxErros * 0.5f)
                        val chipSize  = (12 + (erros.toFloat() / maxErros * 10).toInt()).sp
                        Box(
                            modifier = Modifier
                                .border(1.dp, Rosa.copy(alpha = chipAlpha), RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(RoxoMedio.copy(alpha = chipAlpha))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = palavra, color = Color.White, fontSize = chipSize, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Gráficos por tema
        if (uiState.estatisticasPorTema.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FooIcone(icone = FooIcones.Grafico, cor = Rosa, tamanho = 18.dp)
                    Text("Desempenho por Tema", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))
                AndroidView(
                    factory = { context ->
                        com.github.mikephil.charting.charts.BarChart(context).apply {
                            description.isEnabled = false
                            legend.apply { isEnabled = true; textColor = android.graphics.Color.WHITE }
                            axisRight.isEnabled = false
                            axisLeft.apply {
                                textColor = android.graphics.Color.WHITE
                                axisMinimum = 0f
                                gridColor = android.graphics.Color.argb(50, 255, 255, 255)
                            }
                            xAxis.apply {
                                textColor = android.graphics.Color.WHITE
                                granularity = 1f
                                setDrawGridLines(false)
                                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                labelRotationAngle = -45f
                            }
                            setExtraBottomOffset(20f)
                            setTouchEnabled(false)
                        }
                    },
                    update = { chart ->
                        val labels = uiState.estatisticasPorTema.map {
                            it["tema"].toString().let { n -> if (n.length > 5) n.take(5) + "." else n }
                        }
                        val entriesVitorias = uiState.estatisticasPorTema.mapIndexed { i, d ->
                            com.github.mikephil.charting.data.BarEntry(i.toFloat(), (d["vitorias"] as? Int)?.toFloat() ?: 0f)
                        }
                        val entriesDerrotas = uiState.estatisticasPorTema.mapIndexed { i, d ->
                            com.github.mikephil.charting.data.BarEntry(i.toFloat(), (d["derrotas"] as? Int)?.toFloat() ?: 0f)
                        }
                        val intFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                            override fun getFormattedValue(value: Float) = value.toInt().toString()
                        }
                        val setVitorias = com.github.mikephil.charting.data.BarDataSet(entriesVitorias, "Vitórias").apply {
                            color = Ciano.toArgb()
                            valueTextColor = android.graphics.Color.WHITE; valueTextSize = 9f; valueFormatter = intFormatter
                        }
                        val setDerrotas = com.github.mikephil.charting.data.BarDataSet(entriesDerrotas, "Derrotas").apply {
                            color = ErroVermelho.toArgb()
                            valueTextColor = android.graphics.Color.WHITE; valueTextSize = 9f; valueFormatter = intFormatter
                        }
                        chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels.toTypedArray())
                        val barData = com.github.mikephil.charting.data.BarData(setVitorias, setDerrotas).apply { barWidth = 0.35f }
                        chart.data = barData
                        chart.groupBars(0f, 0.1f, 0.05f)
                        chart.invalidate()
                    },
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FooIcone(icone = FooIcones.Meta, cor = Rosa, tamanho = 18.dp)
                    Text("Temas Jogados", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))
                AndroidView(
                    factory = { context ->
                        com.github.mikephil.charting.charts.PieChart(context).apply {
                            description.isEnabled = false
                            isDrawHoleEnabled = true
                            holeRadius = 40f
                            setHoleColor(android.graphics.Color.TRANSPARENT)
                            legend.apply { isEnabled = true; textColor = android.graphics.Color.WHITE; textSize = 10f }
                            setEntryLabelColor(android.graphics.Color.WHITE)
                            setEntryLabelTextSize(10f)
                        }
                    },
                    update = { chart ->
                        val cores = listOf(Rosa, Ciano, RoxoMedio, ErroVermelho, AzulCinza, Pink, Color(0xFFFFEB3B), Color(0xFF9C27B0), Color(0xFF607D8B))
                        val entries = uiState.estatisticasPorTema.map { d ->
                            com.github.mikephil.charting.data.PieEntry((d["total"] as? Int)?.toFloat() ?: 0f, d["tema"].toString().take(8))
                        }
                        val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "").apply {
                            colors = cores.map { it.toArgb() }
                            valueTextColor = android.graphics.Color.WHITE; valueTextSize = 11f
                        }
                        chart.data = com.github.mikephil.charting.data.PieData(dataSet)
                        chart.invalidate()
                    },
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (uiState.usuarioLogado == null) {
            BotaoCartoon(
                texto    = "Fazer Login",
                onClick  = onFazerLogin,
                tipo     = BotaoCartoonTipo.PRIMARIO,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MiniCard(icone: ImageVector, valor: String, label: String, modifier: Modifier = Modifier) {
    CardCartoon(modifier = modifier, padding = 8.dp, corBorda = Rosa, corFundo = RoxoEscuro, raio = 8.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FooIcone(icone = icone, cor = Rosa, tamanho = 22.dp)
            Text(valor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ConteudoEditorAvatar(
    avatarAtual: AvatarConfig,
    onSalvar: (AvatarConfig) -> Unit,
    onVoltar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderFoo("Personalizar Avatar", onVoltar = onVoltar)

        Box(modifier = Modifier.weight(1f)) {
            AvatarEditorScreen(
                avatarAtual = avatarAtual,
                onSalvar    = onSalvar
            )
        }
    }
}

private fun corNivel(nivel: String): Color = when (nivel) {
    "INICIANTE"     -> Color(0xFF4CAF50)
    "INTERMEDIARIO" -> Color(0xFF2196F3)
    "AVANCADO"      -> Color(0xFFFF9800)
    "EXPERT"        -> Color(0xFF9C27B0)
    else            -> Color(0xFF4CAF50)
}