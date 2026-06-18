package playfoo.com.data.local

import android.content.SharedPreferences
import playfoo.com.domain.AvatarConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarPreferences @Inject constructor(
    private val prefs: SharedPreferences
) {
    fun load(): AvatarConfig = AvatarConfig(
        tonDePele = prefs.getString("tom_pele", "medio") ?: "medio",
        cabelo    = prefs.getString("cabelo", "curto") ?: "curto",
        corCabelo = prefs.getString("cor_cabelo", "preto") ?: "preto",
        camisa    = prefs.getString("camisa", "maniva") ?: "maniva"
    )

    fun save(config: AvatarConfig) {
        prefs.edit()
            .putString("tom_pele", config.tonDePele)
            .putString("cabelo", config.cabelo)
            .putString("cor_cabelo", config.corCabelo)
            .putString("camisa", config.camisa)
            .apply()
    }
}
