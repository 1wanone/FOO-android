package playfoo.com.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import playfoo.com.domain.TipoUsuario
import playfoo.com.ui.components.AvatarCirculo
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.BottomNavFoo
import playfoo.com.ui.components.FooIcone
import playfoo.com.ui.components.FooIcones
import playfoo.com.ui.components.FundoAnimado
import playfoo.com.viewmodel.MenuViewModel
import playfoo.com.ui.theme.*

@Composable
fun MenuScreen(
    navController: NavController,
    tipo: String = TipoUsuario.ALUNO,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val auth = remember { FirebaseAuth.getInstance() }
    // detecta convidado pela ausência de sessão Firebase, não pelo argumento de rota
    val isConvidado = auth.currentUser == null
    val nomeUsuario = if (isConvidado) "Convidado"
        else auth.currentUser?.displayName
            ?: auth.currentUser?.email?.substringBefore('@')
            ?: "Jogador"

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.recarregar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FundoAnimado()
        Box(
            Modifier
                .fillMaxSize()
                .background(FundoOverlay)
        )

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(36.dp))

                // Logo
                Text(
                    text = "FOO",
                    color = Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp
                )
                Text(
                    text = "Forca Orientada a objetos",
                    color = Rosa,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(20.dp))

                // Avatar em destaque
                AvatarCirculo(
                    config   = uiState.avatarConfig,
                    tamanho  = 120.dp,
                    bordaCor = Rosa
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = nomeUsuario,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                // Banner de convidado
                if (isConvidado) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AzulCinza.copy(alpha = 0.25f))
                            .border(1.dp, AzulCinza.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text      = "Modo Convidado — crie uma conta para salvar seu progresso",
                            color     = Color.White.copy(alpha = 0.75f),
                            fontSize  = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // Badge de papel
                Box(
                    modifier = Modifier
                        .border(1.5.dp, Rosa, RoundedCornerShape(12.dp))
                        .background(RoxoMedio.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FooIcone(
                            icone   = if (tipo == TipoUsuario.GESTOR) FooIcones.Estrela else FooIcones.Jogador,
                            cor     = Rosa,
                            tamanho = 14.dp
                        )
                        Text(
                            text = if (tipo == TipoUsuario.GESTOR) "PROFESSOR" else "JOGADOR",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Botão JOGAR principal
                BotaoCartoon(
                    texto    = "JOGAR",
                    icone    = FooIcones.Jogar,
                    onClick  = { navController.navigate("selecionar_tema") },
                    modifier = Modifier.fillMaxWidth(),
                    tipo     = BotaoCartoonTipo.PRIMARIO,
                    altura   = 72.dp,
                    fontSize = 22.sp
                )

                Spacer(Modifier.height(16.dp))

                // Botão 2 JOGADORES
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, if (isConvidado) Color.White.copy(alpha = 0.15f) else Rosa, RoundedCornerShape(16.dp))
                        .background(if (isConvidado) RoxoMedio.copy(alpha = 0.5f) else RoxoMedio)
                        .then(if (!isConvidado) Modifier.clickable { navController.navigate("multiplayer") } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FooIcone(FooIcones.Multi, cor = if (isConvidado) Color.White.copy(alpha = 0.25f) else Rosa, tamanho = 20.dp)
                        Text(
                            text          = if (isConvidado) "2 JOGADORES  •  requer conta" else "2 JOGADORES",
                            color         = if (isConvidado) Color.White.copy(alpha = 0.25f) else Rosa,
                            fontWeight    = FontWeight.ExtraBold,
                            fontSize      = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            BottomNavFoo(
                currentRoute = "menu",
                onInicio     = {},
                onTurma      = { navController.navigate("turmas") },
                onPerfil     = { navController.navigate("perfil") },
                onOpcoes     = { navController.navigate("opcoes") },
                modifier     = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}