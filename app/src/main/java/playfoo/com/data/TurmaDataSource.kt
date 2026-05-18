package playfoo.com.data

import playfoo.com.domain.JogadorGestor
import playfoo.com.domain.Turma

object TurmaDataSource {
    val turmas: List<Turma> = listOf(
        Turma(id = "1", nome = "POO 2024/1",       codigo = "POO01", gestor = JogadorGestor(nome = "Prof. Silva")),
        Turma(id = "2", nome = "Algoritmos 2024/1", codigo = "ALG01", gestor = JogadorGestor(nome = "Prof. Santos")),
        Turma(id = "3", nome = "Banco de Dados",    codigo = "BD01",  gestor = JogadorGestor(nome = "Prof. Lima")),
        Turma(id = "4", nome = "Redes 2024/2",      codigo = "RED01", gestor = JogadorGestor(nome = "Prof. Costa"))
    )

    fun getByCodigo(codigo: String): Turma? =
        turmas.firstOrNull { it.codigo.equals(codigo.trim(), ignoreCase = true) }
}
