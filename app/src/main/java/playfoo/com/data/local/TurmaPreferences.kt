package playfoo.com.data.local

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurmaPreferences @Inject constructor(
    private val prefs: SharedPreferences
) {
    fun getCodigos(): Set<String> =
        prefs.getStringSet("turmas_codigos", emptySet()) ?: emptySet()

    fun entrar(codigo: String) {
        val atual = getCodigos().toMutableSet()
        atual.add(codigo.uppercase())
        prefs.edit().putStringSet("turmas_codigos", atual).apply()
    }

    fun sair(codigo: String) {
        val atual = getCodigos().toMutableSet()
        atual.remove(codigo.uppercase())
        prefs.edit().putStringSet("turmas_codigos", atual).apply()
    }
}
