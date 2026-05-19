package playfoo.com.domain

class JogoDaForca {
    private var partidaAtual: Partida? = null

    fun iniciarPartida(
        tema: Tema,
        palavra: Palavra,
        jogador: Jogador,
        dificuldade: Dificuldade
    ): Partida {
        partidaAtual = Partida(
            tema = tema,
            palavra = palavra,
            jogador = jogador,
            dificuldade = dificuldade
        )
        return partidaAtual!!
    }

    // Retorna null se não há partida ativa ou se a partida já terminou.
    fun getPartidaAtual(): Partida? = partidaAtual?.takeIf { !it.terminou() }
}
