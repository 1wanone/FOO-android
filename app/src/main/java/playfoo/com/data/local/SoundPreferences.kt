package playfoo.com.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPreferences @Inject constructor(
    private val prefs: SharedPreferences
) {
    fun getEfeitosSonoros(): Boolean = prefs.getBoolean("efeitos_sonoros", true)
    fun getMusicaFundo(): Boolean    = prefs.getBoolean("musica_fundo", true)

    fun setEfeitosSonoros(value: Boolean) = prefs.edit().putBoolean("efeitos_sonoros", value).apply()
    fun setMusicaFundo(value: Boolean)    = prefs.edit().putBoolean("musica_fundo", value).apply()
}
