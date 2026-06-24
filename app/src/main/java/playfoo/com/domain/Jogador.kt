package playfoo.com.domain
//classe abstrada que so implementa as caracteristica das classes que vão herdesa
//classe mãe
abstract class Jogador(
    open val id: String = "",
    open val nome: String = "",
    open val avatarConfig: AvatarConfig = AvatarConfig()
) {
    abstract val tipo: TipoJogador
    //testecommit

    open fun podeAcessarDashboard(): Boolean = false //sobreescrita
    open fun podeCriarTurma(): Boolean = false //sobreescrita
    open fun podeEntrarEmTurma(): Boolean = false //sobreescrita

    override fun toString(): String = "[$tipo] $nome"
}

enum class TipoJogador { ALUNO, GESTOR }
