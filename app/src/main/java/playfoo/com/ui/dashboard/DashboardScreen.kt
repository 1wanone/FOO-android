package playfoo.com.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import playfoo.com.domain.NivelAluno
import playfoo.com.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val jogador = uiState.jogador

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.carregar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cartão do jogador
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(jogador.nome, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        NivelBadge(jogador.calcularNivel())
                    }
                    Text(
                        text = nivelEmoji(jogador.calcularNivel()),
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }

            // Stats: partidas, vitórias, derrotas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Partidas", jogador.totalPartidas, Modifier.weight(1f))
                StatCard("Vitórias", jogador.totalVitorias, Modifier.weight(1f))
                StatCard("Derrotas", jogador.totalDerrotas, Modifier.weight(1f))
            }

            // Taxa de vitória
            val taxa = jogador.calcularTaxaVitoria()
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Taxa de Vitória", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${taxa.toInt()}%",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { taxa / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Progresso de nível
            NivelProgressCard(jogador.totalPartidas)
        }
    }
}

@Composable
private fun StatCard(label: String, valor: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NivelBadge(nivel: NivelAluno) {
    val (label, cor) = when (nivel) {
        NivelAluno.INICIANTE      -> "Iniciante" to MaterialTheme.colorScheme.secondary
        NivelAluno.INTERMEDIARIO  -> "Intermediário" to MaterialTheme.colorScheme.tertiary
        NivelAluno.AVANCADO       -> "Avançado" to MaterialTheme.colorScheme.primary
        NivelAluno.EXPERT         -> "Expert" to MaterialTheme.colorScheme.error
    }
    Surface(
        color = cor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = cor
        )
    }
}

@Composable
private fun NivelProgressCard(totalPartidas: Int) {
    val (proximoNivel, progresso, meta) = when {
        totalPartidas < 10  -> Triple("Intermediário", totalPartidas, 10)
        totalPartidas < 50  -> Triple("Avançado", totalPartidas - 10, 40)
        else                -> Triple("Expert", totalPartidas, totalPartidas)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Progresso de Nível", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$progresso / $meta partidas", style = MaterialTheme.typography.bodySmall)
                Text("Próximo: $proximoNivel", style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                progress = { if (meta > 0) progresso.toFloat() / meta else 1f },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun nivelEmoji(nivel: NivelAluno): String = when (nivel) {
    NivelAluno.INICIANTE     -> "🌱"
    NivelAluno.INTERMEDIARIO -> "📚"
    NivelAluno.AVANCADO      -> "⭐"
    NivelAluno.EXPERT        -> "🏆"
}
