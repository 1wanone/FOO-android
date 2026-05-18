package playfoo.com.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MenuScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Jogo da Forca")
        Button(onClick = { navController.navigate("jogo") }) { Text("Jogar") }
        Button(onClick = { navController.navigate("multiplayer") }) { Text("Multiplayer") }
        Button(onClick = { navController.navigate("turmas") }) { Text("Turmas") }
        Button(onClick = { navController.navigate("dashboard") }) { Text("Dashboard") }
        Button(onClick = { navController.navigate("perfil") }) { Text("Perfil") }
    }
}
