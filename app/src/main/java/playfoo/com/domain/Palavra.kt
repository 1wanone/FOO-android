package playfoo.com.domain

import java.text.Normalizer

// Retorna a letra base sem acento: Ã→A, Ç→C, É→E etc.
private fun normalizarLetra(c: Char): Char =
    Normalizer.normalize(c.toString(), Normalizer.Form.NFD)[0]

data class Palavra(val texto: String) {

    fun contemLetra(letra: Char): Boolean =
        texto.uppercase().any { normalizarLetra(it) == normalizarLetra(letra) }

    fun todasLetrasReveladas(letrasCorretas: Set<Char>): Boolean {
        val corrNorm = letrasCorretas.map { normalizarLetra(it) }.toSet()
        return texto.uppercase().filter { it.isLetter() }.all { normalizarLetra(it) in corrNorm }
    }

    fun mostraProgresso(letrasCorretas: Set<Char>): String {
        val corrNorm = letrasCorretas.map { normalizarLetra(it) }.toSet()
        return texto.uppercase().map { char ->
            when {
                !char.isLetter()                -> char
                normalizarLetra(char) in corrNorm -> char
                else                            -> '_'
            }
        }.joinToString(" ")
    }
}
