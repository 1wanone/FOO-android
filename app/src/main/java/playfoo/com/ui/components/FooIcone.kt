package playfoo.com.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FooIcones {
    // Navegação e ações
    val Jogar          : ImageVector = Icons.Rounded.PlayArrow
    val Voltar         : ImageVector = Icons.Rounded.ArrowBack
    val Adicionar      : ImageVector = Icons.Rounded.Add
    val Sair           : ImageVector = Icons.Rounded.Logout
    val Inicio         : ImageVector = Icons.Rounded.Home

    // Seções principais
    val Multi          : ImageVector = Icons.Rounded.Gamepad
    val Turmas         : ImageVector = Icons.Rounded.School
    val Perfil         : ImageVector = Icons.Rounded.Person
    val Turma          : ImageVector = Icons.Rounded.Group
    val Grafico        : ImageVector = Icons.Rounded.BarChart
    val Dashboard      : ImageVector = Icons.Rounded.BarChart

    // Temas de Java
    val Jogador        : ImageVector = Icons.Rounded.SportsEsports
    val Excecao        : ImageVector = Icons.Rounded.Warning
    val Heranca        : ImageVector = Icons.Rounded.DeviceHub
    val Polimorfismo   : ImageVector = Icons.Rounded.Autorenew
    val Encapsulamento : ImageVector = Icons.Rounded.Lock
    val Introducao     : ImageVector = Icons.Rounded.Book
    val Relacionamento : ImageVector = Icons.Rounded.Share
    val Codigo         : ImageVector = Icons.Rounded.Code
    val InterfaceGraf  : ImageVector = Icons.Rounded.DesktopWindows
    val Interface      : ImageVector = Icons.Rounded.Handshake
    val Colecao        : ImageVector = Icons.Rounded.Inventory2

    // Dificuldade
    val Facil          : ImageVector = Icons.Rounded.Mood
    val Normal         : ImageVector = Icons.Rounded.SentimentNeutral
    val Dificil        : ImageVector = Icons.Rounded.MoodBad

    // Estatísticas e perfil
    val Estrela        : ImageVector = Icons.Rounded.Star
    val Trofeu         : ImageVector = Icons.Rounded.EmojiEvents
    val Avatar         : ImageVector = Icons.Rounded.Face
    val Coracao        : ImageVector = Icons.Rounded.Favorite
    val Derrota        : ImageVector = Icons.Rounded.FavoriteBorder
    val Tempo          : ImageVector = Icons.Rounded.Timer
    val Caveira        : ImageVector = Icons.Rounded.SportsMartialArts
    val Meta           : ImageVector = Icons.Rounded.TrackChanges
    val Opcoes         : ImageVector = Icons.Rounded.Settings

    // Ações extras
    val Editar         : ImageVector = Icons.Rounded.Edit
    val Compartilhar   : ImageVector = Icons.Rounded.Share
    val AdicionarPessoa: ImageVector = Icons.Rounded.PersonAdd
    val RemoverPessoa  : ImageVector = Icons.Rounded.PersonRemove
    val Copiar         : ImageVector = Icons.Rounded.ContentCopy
    val Deletar        : ImageVector = Icons.Rounded.Delete
    val Aviso          : ImageVector = Icons.Rounded.Warning
}

@Composable
fun FooIcone(
    icone: ImageVector,
    modifier: Modifier = Modifier,
    cor: Color = Color.White,
    tamanho: Dp = 24.dp
) {
    Icon(
        imageVector = icone,
        contentDescription = null,
        tint = cor,
        modifier = modifier.size(tamanho)
    )
}
