package playfoo.com.domain

data class Partida(
    val id: String = "",
    val tema: Tema,
    val palavra: Palavra,
    val jogador: Jogador,
    val dificuldade: Dificuldade,
    private var tentativasRestantes: Int = dificuldade.tentativasMaximas,
    private val letrasErradas: MutableSet<Char> = mutableSetOf(),
    private val letrasCorretas: MutableSet<Char> = mutableSetOf()
) {
    // Retorna true se acertou, false se errou. Ignora letra já tentada.
    fun tentativa(letra: Char): Boolean {
        val letraUpper = letra.uppercaseChar()
        if (letraUpper in letrasCorretas) return true
        if (letraUpper in letrasErradas) return false
        return if (palavra.contemLetra(letraUpper)) {
            letrasCorretas.add(letraUpper)
            true
        } else {
            letrasErradas.add(letraUpper)
            tentativasRestantes--
            false
        }
    }

    // Chamado quando o timer expira para forçar estado de derrota.
    fun forcarDerrota() {
        tentativasRestantes = 0
    }

    fun venceu(): Boolean = palavra.todasLetrasReveladas(letrasCorretas)
    fun terminou(): Boolean = venceu() || tentativasRestantes <= 0
    fun getProgresso(): String = palavra.mostraProgresso(letrasCorretas)
    fun getTentativasRestantes(): Int = tentativasRestantes
    fun getLetrasErradas(): Set<Char> = letrasErradas.toSet()
    fun getLetrasCorretas(): Set<Char> = letrasCorretas.toSet()
}
