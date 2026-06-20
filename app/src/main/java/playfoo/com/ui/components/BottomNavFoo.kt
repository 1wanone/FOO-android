package playfoo.com.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import playfoo.com.ui.theme.*

@Composable
fun BottomNavFoo(
    currentRoute: String,
    onInicio: () -> Unit,
    onTurma: () -> Unit,
    onPerfil: () -> Unit,
    onOpcoes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor      = RoxoMedio,
        selectedIconColor   = Rosa,
        selectedTextColor   = Rosa,
        unselectedIconColor = AzulCinza,
        unselectedTextColor = AzulCinza
    )

    NavigationBar(
        modifier       = modifier,
        containerColor = RoxoEscuro,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "menu",
            onClick  = onInicio,
            icon     = { Icon(Icons.Rounded.Home, contentDescription = "Início") },
            label    = { Text("Início") },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = currentRoute == "turmas",
            onClick  = onTurma,
            icon     = { Icon(Icons.Rounded.School, contentDescription = "Turma") },
            label    = { Text("Turma") },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = currentRoute == "perfil",
            onClick  = onPerfil,
            icon     = { Icon(Icons.Rounded.Person, contentDescription = "Perfil") },
            label    = { Text("Perfil") },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = currentRoute == "opcoes",
            onClick  = onOpcoes,
            icon     = { Icon(Icons.Rounded.Settings, contentDescription = "Opções") },
            label    = { Text("Opções") },
            colors   = itemColors
        )
    }
}