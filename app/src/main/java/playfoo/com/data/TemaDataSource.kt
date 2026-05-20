package playfoo.com.data

import playfoo.com.domain.Palavra
import playfoo.com.domain.Tema

object TemaDataSource {

    val temas = listOf(

        Tema(
            id = 1,
            nome = "Exceções e Tratamento",
            palavras = listOf(
                Palavra("TRY"),
                Palavra("CATCH"),
                Palavra("FINALLY"),
                Palavra("THROW"),
                Palavra("THROWS"),
                Palavra("EXCEPTION")
            )
        ),

        Tema(
            id = 2,
            nome = "Herança",
            palavras = listOf(
                Palavra("SUPERCLASSE"),
                Palavra("SUBCLASSE"),
                Palavra("EXTENDS"),
                Palavra("CLASSEBASE"),
                Palavra("CLASSEDERIVADA"),
                Palavra("SUPER")
            )
        ),

        Tema(
            id = 3,
            nome = "Polimorfismo",
            palavras = listOf(
                Palavra("SOBRECARGA"),
                Palavra("SOBRESCRITA"),
                Palavra("DINAMICO"),
                Palavra("ESTATICO"),
                Palavra("OVERRIDE")
            )
        ),

        Tema(
            id = 4,
            nome = "Encapsulamento",
            palavras = listOf(
                Palavra("PRIVATE"),
                Palavra("PUBLIC"),
                Palavra("PROTECT"),
                Palavra("GETTER"),
                Palavra("SETTER")
            )
        ),

        Tema(
            id = 5,
            nome = "Introdução",
            palavras = listOf(
                Palavra("METODO"),
                Palavra("ATRIBUTO"),
                Palavra("CLASSE"),
                Palavra("OBJETO"),
                Palavra("PARAMETRO"),
                Palavra("ARGUMENTO")
            )
        ),

        Tema(
            id = 6,
            nome = "Relacionamento entre Classes",
            palavras = listOf(
                Palavra("ASSOCIACAO"),
                Palavra("COMPOSICAO"),
                Palavra("AGREGACAO"),
                Palavra("MULTIPLICIDADE"),
                Palavra("GENERALIZACAO")
            )
        ),

        Tema(
            id = 7,
            nome = "Interface",
            palavras = listOf(
                Palavra("IMPLEMENTS"),
                Palavra("CONTRATO"),
                Palavra("METODOABSTRATO")
            )
        ),

        Tema(
            id = 8,
            nome = "Coleções",
            palavras = listOf(
                Palavra("LIST"),
                Palavra("ARRAYLIST"),
                Palavra("MAP"),
                Palavra("HASHMAP"),
                Palavra("SET"),
                Palavra("VECTOR"),
                Palavra("STACK"),
                Palavra("QUEUE"),
                Palavra("LINKEDLIST"),
                Palavra("TREEMAP"),
                Palavra("HASHSET"),
                Palavra("TREESET")
            )
        ),

        Tema(
            id = 9,
            nome = "Interface Gráfica",
            palavras = listOf(
                Palavra("JAVAFX"),
                Palavra("SWING"),
                Palavra("BUTTON"),
                Palavra("PANEL"),
                Palavra("LABEL"),
                Palavra("TELA")
            )
        )
    )

    fun getTemaById(id: Int): Tema? = temas.find { it.id == id }

    fun getById(id: Int): Tema? = getTemaById(id)

    fun getPalavraAleatoria(temaId: Int): Palavra? =
        getTemaById(temaId)?.palavras?.random()

    fun getTemaAleatorio(): Tema = temas.random()
}
