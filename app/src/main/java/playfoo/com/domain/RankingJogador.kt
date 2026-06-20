package playfoo.com.domain

data class RankingJogador(
    val id: String = "",
    val nome: String = "",
    val vitorias: Int = 0,
    val partidas: Int = 0,
    val taxaVitoria: Float = 0f
)