package playfoo.com.domain

data class Palavra(val texto: String) {
    fun contemLetra(letra: Char): Boolean =
        texto.uppercase().contains(letra)

    fun todasLetrasReveladas(letrasCorretas: Set<Char>): Boolean =
        texto.uppercase().filter { it.isLetter() }.all { it in letrasCorretas }

    fun mostraProgresso(letrasCorretas: Set<Char>): String =
        texto.uppercase().map { char ->
            when {
                !char.isLetter() -> char
                char in letrasCorretas -> char
                else -> '_'
            }
        }.joinToString(" ")
}
