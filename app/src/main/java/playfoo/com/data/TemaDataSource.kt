package playfoo.com.data

import playfoo.com.domain.Palavra
import playfoo.com.domain.Tema

object TemaDataSource {
    val temas: List<Tema> = listOf(
        Tema(
            id = 1, nome = "POO", palavras = listOf(
                Palavra("HERANCA"), Palavra("POLIMORFISMO"), Palavra("ENCAPSULAMENTO"),
                Palavra("CLASSE"), Palavra("OBJETO"), Palavra("INTERFACE"),
                Palavra("METODO"), Palavra("ATRIBUTO"), Palavra("CONSTRUTOR"), Palavra("ABSTRATO")
            )
        ),
        Tema(
            id = 2, nome = "Algoritmos", palavras = listOf(
                Palavra("RECURSAO"), Palavra("ITERACAO"), Palavra("ORDENACAO"),
                Palavra("PILHA"), Palavra("FILA"), Palavra("ARVORE"),
                Palavra("GRAFO"), Palavra("COMPLEXIDADE"), Palavra("BUSCA"), Palavra("VETOR")
            )
        ),
        Tema(
            id = 3, nome = "Banco de Dados", palavras = listOf(
                Palavra("TABELA"), Palavra("CONSULTA"), Palavra("INDICE"),
                Palavra("TRANSACAO"), Palavra("NORMALIZACAO"), Palavra("CHAVE"),
                Palavra("RELACIONAMENTO"), Palavra("SEQUENCIA"), Palavra("VISAO"), Palavra("TRIGGER")
            )
        ),
        Tema(
            id = 4, nome = "Redes", palavras = listOf(
                Palavra("PROTOCOLO"), Palavra("SOCKET"), Palavra("SERVIDOR"),
                Palavra("CLIENTE"), Palavra("FIREWALL"), Palavra("ROTEADOR"),
                Palavra("PACOTE"), Palavra("GATEWAY"), Palavra("DOMINIO"), Palavra("LATENCIA")
            )
        )
    )

    fun getById(id: Int): Tema? = temas.firstOrNull { it.id == id }
}
