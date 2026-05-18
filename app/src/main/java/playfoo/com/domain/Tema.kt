package playfoo.com.domain

data class Tema(
    val id: Int = 0,
    val nome: String = "",
    val palavras: List<Palavra> = emptyList()
)
