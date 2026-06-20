package playfoo.com.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import playfoo.com.R
import androidx.compose.foundation.Image

@Composable
fun FundoAnimado(modifier: Modifier = Modifier) {
    Image(
        painter      = painterResource(R.drawable.fundo_jogo),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier     = modifier.fillMaxSize()
    )
}