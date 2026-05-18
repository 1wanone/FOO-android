package playfoo.com.domain

enum class Dificuldade(val tentativasMaximas: Int, val tempoSegundos: Int?) {
    FACIL(8, null),
    NORMAL(6, 120),
    DIFICIL(4, 60)
}
