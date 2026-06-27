package playfoo.com.ui.opcoes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import playfoo.com.ui.components.*
import playfoo.com.ui.game.LocalAudioManager
import playfoo.com.ui.theme.*
import playfoo.com.viewmodel.OpcoesViewModel

@Composable
fun OpcoesScreen(
    navController: NavController,
    viewModel: OpcoesViewModel = hiltViewModel()
) {
    val altoContraste   by viewModel.altoContraste.collectAsState()
    val efeitosSonoros  by viewModel.efeitosSonoros.collectAsState()
    val musicaFundo     by viewModel.musicaFundo.collectAsState()
    val audio = LocalAudioManager.current
    var mostrarConfirmacaoLogout by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        FundoAnimado()
        Box(Modifier.fillMaxSize().background(FundoOverlay))

        Column(Modifier.fillMaxSize().padding(bottom = 80.dp)) {
            HeaderFoo("Opções", onVoltar = { navController.navigateUp() })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecaoLabel("ACESSIBILIDADE")
                CardCartoon(modifier = Modifier.fillMaxWidth()) {
                    OpcaoToggle(
                        titulo     = "Alto contraste",
                        descricao  = "Aumenta o contraste dos textos",
                        checked    = altoContraste,
                        onToggle   = { viewModel.toggleAltoContraste() },
                        habilitado = false,
                        labelExtra = "Em breve"
                    )
                }

                SecaoLabel("SOM")
                CardCartoon(modifier = Modifier.fillMaxWidth()) {
                    OpcaoToggle(
                        titulo    = "Efeitos sonoros",
                        descricao = "Sons ao acertar e errar letras",
                        checked   = efeitosSonoros,
                        onToggle  = { audio?.atualizarEfeitos(viewModel.toggleEfeitosSonoros()) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                    OpcaoToggle(
                        titulo    = "Música de fundo",
                        descricao = "Trilha sonora do jogo",
                        checked   = musicaFundo,
                        onToggle  = { audio?.atualizarMusica(viewModel.toggleMusicaFundo()) }
                    )
                }

                SecaoLabel("CONTA")
                CardCartoon(modifier = Modifier.fillMaxWidth()) {
                    BotaoCartoon(
                        texto    = "SAIR DA CONTA",
                        icone    = FooIcones.Sair,
                        onClick  = { mostrarConfirmacaoLogout = true },
                        tipo     = BotaoCartoonTipo.PERIGO,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        BottomNavFoo(
            currentRoute = "opcoes",
            onInicio     = { navController.navigate("menu") { popUpTo("menu") { inclusive = false } } },
            onTurma      = { navController.navigate("turmas") },
            onPerfil     = { navController.navigate("perfil") },
            onOpcoes     = {},
            modifier     = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (mostrarConfirmacaoLogout) {
        Dialog(onDismissRequest = { mostrarConfirmacaoLogout = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(FundoCard)
                    .border(2.dp, ErroVermelho, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FooIcone(icone = FooIcones.Sair, cor = ErroVermelho, tamanho = 48.dp)
                Text(
                    text       = "Sair da conta?",
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp
                )
                Text(
                    text     = "Você precisará fazer login novamente para acessar o aplicativo.",
                    color    = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                BotaoCartoon(
                    texto    = "Cancelar",
                    onClick  = { mostrarConfirmacaoLogout = false },
                    tipo     = BotaoCartoonTipo.NEUTRO,
                    modifier = Modifier.fillMaxWidth()
                )
                BotaoCartoon(
                    texto    = "SAIR",
                    icone    = FooIcones.Sair,
                    onClick  = {
                        mostrarConfirmacaoLogout = false
                        viewModel.sair()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    tipo     = BotaoCartoonTipo.PERIGO,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SecaoLabel(texto: String) {
    Text(
        text          = texto,
        color         = TextoSecundario,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.ExtraBold,
        letterSpacing = 2.sp
    )
}

@Composable
private fun OpcaoToggle(
    titulo: String,
    descricao: String,
    checked: Boolean,
    onToggle: () -> Unit,
    habilitado: Boolean = true,
    labelExtra: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = titulo,
                    color      = if (habilitado) Color.White else TextoDesabilitado,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
                if (labelExtra != null) {
                    Box(
                        modifier = Modifier
                            .background(AzulCinza.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(labelExtra, color = AzulCinza, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text  = descricao,
                color = if (habilitado) TextoSecundario else TextoDesabilitado,
                fontSize = 12.sp
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = { if (habilitado) onToggle() },
            enabled         = habilitado,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = Rosa,
                uncheckedTrackColor = AzulCinza.copy(alpha = 0.3f)
            )
        )
    }
}