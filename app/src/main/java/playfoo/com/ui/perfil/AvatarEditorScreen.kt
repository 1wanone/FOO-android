package playfoo.com.ui.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.perfil.components.SeletorTomDePele

@Composable
fun AvatarEditorScreen(
    avatarAtual: AvatarConfig = AvatarConfig(),
    onSalvar: (AvatarConfig) -> Unit = {}
) {
    var config by remember { mutableStateOf(avatarAtual) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Personalizar Avatar", style = MaterialTheme.typography.headlineMedium)

        AvatarView(
            config = config,
            estado = EstadoAvatar.NEUTRO,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        HorizontalDivider()

        Text("Tom de pele", style = MaterialTheme.typography.titleMedium)
        SeletorTomDePele(
            tomSelecionado = config.tonDePele,
            onSelecionar = { config = config.copy(tonDePele = it) }
        )

        Text("Cabelo", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("curto", "liso", "cacheado", "raspado").forEach { cab ->
                FilterChip(
                    selected = config.cabelo == cab,
                    onClick = { config = config.copy(cabelo = cab) },
                    label = { Text(cab) }
                )
            }
        }

        Text("Cor do cabelo", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("preto", "castanho", "loiro", "ruivo").forEach { cor ->
                FilterChip(
                    selected = config.corCabelo == cor,
                    onClick = { config = config.copy(corCabelo = cor) },
                    label = { Text(cor) }
                )
            }
        }

        Text("Camisa", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("maniva", "divas").forEach { cam ->
                FilterChip(
                    selected = config.camisa == cam,
                    onClick = { config = config.copy(camisa = cam) },
                    label = { Text(cam) }
                )
            }
        }

        HorizontalDivider()

        Button(
            onClick = { onSalvar(config) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Avatar")
        }
    }
}