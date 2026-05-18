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
    fun tentativa(letra: Char): Boolean {
        val letraUpper = letra.uppercaseChar()
        return if (palavra.contemLetra(letraUpper)) {
            letrasCorretas.add(letraUpper)
            true
        } else {
            letrasErradas.add(letraUpper)
            tentativasRestantes--
            false
        }
    }

    fun venceu(): Boolean = palavra.todasLetrasReveladas(letrasCorretas)
    fun terminou(): Boolean = venceu() || tentativasRestantes <= 0
    fun getProgresso(): String = palavra.mostraProgresso(letrasCorretas)
    fun getTentativasRestantes(): Int = tentativasRestantes
    fun getLetrasErradas(): Set<Char> = letrasErradas
    fun getLetrasCorretas(): Set<Char> = letrasCorretas
}
