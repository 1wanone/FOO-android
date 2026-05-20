package playfoo.com.ui.turma

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.CardCartoon
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
import playfoo.com.viewmodel.TelaTurma
import playfoo.com.viewmodel.TurmaViewModel

@Composable
fun TurmaScreen(
    onVoltar: () -> Unit = {},
    viewModel: TurmaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.telaTurma != TelaTurma.INICIAL) {
        viewModel.irPara(TelaTurma.INICIAL)
    }

    FundoTela(tipo = TipoFundo.TURMA) {
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
                    uiState = uiState,
                    onVoltar = onVoltar,
                    onCriar = { viewModel.irPara(TelaTurma.CRIAR) },
                    onEntrar = { viewModel.irPara(TelaTurma.ENTRAR) },
                    onLimpar = { viewModel.limparMensagens() }
                )
                TelaTurma.CRIAR -> TelaCriarTurma(
                    uiState = uiState,
                    onVoltar = { viewModel.irPara(TelaTurma.INICIAL) },
                    onCriar = { nome -> viewModel.criarTurma(nome) }
                )
                TelaTurma.ENTRAR -> TelaEntrarTurma(
                    uiState = uiState,
                    onVoltar = { viewModel.irPara(TelaTurma.INICIAL) },
                    onEntrar = { codigo -> viewModel.entrarNaTurma(codigo) }
                )
                TelaTurma.DETALHES -> TelaDetalhesTurma(
                    uiState = uiState,
                    onVoltar = { viewModel.irPara(TelaTurma.INICIAL) }
                )
            }
        }
    }
}

@Composable
private fun TelaInicialTurma(
    uiState: playfoo.com.viewmodel.TurmaUiState,
    onVoltar: () -> Unit,
    onCriar: () -> Unit,
    onEntrar: () -> Unit,
    onLimpar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                text = "Turmas",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        uiState.sucesso?.let { msg ->
            CardCartoon(corBorda = Color(0xFF4CAF50)) {
                Text(
                    text = msg,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(4000)
                onLimpar()
            }
        }

        uiState.erro?.let { erro ->
            CardCartoon(corBorda = Color(0xFFE53935)) {
                Text(
                    text = erro,
                    color = Color(0xFFE53935),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "🏫", fontSize = 80.sp)

        Text(
            text = "Gerencie suas turmas",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Crie uma turma ou entre em uma existente usando o código fornecido pelo professor",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        BotaoCartoon(
            texto = "🎓  Criar turma",
            onClick = onCriar,
            tipo = BotaoCartoonTipo.PRIMARIO,
            modifier = Modifier.fillMaxWidth()
        )

        BotaoCartoon(
            texto = "🔑  Entrar com código",
            onClick = onEntrar,
            tipo = BotaoCartoonTipo.SECUNDARIO,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TelaCriarTurma(
    uiState: playfoo.com.viewmodel.TurmaUiState,
    onVoltar: () -> Unit,
    onCriar: (String) -> Unit
) {
    var nomeTurma by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                text = "Criar Turma",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "🏫", fontSize = 64.sp)

        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Nome da turma",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = nomeTurma,
                    onValueChange = { nomeTurma = it },
                    placeholder = { Text("Ex: POO - 2026.1", color = Color.White.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                uiState.erro?.let {
                    Text(text = it, color = Color(0xFFE53935))
                }

                if (uiState.carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color(0xFF6C63FF)
                    )
                } else {
                    BotaoCartoon(
                        texto = "Criar turma",
                        onClick = { onCriar(nomeTurma) },
                        tipo = BotaoCartoonTipo.PRIMARIO,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Text(
            text = "Um código único será gerado automaticamente para compartilhar com os alunos",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TelaEntrarTurma(
    uiState: playfoo.com.viewmodel.TurmaUiState,
    onVoltar: () -> Unit,
    onEntrar: (String) -> Unit
) {
    var codigo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                text = "Entrar em Turma",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "🔑", fontSize = 64.sp)

        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Código da turma",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.uppercase().take(6) },
                    placeholder = { Text("Ex: ABC123", color = Color.White.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                uiState.erro?.let {
                    Text(text = it, color = Color(0xFFE53935))
                }

                if (uiState.carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    BotaoCartoon(
                        texto = "Entrar na turma",
                        onClick = { onEntrar(codigo) },
                        tipo = BotaoCartoonTipo.SECUNDARIO,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Text(
            text = "Peça o código ao seu professor",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TelaDetalhesTurma(
    uiState: playfoo.com.viewmodel.TurmaUiState,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                text = "Minha Turma",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        uiState.turmaAtual?.let { turma ->
            CardCartoon(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = turma["nome"].toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Código:", color = Color.White.copy(alpha = 0.7f))
                        Text(
                            text = turma["codigo"].toString(),
                            color = Color(0xFF6C63FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
