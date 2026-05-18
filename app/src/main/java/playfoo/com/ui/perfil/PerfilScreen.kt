package playfoo.com.ui.perfil

import androidx.activity.compose.BackHandler
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
import playfoo.com.domain.AuthProvedor
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.carregar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (uiState.modoEdicao) {
        BackHandler { viewModel.fecharEditor() }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Avatar") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.fecharEditor() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AvatarEditorScreen(
                    avatarAtual = uiState.avatarConfig,
                    onSalvar = { viewModel.salvarAvatar(it) }
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            AvatarView(
                config = uiState.avatarConfig,
                estado = EstadoAvatar.NEUTRO,
                modifier = Modifier.size(200.dp)
            )

            val usuario = uiState.usuarioLogado
            if (usuario != null) {
                Text(usuario.nome, style = MaterialTheme.typography.headlineSmall)
                Text(usuario.email, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline)

                val provedorLabel = when (usuario.provedor) {
                    AuthProvedor.GOOGLE -> "Google"
                    AuthProvedor.EMAIL  -> "Email"
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Conta $provedorLabel",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            } else {
                Text("Visitante", style = MaterialTheme.typography.headlineSmall)
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = { viewModel.abrirEditor() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Avatar")
            }

            if (usuario != null) {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sair")
                }
            } else {
                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fazer Login")
                }
            }
        }
    }
}