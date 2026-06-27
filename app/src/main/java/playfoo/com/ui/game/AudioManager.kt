package playfoo.com.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import playfoo.com.R

val LocalAudioManager = compositionLocalOf<AudioManager?> { null }

class AudioManager(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val idClick   = soundPool.load(context, R.raw.click_keyboard, 1)
    private val idCorrect = soundPool.load(context, R.raw.correct, 1)
    private val idErro    = soundPool.load(context, R.raw.erro, 1)
    private val idVictory = soundPool.load(context, R.raw.victory, 1)
    private val idDerrota = soundPool.load(context, R.raw.derrota, 1)

    private var mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.background_music)
        .apply {
            isLooping = true
            setVolume(1.0f, 1.0f)
        }

    fun iniciarMusica() {
        if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()
    }

    fun pausarMusica() {
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
    }

    fun playClick()   = soundPool.play(idClick,   0.6f, 0.6f, 1, 0, 1f)
    fun playCorrect() = soundPool.play(idCorrect, 1f,   1f,   1, 0, 1f)
    fun playErro()    = soundPool.play(idErro,    1f,   1f,   1, 0, 1f)
    fun playVictory() = soundPool.play(idVictory, 1f,   1f,   1, 0, 1f)
    fun playDerrota() = soundPool.play(idDerrota, 1f,   1f,   1, 0, 1f)

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool.release()
    }
}

@Composable
fun rememberAudioManager(): AudioManager {
    val context = LocalContext.current
    val audio = remember { AudioManager(context) }
    DisposableEffect(Unit) {
        audio.iniciarMusica()
        onDispose { audio.release() }
    }
    return audio
}
