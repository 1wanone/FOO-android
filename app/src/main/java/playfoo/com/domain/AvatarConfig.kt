package playfoo.com.domain

import androidx.compose.ui.graphics.Color

data class AvatarConfig(
    val tonDePele : String = "pele_parda",
    val cabelo    : String = "curto",
    val corCabelo : String = "preto",
    val camisa    : String = "maniva",
    val corCamisa : String = "#1B4E2D"   // hex "#RRGGBB"
)

fun AvatarConfig.corCamisaColor(): Color = parseHexCor(corCamisa)

fun parseHexCor(hex: String): Color = try {
    val c = if (hex.startsWith("#")) hex else "#$hex"
    Color(android.graphics.Color.parseColor(c))
} catch (_: Exception) {
    Color(0xFF1B4E2D)
}

fun Color.toHexCor(): String {
    val r = (red   * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue  * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}
