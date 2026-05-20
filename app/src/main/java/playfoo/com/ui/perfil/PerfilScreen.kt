package playfoo.com.ui.perfil

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.CardCartoon
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.viewmodel.PerfilUiState
import playfoo.com.viewmodel.PerfilViewModel

private enum class TelaPerfil { PERFIL, EDITOR_AVATAR }

@Composable
fun PerfilScreen(
    navController: NavController,
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
                TelaPerfil.PERFIL ->
                    ConteudoPerfil(
                        uiState        = uiState,
                        onPersonalizar = { viewModel.abrirEditor() },
                        onLogout       = {
                            viewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onFazerLogin   = { navController.navigate("login") },
                        onEntrarTurma  = { navController.navigate("turmas") },
                        onVoltar       = { navController.navigateUp() }
                    )
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
    onLogout: () -> Unit,
    onFazerLogin: () -> Unit,
    onEntrarTurma: () -> Unit,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                text = "Meu Perfil",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        // Avatar
        AvatarView(
            config = uiState.avatarConfig,
            estado = EstadoAvatar.NEUTRO,
            modifier = Modifier.size(160.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = uiState.nomeUsuario.ifBlank { "Jogador" },
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        if (uiState.emailUsuario.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = uiState.emailUsuario,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }

        Spacer(Modifier.height(16.dp))

        BotaoCartoon(
            texto    = "Personalizar Avatar",
            onClick  = onPersonalizar,
            tipo     = BotaoCartoonTipo.SECUNDARIO,
            modifier = Modifier.fillMaxWidth(0.78f),
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        // Estatísticas básicas
        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Estatísticas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nível",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(color = corNivel(uiState.nivel), shape = MaterialTheme.shapes.small) {
                    Text(
                        text = uiState.nivel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))

            if (uiState.carregando) {
                CircularProgressIndicator(
                    color = Color(0xFF6C63FF),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LinhaEstatistica("Partidas jogadas", "${uiState.totalPartidas}", Color.White)
                Spacer(Modifier.height(8.dp))
                LinhaEstatistica("Vitórias", "${uiState.totalVitorias}", Color(0xFF66BB6A))
                Spacer(Modifier.height(8.dp))
                LinhaEstatistica("Derrotas", "${uiState.totalDerrotas}", Color(0xFFEF5350))

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Taxa de vitória",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "%.0f%%".format(uiState.taxaVitoria),
                        color = Color(0xFF66BB6A),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress   = { uiState.taxaVitoria / 100f },
                    modifier   = Modifier.fillMaxWidth(),
                    color      = Color(0xFF66BB6A),
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Turma
        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Turma",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            if (uiState.turmaId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Código",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = Color(0xFF6C63FF).copy(alpha = 0.25f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = uiState.turmaId,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFAFA8FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "Você não está em nenhuma turma.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                BotaoCartoon(
                    texto    = "Entrar em uma turma",
                    onClick  = onEntrarTurma,
                    tipo     = BotaoCartoonTipo.SECUNDARIO,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Minhas Estatísticas detalhadas
        CardCartoon(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "📈 Minhas Estatísticas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // Nível chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nível:",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(color = corNivel(uiState.nivel), shape = MaterialTheme.shapes.medium) {
                    Text(
                        text = uiState.nivel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Grid 2x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniCard("🎮", uiState.totalPartidas.toString(), "Partidas", Modifier.weight(1f))
                MiniCard("🏆", uiState.totalVitorias.toString(), "Vitórias", Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniCard("💔", uiState.totalDerrotas.toString(), "Derrotas", Modifier.weight(1f))
                MiniCard("📊", "${uiState.taxaVitoria.toInt()}%", "Taxa", Modifier.weight(1f))
            }

            if (uiState.temaFavorito.isNotBlank() && uiState.temaFavorito != "Nenhum") {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tema favorito:",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = uiState.temaFavorito,
                        color = Color(0xFF6C63FF),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.palavrasErradas.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Palavras difíceis:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                uiState.palavrasErradas.entries.forEach { (palavra, erros) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(palavra, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        Text("$erros erros", color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (uiState.usuarioLogado != null) {
            BotaoCartoon(
                texto    = "Sair",
                onClick  = onLogout,
                tipo     = BotaoCartoonTipo.PERIGO,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
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

@Composable
private fun MiniCard(emoji: String, valor: String, label: String, modifier: Modifier = Modifier) {
    CardCartoon(modifier = modifier, padding = 8.dp, corBorda = Color(0xFF6C63FF)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
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
        Surface(
            color = Color.Black.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
                Text(
                    text = "Editar Avatar",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AvatarEditorScreen(
                avatarAtual = avatarAtual,
                onSalvar    = onSalvar
            )
        }
    }
}

@Composable
private fun LinhaEstatistica(label: String, valor: String, corValor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
        Text(valor, color = corValor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

private fun corNivel(nivel: String): Color = when (nivel) {
    "INICIANTE"     -> Color(0xFF4CAF50)
    "INTERMEDIARIO" -> Color(0xFF2196F3)
    "AVANCADO"      -> Color(0xFFFF9800)
    "EXPERT"        -> Color(0xFF9C27B0)
    else            -> Color(0xFF4CAF50)
}
