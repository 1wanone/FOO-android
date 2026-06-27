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
import playfoo.com.domain.corCamisaColor
import playfoo.com.domain.parseHexCor
import playfoo.com.domain.toHexCor
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.FooIcone
import playfoo.com.ui.components.FooIcones
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.ui.perfil.components.RodaDeCores
import playfoo.com.ui.theme.*

private val TONS_DE_PELE = listOf(
    "pele_branca" to Color(0xFFFFF2E0),
    "base_rosa"   to Color(0xFFFFD8B8),
    "pele_parda"  to Color(0xFFD4975A),
    "pele_negra"  to Color(0xFF9B5C30)
)

private val CORES_CABELO = listOf(
    "preto"    to Color(0xFF1A1A1A),
    "castanho" to Color(0xFF5C2E0E),
    "loiro"    to Color(0xFFDDB836),
    "ruivo"    to Color(0xFFCC3D00)
)

private val ESTILOS_CABELO = listOf("curto", "liso", "afro", "fade")

private val MODELOS_CAMISA = listOf(
    "maniva"        to "Maniva Lab",
    "divas"         to "Divas Digitais",
    "personalizada" to "Personalizada"
)

private enum class AbaEditor { PELE, CABELO, CAMISA }

@Composable
fun AvatarEditorScreen(
    avatarAtual: AvatarConfig = AvatarConfig(),
    onSalvar: (AvatarConfig) -> Unit = {}
) {
    var config by remember { mutableStateOf(avatarAtual) }
    var abaAtiva by remember { mutableStateOf(AbaEditor.PELE) }
    var mostrarConfirmacaoSalvar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoxoEscuro)
    ) {
        // ── Título ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "Personalizar Avatar",
                style      = MaterialTheme.typography.titleLarge,
                color      = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // ── Preview fixo do avatar ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(FundoCard),
            contentAlignment = Alignment.Center
        ) {
            AvatarView(
                config   = config,
                estado   = EstadoAvatar.NEUTRO,
                modifier = Modifier
                    .fillMaxHeight(0.95f)
                    .aspectRatio(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Abas de categoria ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AbaEditor.entries.forEach { aba ->
                val label = when (aba) {
                    AbaEditor.PELE   -> "Tom de pele"
                    AbaEditor.CABELO -> "Cabelo"
                    AbaEditor.CAMISA -> "Camisa"
                }
                val selecionada = aba == abaAtiva
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selecionada) Pink else AzulCinza.copy(alpha = 0.2f))
                        .clickable { abaAtiva = aba }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = label,
                        fontSize   = 12.sp,
                        fontWeight = if (selecionada) FontWeight.ExtraBold else FontWeight.Normal,
                        color      = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Painel de opções (rola, mas avatar fica fixo acima) ─────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (abaAtiva) {

                // ── ABA: TOM DE PELE ────────────────────────────────────
                AbaEditor.PELE -> {
                    SecaoLabel("Escolha o tom de pele")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TONS_DE_PELE.forEach { (tom, cor) ->
                            val selecionado = config.tonDePele == tom
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
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
                }

                // ── ABA: CABELO ─────────────────────────────────────────
                AbaEditor.CABELO -> {
                    SecaoLabel("Estilo")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ESTILOS_CABELO) { estilo ->
                            val selecionado = config.cabelo == estilo
                            FilterChip(
                                selected = selecionado,
                                onClick  = { config = config.copy(cabelo = estilo) },
                                label    = {
                                    Text(
                                        text       = estilo.replaceFirstChar { it.uppercase() },
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

                    SecaoLabel("Cor do cabelo")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CORES_CABELO.forEach { (cor, corVisual) ->
                            val selecionado = config.corCabelo == cor
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
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
                }

                // ── ABA: CAMISA ─────────────────────────────────────────
                AbaEditor.CAMISA -> {
                    SecaoLabel("Modelo")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(MODELOS_CAMISA) { (modelo, nome) ->
                            val selecionado = config.camisa == modelo
                            FilterChip(
                                selected = selecionado,
                                onClick  = { config = config.copy(camisa = modelo) },
                                label    = {
                                    Text(
                                        text       = nome,
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

                    if (config.camisa == "personalizada") {
                        SecaoLabel("Cor")
                        RodaDeCores(
                            corSelecionada   = config.corCamisaColor(),
                            onCorSelecionada = { nova -> config = config.copy(corCamisa = nova.toHexCor()) },
                            modifier         = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── Botão Salvar (sempre visível no rodapé) ─────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BotaoCartoon(
                texto    = "Salvar Avatar",
                onClick  = { mostrarConfirmacaoSalvar = true },
                tipo     = BotaoCartoonTipo.PRIMARIO,
                modifier = Modifier.fillMaxWidth(),
                altura   = 52.dp
            )
        }
    }

    if (mostrarConfirmacaoSalvar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoSalvar = false },
            containerColor   = FundoCard,
            title = {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(8.dp)
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
        style      = MaterialTheme.typography.titleSmall,
        color      = Color.White.copy(alpha = 0.7f),
        fontWeight = FontWeight.Bold
    )
}
