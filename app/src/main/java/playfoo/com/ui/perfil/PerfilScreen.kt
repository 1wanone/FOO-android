package playfoo.com.ui.perfil

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedContent
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
import playfoo.com.domain.AuthUser
import playfoo.com.domain.JogadorAluno
import playfoo.com.domain.NivelAluno
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.CardCartoon
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.viewmodel.PerfilViewModel

private enum class TelaPerfil { PERFIL, EDITOR_AVATAR }

// Mock — será substituído quando o backend estiver pronto
private val jogadorMock = JogadorAluno(
    id            = "1",
    nome          = "Jogador Teste",
    totalPartidas = 15,
    totalVitorias = 9,
    totalDerrotas = 6,
    turmaId       = "POO-2026"
)

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
                        avatarConfig   = uiState.avatarConfig,
                        usuario        = uiState.usuarioLogado,
                        jogador        = jogadorMock,
                        onPersonalizar = { viewModel.abrirEditor() },
                        onLogout       = {
                            viewModel.logout()
                            navController.navigate("login") {
                                popUpTo("menu") { inclusive = false }
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
    avatarConfig: AvatarConfig,
    usuario: AuthUser?,
    jogador: JogadorAluno,
    onPersonalizar: () -> Unit,
    onLogout: () -> Unit,
    onFazerLogin: () -> Unit,
    onEntrarTurma: () -> Unit,
    onVoltar: () -> Unit
) {
    val nomeExibido = usuario?.nome ?: jogador.nome
    val nivel = jogador.calcularNivel()
    val taxa  = jogador.calcularTaxaVitoria()

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
            config = avatarConfig,
            estado = EstadoAvatar.NEUTRO,
            modifier = Modifier.size(160.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = nomeExibido,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        usuario?.email?.let { email ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = email,
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

        // Estatísticas
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
                Surface(color = corNivel(nivel), shape = MaterialTheme.shapes.small) {
                    Text(
                        text = nivel.name,
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

            LinhaEstatistica("Partidas jogadas", "${jogador.totalPartidas}", Color.White)
            Spacer(Modifier.height(8.dp))
            LinhaEstatistica("Vitórias", "${jogador.totalVitorias}", Color(0xFF66BB6A))
            Spacer(Modifier.height(8.dp))
            LinhaEstatistica("Derrotas", "${jogador.totalDerrotas}", Color(0xFFEF5350))

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
                    text = "%.0f%%".format(taxa),
                    color = Color(0xFF66BB6A),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress      = { taxa / 100f },
                modifier      = Modifier.fillMaxWidth(),
                color         = Color(0xFF66BB6A),
                trackColor    = Color.White.copy(alpha = 0.15f)
            )
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

            if (jogador.turmaId != null) {
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
                            text = jogador.turmaId,
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

        Spacer(Modifier.height(24.dp))

        if (usuario != null) {
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

        // AvatarEditorScreen já tem seu próprio scroll interno
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

private fun corNivel(nivel: NivelAluno): Color = when (nivel) {
    NivelAluno.INICIANTE     -> Color(0xFF4CAF50) // verde
    NivelAluno.INTERMEDIARIO -> Color(0xFF2196F3) // azul
    NivelAluno.AVANCADO      -> Color(0xFFFF9800) // laranja
    NivelAluno.EXPERT        -> Color(0xFF9C27B0) // roxo
}
