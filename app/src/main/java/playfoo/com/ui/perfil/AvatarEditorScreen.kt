package playfoo.com.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import playfoo.com.domain.AvatarConfig
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.FooIcone
import playfoo.com.ui.components.FooIcones
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.theme.*

private val TONS_DE_PELE = listOf(
    "pele_branca" to Color(0xFFFFDBAC),
    "base_rosa"   to Color(0xFFF4A986),
    "pele_parda"  to Color(0xFFC68642),
    "pele_negra"  to Color(0xFF5C3317)
)

private val CORES_CABELO = listOf(
    "preto"    to Color(0xFF1A1A1A),
    "castanho" to Color(0xFF6B3A2A),
    "loiro"    to Color(0xFFE8C84A),
    "ruivo"    to Color(0xFFB5390A)
)

private val ESTILOS_CABELO = listOf("curto", "liso", "afro", "fade")

private val CAMISAS = listOf(
    Triple("maniva", Color(0xFF1B4E2D), "Maniva Lab"),
    Triple("divas",  Color(0xFF4A1080), "Divas Digitais")
)

@Composable
fun AvatarEditorScreen(
    avatarAtual: AvatarConfig = AvatarConfig(),
    onSalvar: (AvatarConfig) -> Unit = {}
) {
    var config by remember { mutableStateOf(avatarAtual) }
    var mostrarConfirmacaoSalvar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoxoEscuro)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text  = "Personalizar Avatar",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        // Preview do avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(FundoCard),
            contentAlignment = Alignment.Center
        ) {
            AvatarView(
                config   = config,
                estado   = EstadoAvatar.NEUTRO,
                modifier = Modifier.fillMaxHeight(0.95f).aspectRatio(1f)
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        // ── Tom de pele ───────────────────────────────────────────────
        SecaoLabel("Tom de pele")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TONS_DE_PELE.forEach { (tom, cor) ->
                val selecionado = config.tonDePele == tom
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(cor)
                        .then(
                            if (selecionado)
                                Modifier
                                    .border(3.dp, Color.White, CircleShape)
                                    .border(5.dp, Color(0xFF333333), CircleShape)
                            else
                                Modifier.border(2.dp, Color(0x44000000), CircleShape)
                        )
                        .clickable { config = config.copy(tonDePele = tom) }
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        // ── Estilo de cabelo ──────────────────────────────────────────
        SecaoLabel("Estilo de cabelo")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ESTILOS_CABELO) { estilo ->
                val selecionado = config.cabelo == estilo
                FilterChip(
                    selected = selecionado,
                    onClick  = { config = config.copy(cabelo = estilo) },
                    label    = {
                        Text(
                            text = estilo.replaceFirstChar { it.uppercase() },
                            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Pink,
                        selectedLabelColor     = Color.White,
                        containerColor         = AzulCinza.copy(alpha = 0.3f),
                        labelColor             = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        // ── Cor do cabelo ─────────────────────────────────────────────
        SecaoLabel("Cor do cabelo")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CORES_CABELO.forEach { (cor, corVisual) ->
                val selecionado = config.corCabelo == cor
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(corVisual)
                        .then(
                            if (selecionado)
                                Modifier
                                    .border(3.dp, Color.White, CircleShape)
                                    .border(5.dp, Color(0xFF333333), CircleShape)
                            else
                                Modifier.border(2.dp, Color(0x44000000), CircleShape)
                        )
                        .clickable { config = config.copy(corCabelo = cor) }
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

        // ── Camisa ────────────────────────────────────────────────────
        SecaoLabel("Camisa")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CAMISAS.forEach { (camisa, cor, nomeCamisa) ->
                val selecionado = config.camisa == camisa
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cor)
                            .then(
                                if (selecionado)
                                    Modifier.border(3.dp, Pink, RoundedCornerShape(12.dp))
                                else
                                    Modifier.border(2.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                            )
                            .clickable { config = config.copy(camisa = camisa) },
                        contentAlignment = Alignment.Center
                    ) {
                        FooIcone(FooIcones.Avatar, cor = Color.White.copy(alpha = 0.9f), tamanho = 28.dp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = nomeCamisa,
                        fontSize   = 12.sp,
                        color      = if (selecionado) Color.White else Color.White.copy(alpha = 0.6f),
                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        BotaoCartoon(
            texto    = "Salvar Avatar",
            onClick  = { mostrarConfirmacaoSalvar = true },
            tipo     = BotaoCartoonTipo.PRIMARIO,
            modifier = Modifier.fillMaxWidth(),
            altura   = 56.dp
        )

        Spacer(Modifier.height(16.dp))
    }

    if (mostrarConfirmacaoSalvar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoSalvar = false },
            containerColor   = FundoCard,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FooIcone(FooIcones.Avatar, cor = Rosa, tamanho = 22.dp)
                    Text("Salvar avatar?", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text  = "Deseja salvar as alterações no seu avatar?",
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                BotaoCartoon(
                    texto   = "Salvar",
                    onClick = { onSalvar(config); mostrarConfirmacaoSalvar = false },
                    tipo    = BotaoCartoonTipo.PRIMARIO
                )
            },
            dismissButton = {
                BotaoCartoon(
                    texto   = "Cancelar",
                    onClick = { mostrarConfirmacaoSalvar = false },
                    tipo    = BotaoCartoonTipo.NEUTRO
                )
            }
        )
    }
}

@Composable
private fun SecaoLabel(texto: String) {
    Text(
        text       = texto,
        style      = MaterialTheme.typography.titleMedium,
        color      = Color.White,
        fontWeight = FontWeight.Bold
    )
}