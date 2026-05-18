package playfoo.com.data.local

import android.content.SharedPreferences
import playfoo.com.domain.JogadorAluno
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JogadorPreferences @Inject constructor(
    private val prefs: SharedPreferences
) {
    fun load(): JogadorAluno = JogadorAluno(
        id            = "1",
        nome          = prefs.getString("jogador_nome", "Jogador") ?: "Jogador",
        totalPartidas = prefs.getInt("total_partidas", 0),
        totalVitorias = prefs.getInt("total_vitorias", 0),
        totalDerrotas = prefs.getInt("total_derrotas", 0)
    )

    fun registrarVitoria() {
        val j = load()
        prefs.edit()
            .putInt("total_partidas", j.totalPartidas + 1)
            .putInt("total_vitorias", j.totalVitorias + 1)
            .apply()
    }

    fun registrarDerrota() {
        val j = load()
        prefs.edit()
            .putInt("total_partidas", j.totalPartidas + 1)
            .putInt("total_derrotas", j.totalDerrotas + 1)
            .apply()
    }
}
