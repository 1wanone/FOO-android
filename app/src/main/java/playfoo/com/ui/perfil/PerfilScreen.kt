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
import androidx.navigation.NavController
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

            Text("Jogador", style = MaterialTheme.typography.headlineSmall)

            HorizontalDivider()

            OutlinedButton(
                onClick = { viewModel.abrirEditor() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Avatar")
            }
        }
    }
}
