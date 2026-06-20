package playfoo.com.ui.selecionar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import playfoo.com.domain.Dificuldade
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.FooIcone
import playfoo.com.ui.components.FooIcones
import playfoo.com.ui.components.FundoAnimado
import playfoo.com.ui.components.HeaderFoo
import playfoo.com.ui.theme.*
import playfoo.com.viewmodel.SelecionarTemaViewModel

@Composable
fun SelecionarTemaScreen(
    navController: NavController,
    viewModel: SelecionarTemaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var temaIdSelecionado by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        FundoAnimado()
        Box(Modifier.fillMaxSize().background(FundoOverlay))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderFoo("ESCOLHA O TEMA", onVoltar = { navController.navigateUp() })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(Modifier.height(8.dp))

            // Grid 2 colunas de temas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.temas, key = { it.id }) { tema ->
                    CardTema(
                        nome          = tema.nome,
                        quantPalavras = tema.palavras.size,
                        selecionado   = temaIdSelecionado == tema.id,
                        onClick       = { temaIdSelecionado = tema.id },
                        modifier      = Modifier.fillMaxWidth().aspectRatio(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "DIFICULDADE",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(8.dp))

            // Botões de dificuldade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotaoDificuldade(
                    icone       = FooIcones.Facil,
                    corIcone    = RoxoEscuro,
                    texto       = "FÁCIL",
                    cor         = Ciano,
                    corTexto    = RoxoEscuro,
                    selecionado = uiState.dificuldade == Dificuldade.FACIL,
                    onClick     = { viewModel.selecionarDificuldade(Dificuldade.FACIL) },
                    modifier    = Modifier.weight(1f)
                )
                BotaoDificuldade(
                    icone       = FooIcones.Normal,
                    corIcone    = Color.White,
                    texto       = "NORMAL",
                    cor         = Rosa,
                    corTexto    = Color.White,
                    selecionado = uiState.dificuldade == Dificuldade.NORMAL,
                    onClick     = { viewModel.selecionarDificuldade(Dificuldade.NORMAL) },
                    modifier    = Modifier.weight(1f)
                )
                BotaoDificuldade(
                    icone       = FooIcones.Dificil,
                    corIcone    = Color.White,
                    texto       = "DIFÍCIL",
                    cor         = ErroVermelho,
                    corTexto    = Color.White,
                    selecionado = uiState.dificuldade == Dificuldade.DIFICIL,
                    onClick     = { viewModel.selecionarDificuldade(Dificuldade.DIFICIL) },
                    modifier    = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            BotaoCartoon(
                texto      = "JOGAR",
                icone      = FooIcones.Jogar,
                onClick    = {
                    temaIdSelecionado?.let { id ->
                        navController.navigate("jogo/$id/${uiState.dificuldade.name}")
                    }
                },
                modifier   = Modifier.fillMaxWidth(),
                tipo       = BotaoCartoonTipo.PRIMARIO,
                habilitado = temaIdSelecionado != null,
                altura     = 64.dp,
                fontSize   = 20.sp
            )

            Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CardTema(
    nome: String,
    quantPalavras: Int,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selecionado) 1.04f else 1f,
        label = "scale_tema"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(if (selecionado) 12.dp else 4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (selecionado) 3.dp else 2.dp,
                color = Rosa,
                shape = RoundedCornerShape(16.dp)
            )
            .background(RoxoMedio)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = nome,
                color      = Color.White,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                maxLines   = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "$quantPalavras palavras",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun BotaoDificuldade(
    icone: ImageVector,
    corIcone: Color,
    texto: String,
    cor: Color,
    corTexto: Color = Color.White,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selecionado) 1.05f else 1f,
        label = "scale_dif"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(64.dp)
            .shadow(if (selecionado) 8.dp else 3.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (selecionado) cor else cor.copy(alpha = 0.6f))
            .then(
                if (selecionado) Modifier.border(3.dp, Color.White, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            FooIcone(icone = icone, cor = corIcone, tamanho = 22.dp)
            Text(text = texto, color = corTexto, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}