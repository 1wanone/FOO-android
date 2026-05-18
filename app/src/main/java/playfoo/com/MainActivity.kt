package playfoo.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import playfoo.com.ui.dashboard.DashboardScreen
import playfoo.com.ui.game.GameScreen
import playfoo.com.ui.menu.MenuScreen
import playfoo.com.ui.multiplayer.MultiplayerScreen
import playfoo.com.ui.perfil.PerfilScreen
import playfoo.com.ui.selecionar.SelecionarTemaScreen
import playfoo.com.ui.theme.FOOmobileTheme
import playfoo.com.ui.turma.TurmaScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FOOmobileTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    ForcaNavHost()
                }
            }
        }
    }
}

@Composable
fun ForcaNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") { MenuScreen(navController) }
        composable("selecionar_tema") { SelecionarTemaScreen(navController) }
        composable("jogo/{temaId}/{dificuldade}") {
            GameScreen(onVoltar = { navController.navigateUp() })
        }
        composable("multiplayer") { MultiplayerScreen(navController) }
        composable("turmas") { TurmaScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("perfil") { PerfilScreen(navController) }
    }
}
