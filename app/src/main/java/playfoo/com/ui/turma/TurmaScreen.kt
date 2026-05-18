package playfoo.com.ui.turma

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import playfoo.com.domain.Turma
import playfoo.com.viewmodel.TurmaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurmaScreen(
    navController: NavController,
    viewModel: TurmaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Turmas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Entrar em turma por código
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Entrar em uma turma", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = uiState.codigo,
                        onValueChange = viewModel::onCodigoChange,
                        label = { Text("Código da turma") },
                        placeholder = { Text("Ex: POO01") },
                        isError = uiState.erro != null,
                        supportingText = uiState.erro?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { viewModel.entrarTurma() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.entrarTurma() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Entrar")
                    }
                }
            }

            // Lista de turmas inscritas
            if (uiState.turmasInscritas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Você ainda não está em nenhuma turma.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text("Minhas turmas (${uiState.turmasInscritas.size})", style = MaterialTheme.typography.titleMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.turmasInscritas, key = { it.id }) { turma ->
                        TurmaCard(turma = turma, onSair = { viewModel.sairTurma(turma) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TurmaCard(turma: Turma, onSair: () -> Unit) {
    var confirmarSaida by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(turma.nome, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Código: ${turma.codigo}  •  Prof. ${turma.gestor.nome}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { confirmarSaida = true }) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Sair da turma",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            title = { Text("Sair da turma?") },
            text = { Text("Você quer sair de \"${turma.nome}\"?") },
            confirmButton = {
                TextButton(onClick = { confirmarSaida = false; onSair() }) {
                    Text("Sair", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar") }
            }
        )
    }
}
