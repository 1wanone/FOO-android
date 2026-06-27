package playfoo.com.domain

data class Turma(
    val id: String = "",
    val nome: String = "",
    val codigo: String = "",
    val gestor: JogadorGestor = JogadorGestor(),
    val membros: List<JogadorAluno> = emptyList()
)
