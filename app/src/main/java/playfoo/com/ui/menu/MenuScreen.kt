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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import playfoo.com.domain.TipoUsuario
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.BottomNavBar
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.NavItem
import playfoo.com.ui.components.TipoFundo
import playfoo.com.ui.game.components.AvatarView
import playfoo.com.ui.game.components.EstadoAvatar
import playfoo.com.viewmodel.MenuViewModel

private val navItems = listOf(
    NavItem("🏠", "Início", "menu"),
    NavItem("🎮", "Jogar", "selecionar_tema"),
    NavItem("⚔️", "Multi", "multiplayer"),
    NavItem("👤", "Perfil", "perfil")
)

@Composable
fun MenuScreen(
    navController: NavController,
    tipo: String = TipoUsuario.ALUNO,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val auth = remember { FirebaseAuth.getInstance() }
    val nomeUsuario = auth.currentUser?.displayName
        ?: auth.currentUser?.email?.substringBefore('@')
        ?: "Jogador"

    // Recarrega avatar toda vez que a tela volta ao foco
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.recarregar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    FundoTela(tipo = TipoFundo.MENU) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
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
                    text = "mobile",
                    color = Color(0xFF6C63FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                Spacer(Modifier.height(20.dp))

                // Avatar em destaque — carregado do ViewModel
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF6C63FF), CircleShape)
                        .background(Color(0xFF1A1A2E)),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarView(
                        config = uiState.avatarConfig,
                        estado = EstadoAvatar.NEUTRO,
                        modifier = Modifier
                            .fillMaxSize(0.95f)
                            .offset(y = 8.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = nomeUsuario,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFF6C63FF).copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (tipo == TipoUsuario.GESTOR) "⭐ PROFESSOR" else "🎮 JOGADOR",
                        color = Color(0xFF6C63FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(28.dp))

                // Botão JOGAR principal
                BotaoCartoon(
                    texto = "▶  JOGAR",
                    onClick = { navController.navigate("selecionar_tema") },
                    modifier = Modifier.fillMaxWidth(),
                    tipo = BotaoCartoonTipo.PRIMARIO,
                    altura = 72.dp,
                    fontSize = 22.sp
                )

                Spacer(Modifier.height(16.dp))

                // Botões secundários: MULTI | TURMAS | PERFIL (ou DASH)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BotaoSecundarioMenu(
                        emoji = "⚔️",
                        texto = "MULTI",
                        cor = Color(0xFF9C27B0),
                        onClick = { navController.navigate("multiplayer") },
                        modifier = Modifier.weight(1f)
                    )
                    BotaoSecundarioMenu(
                        emoji = "🏫",
                        texto = "TURMAS",
                        cor = Color(0xFF00BCD4),
                        onClick = { navController.navigate("turmas") },
                        modifier = Modifier.weight(1f)
                    )
                    if (tipo == TipoUsuario.GESTOR) {
                        BotaoSecundarioMenu(
                            emoji = "📊",
                            texto = "DASH",
                            cor = Color(0xFFFF9800),
                            onClick = { navController.navigate("turmas") },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        BotaoSecundarioMenu(
                            emoji = "👤",
                            texto = "PERFIL",
                            cor = Color(0xFF4CAF50),
                            onClick = { navController.navigate("perfil") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            // Barra de navegação inferior fixa
            BottomNavBar(
                rotaAtual = "menu",
                items = navItems,
                onNavegar = { rota ->
                    if (rota != "menu") navController.navigate(rota)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun BotaoSecundarioMenu(
    emoji: String,
    texto: String,
    cor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(cor.copy(alpha = 0.9f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = texto,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
