package playfoo.com.ui.selecionar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import playfoo.com.domain.Dificuldade
import playfoo.com.viewmodel.SelecionarTemaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarTemaScreen(
    navController: NavController,
    viewModel: SelecionarTemaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolher Tema") },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text("Dificuldade", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dificuldade.entries.forEach { dif ->
                    FilterChip(
                        selected = uiState.dificuldade == dif,
                        onClick = { viewModel.selecionarDificuldade(dif) },
                        label = { Text(dif.name) }
                    )
                }
            }

            HorizontalDivider()

            Text("Tema", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.temas) { tema ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("jogo/${tema.id}/${uiState.dificuldade.name}")
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(tema.nome, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${tema.palavras.size} palavras",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
