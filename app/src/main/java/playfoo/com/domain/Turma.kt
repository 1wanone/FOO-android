package playfoo.com.domain

data class Turma(
    val id: String = "",
    val nome: String = "",
    val codigo: String = "",
    val gestor: JogadorGestor = JogadorGestor(),
    val membros: List<JogadorAluno> = emptyList()
) {
    fun totalMembros(): Int = membros.size

    fun mediaVitorias(): Float =
        if (membros.isEmpty()) 0f
        else membros.map { it.calcularTaxaVitoria() }.average().toFloat()

    fun rankingMembros(): List<JogadorAluno> =
        membros.sortedByDescending { it.totalVitorias }
}
