package playfoo.com.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FooColorScheme = darkColorScheme(
    primary = Rosa, onPrimary = Color.White,
    primaryContainer = RoxoMedio, onPrimaryContainer = TextoPrimario,
    secondary = Pink, onSecondary = Color.White,
    secondaryContainer = RoxoEscuroSurface, onSecondaryContainer = TextoSecundario,
    tertiary = Ciano, onTertiary = RoxoEscuro,
    background = RoxoEscuro, onBackground = TextoPrimario,
    surface = RoxoMedio, onSurface = TextoPrimario,
    surfaceVariant = RoxoEscuroSurface, onSurfaceVariant = TextoSecundario,
    error = ErroVermelho, onError = Color.White,
    outline = AzulCinza, outlineVariant = AzulCinza.copy(alpha = 0.4f)
)

@Composable
fun FOOmobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FooColorScheme, typography = Typography, content = content)
}
