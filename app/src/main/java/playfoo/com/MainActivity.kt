package playfoo.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import playfoo.com.domain.TipoUsuario
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import playfoo.com.ui.auth.LoginScreen
import playfoo.com.ui.dashboard.DashboardScreen
import playfoo.com.ui.game.AudioManager
import playfoo.com.ui.game.GameScreen
import playfoo.com.ui.game.LocalAudioManager
import playfoo.com.ui.game.rememberAudioManager
import playfoo.com.ui.menu.MenuScreen
import playfoo.com.ui.multiplayer.MultiplayerScreen
import playfoo.com.ui.perfil.PerfilScreen
import playfoo.com.ui.selecionar.SelecionarTemaScreen
import playfoo.com.ui.theme.FOOmobileTheme
import playfoo.com.ui.opcoes.OpcoesScreen
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
    val audio = rememberAudioManager()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> audio.iniciarMusica()
                Lifecycle.Event.ON_PAUSE  -> audio.pausarMusica()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    CompositionLocalProvider(LocalAudioManager provides audio) {
    NavHost(navController = navController, startDestination = "login") {
        composable(
            route = "menu?tipo={tipo}",
            arguments = listOf(navArgument("tipo") { defaultValue = TipoUsuario.ALUNO })
        ) { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: TipoUsuario.ALUNO
            MenuScreen(navController, tipo)
        }
        composable("selecionar_tema") { SelecionarTemaScreen(navController) }
        composable("jogo/{temaId}/{dificuldade}") {
            GameScreen(onVoltar = { navController.navigateUp() })
        }
        composable("multiplayer") {
            MultiplayerScreen(onVoltar = { navController.navigateUp() })
        }
        composable("turmas") {
            TurmaScreen(
                onVoltar = { navController.navigateUp() },
                onNavDashboard = { turmaId -> navController.navigate("dashboard/$turmaId") }
            )
        }
        composable(
            route = "dashboard/{turmaId}",
            arguments = listOf(navArgument("turmaId") { defaultValue = "demo" })
        ) { backStackEntry ->
            val turmaId = backStackEntry.arguments?.getString("turmaId") ?: "demo"
            DashboardScreen(turmaId = turmaId, onVoltar = { navController.navigateUp() })
        }
        composable("perfil") {
            PerfilScreen(
                navController     = navController,
                onGerenciarTurmas = { navController.navigate("turmas") },
                onVerDashboard    = { turmaId -> navController.navigate("dashboard/$turmaId") }
            )
        }
        composable("login") { LoginScreen(navController) }
        composable("opcoes") {
            OpcoesScreen(navController = navController)
        }
    }
    } // CompositionLocalProvider
}