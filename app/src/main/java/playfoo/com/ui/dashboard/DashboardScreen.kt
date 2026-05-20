package playfoo.com.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import playfoo.com.ui.components.CardCartoon
import playfoo.com.domain.TipoUsuario
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
                Column {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.nomeTurma,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            if (uiState.carregando) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFF9800))
                }
            } else if (uiState.tipoUsuario != TipoUsuario.GESTOR) {
                CardCartoon(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "🔒", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Acesso restrito a professores",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Somente gestores podem visualizar o dashboard",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
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
                        emoji = "🎮",
                        valor = uiState.totalPartidas.toString(),
                        label = "Partidas",
                        cor = Color(0xFF6C63FF),
                        modifier = Modifier.weight(1f)
                    )
                    CardResumo(
                        emoji = "👥",
                        valor = uiState.totalAlunos.toString(),
                        label = "Alunos",
                        cor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    CardResumo(
                        emoji = "🏆",
                        valor = "${uiState.taxaVitoriaGeral.toInt()}%",
                        label = "Vitórias",
                        cor = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Gráfico de pizza — partidas por tema
                if (uiState.estatisticasPorTema.isNotEmpty()) {
                    CardCartoon(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "📊 Partidas por Tema",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        GraficoPizza(
                            dados = uiState.estatisticasPorTema.map {
                                it.nome to it.totalPartidas.toFloat()
                            }
                        )
                    }
                }

                // Gráfico de barras — taxa de vitória por tema
                if (uiState.estatisticasPorTema.isNotEmpty()) {
                    CardCartoon(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "✅ Taxa de Vitória por Tema",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        GraficoBarras(
                            labels = uiState.estatisticasPorTema.map { e -> if (e.nome.length > 5) e.nome.take(5) + "." else e.nome },
                            valores = uiState.estatisticasPorTema.map { it.taxaVitoria }
                        )
                    }
                }

                // Nuvem de palavras
                if (uiState.palavrasMaisDificeis.isNotEmpty()) {
                    CardCartoon(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "☁️ Nuvem de Palavras Difíceis",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        NuvemPalavras(palavras = uiState.palavrasMaisDificeis)
                    }
                }

                // Mensagem sem dados
                if (uiState.totalPartidas == 0) {
                    CardCartoon(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "📭", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma partida ainda",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Os dados aparecerão quando os alunos começarem a jogar",
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardResumo(
    emoji: String,
    valor: String,
    label: String,
    cor: Color,
    modifier: Modifier = Modifier
) {
    CardCartoon(
        modifier = modifier,
        corBorda = cor,
        padding = 12.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 24.sp)
            Text(
                text = valor,
                color = cor,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun GraficoPizza(dados: List<Pair<String, Float>>) {
    val cores = listOf(
        Color(0xFF6C63FF), Color(0xFF4CAF50), Color(0xFFFF9800),
        Color(0xFFE53935), Color(0xFF00BCD4), Color(0xFFFF4081),
        Color(0xFFFFEB3B), Color(0xFF9C27B0), Color(0xFF607D8B)
    )
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 40f
                setHoleColor(android.graphics.Color.TRANSPARENT)
                legend.apply {
                    isEnabled = true
                    textColor = android.graphics.Color.WHITE
                    textSize = 10f
                    orientation = Legend.LegendOrientation.VERTICAL
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                }
                setEntryLabelColor(android.graphics.Color.WHITE)
                setEntryLabelTextSize(10f)
            }
        },
        update = { chart ->
            val entries = dados.mapIndexed { _, (label, value) ->
                PieEntry(value, if (label.length > 8) label.take(8) else label)
            }
            val dataSet = PieDataSet(entries, "").apply {
                colors = cores.map { it.toArgb() }
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize = 11f
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

@Composable
private fun GraficoBarras(labels: List<String>, valores: List<Float>) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.apply {
                    textColor = android.graphics.Color.WHITE
                    axisMinimum = 0f
                    axisMaximum = 100f
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
            val entries = valores.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
            val dataSet = BarDataSet(entries, "Taxa de Vitória %").apply {
                color = Color(0xFF6C63FF).toArgb()
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize = 10f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float) = value.toInt().toString()
                }
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels.toTypedArray())
            chart.data = BarData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )
}

@Composable
private fun NuvemPalavras(palavras: List<playfoo.com.viewmodel.EstatisticaPalavra>) {
    val maxErros = palavras.maxOfOrNull { it.totalErros }?.toFloat() ?: 1f
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        palavras.forEach { palavra ->
            val tamanho = 12 + (palavra.totalErros.toFloat() / maxErros * 16).toInt()
            val alpha = 0.5f + (palavra.totalErros.toFloat() / maxErros * 0.5f)
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFE53935).copy(alpha = alpha),
                        androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = palavra.palavra,
                    color = Color.White,
                    fontSize = tamanho.sp,
                    fontWeight = if (tamanho > 20) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
