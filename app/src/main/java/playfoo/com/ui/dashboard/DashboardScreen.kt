package playfoo.com.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import playfoo.com.domain.PalavraFrequencia
import playfoo.com.domain.TipoUsuario
import playfoo.com.ui.components.*
import playfoo.com.ui.theme.*
import playfoo.com.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    turmaId: String = "demo",
    onVoltar: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(turmaId) {
        viewModel.carregarDados(turmaId)
    }

    FundoTela(tipo = TipoFundo.DASHBOARD) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderFoo(titulo = "Dashboard", onVoltar = onVoltar)

            if (uiState.nomeTurma.isNotBlank()) {
                Text(
                    text     = uiState.nomeTurma,
                    color    = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.carregando) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Rosa)
                    }
                } else if (uiState.tipoUsuario != TipoUsuario.GESTOR) {
                    CardCartoon(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FooIcone(FooIcones.Encapsulamento, cor = AzulCinza, tamanho = 48.dp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text       = "Acesso restrito a professores",
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign  = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text      = "Somente gestores podem visualizar o dashboard",
                                color     = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                style     = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    // Cards de resumo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardResumo(
                            icone    = FooIcones.Jogador,
                            valor    = uiState.totalPartidas.toString(),
                            label    = "Partidas",
                            cor      = Rosa,
                            modifier = Modifier.weight(1f)
                        )
                        CardResumo(
                            icone    = FooIcones.Turma,
                            valor    = uiState.totalAlunos.toString(),
                            label    = "Alunos",
                            cor      = Ciano,
                            modifier = Modifier.weight(1f)
                        )
                        CardResumo(
                            icone    = FooIcones.Trofeu,
                            valor    = "${uiState.taxaVitoriaGeral.toInt()}%",
                            label    = "Vitórias",
                            cor      = Rosa,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Gráfico de pizza
                    if (uiState.estatisticasPorTema.isNotEmpty()) {
                        CardCartoon(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FooIcone(FooIcones.Grafico, cor = Rosa, tamanho = 18.dp)
                                Text("Partidas por Tema", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            GraficoPizza(
                                dados = uiState.estatisticasPorTema.map {
                                    it.nome to it.totalPartidas.toFloat()
                                },
                                totalPartidas = uiState.totalPartidas
                            )
                        }
                    }

                    // Gráfico de barras agrupado (vitórias + derrotas)
                    if (uiState.estatisticasPorTema.isNotEmpty()) {
                        CardCartoon(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FooIcone(FooIcones.Meta, cor = Ciano, tamanho = 18.dp)
                                Text("Vitórias e Derrotas por Tema", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            GraficoBarrasAgrupado(
                                labels   = uiState.estatisticasPorTema.map { e -> if (e.nome.length > 5) e.nome.take(5) + "." else e.nome },
                                vitorias = uiState.estatisticasPorTema.map { it.totalVitorias.toFloat() },
                                derrotas = uiState.estatisticasPorTema.map { it.totalDerrotas.toFloat() }
                            )
                        }
                    }

                    // Nuvem de palavras (mesmo componente do perfil)
                    if (uiState.palavrasMaisDificeis.isNotEmpty()) {
                        CardCartoon(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FooIcone(FooIcones.Excecao, cor = ErroVermelho, tamanho = 18.dp)
                                Text("Palavras mais difíceis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            NuvemPalavras(
                                palavras = uiState.palavrasMaisDificeis.map {
                                    PalavraFrequencia(it.palavra, it.totalErros)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Sem dados
                    if (uiState.totalPartidas == 0) {
                        CardCartoon(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier            = Modifier.fillMaxWidth()
                            ) {
                                FooIcone(FooIcones.Grafico, cor = AzulCinza, tamanho = 48.dp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text       = "Nenhuma partida ainda",
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign  = TextAlign.Center
                                )
                                Text(
                                    text      = "Os dados aparecerão quando os alunos começarem a jogar",
                                    color     = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    style     = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardResumo(
    icone: ImageVector,
    valor: String,
    label: String,
    cor: Color,
    modifier: Modifier = Modifier
) {
    CardCartoon(modifier = modifier, padding = 8.dp, corBorda = Rosa, corFundo = RoxoEscuro, raio = 8.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FooIcone(icone = icone, cor = Rosa, tamanho = 22.dp)
            Text(text = valor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun GraficoPizza(dados: List<Pair<String, Float>>, totalPartidas: Int) {
    val cores = listOf(
        Rosa, Ciano, RoxoMedio, ErroVermelho, AzulCinza, Pink,
        Color(0xFFFFEB3B), Color(0xFF9C27B0), Color(0xFF607D8B)
    )
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled     = true
                holeRadius            = 40f
                setHoleColor(android.graphics.Color.TRANSPARENT)
                legend.apply {
                    isEnabled           = true
                    textColor           = android.graphics.Color.WHITE
                    textSize            = 10f
                    orientation         = Legend.LegendOrientation.VERTICAL
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                }
                setEntryLabelColor(android.graphics.Color.WHITE)
                setEntryLabelTextSize(10f)
                setCenterTextColor(android.graphics.Color.WHITE)
                setCenterTextSize(12f)
            }
        },
        update = { chart ->
            val entries = dados.map { (label, value) ->
                PieEntry(value, if (label.length > 7) label.take(7) else label)
            }
            val dataSet = PieDataSet(entries, "").apply {
                colors         = cores.map { it.toArgb() }
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize  = 11f
            }
            chart.data       = PieData(dataSet)
            chart.centerText = "$totalPartidas\npartidas"
            chart.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(220.dp)
    )
}

@Composable
private fun GraficoBarrasAgrupado(
    labels: List<String>,
    vitorias: List<Float>,
    derrotas: List<Float>
) {
    val intFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float) = value.toInt().toString()
    }
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                axisRight.isEnabled   = false
                axisLeft.apply {
                    textColor    = android.graphics.Color.WHITE
                    axisMinimum  = 0f
                    gridColor    = android.graphics.Color.argb(50, 255, 255, 255)
                }
                xAxis.apply {
                    textColor          = android.graphics.Color.WHITE
                    granularity        = 1f
                    setDrawGridLines(false)
                    position           = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    labelRotationAngle = -45f
                    setCenterAxisLabels(true)
                }
                legend.apply {
                    isEnabled  = true
                    textColor  = android.graphics.Color.WHITE
                    textSize   = 11f
                }
                setExtraBottomOffset(20f)
                setTouchEnabled(false)
            }
        },
        update = { chart ->
            val entriesVitorias = vitorias.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
            val entriesDerrotas = derrotas.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
            val dsVitorias = BarDataSet(entriesVitorias, "Vitórias").apply {
                color          = Rosa.toArgb()
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize  = 10f
                valueFormatter = intFormatter
            }
            val dsDerrotas = BarDataSet(entriesDerrotas, "Derrotas").apply {
                color          = AzulCinza.toArgb()
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize  = 10f
                valueFormatter = intFormatter
            }
            val groupCount   = labels.size.toFloat()
            val groupSpace   = 0.15f
            val barSpace     = 0.03f
            val barWidth     = 0.35f
            val barData = BarData(dsVitorias, dsDerrotas).apply {
                this.barWidth = barWidth
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels.toTypedArray())
            chart.xAxis.axisMinimum    = 0f
            chart.xAxis.axisMaximum    = groupCount
            chart.data = barData
            chart.groupBars(0f, groupSpace, barSpace)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxWidth().height(260.dp)
    )
}

