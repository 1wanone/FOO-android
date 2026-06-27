package playfoo.com.domain

abstract class Jogador(
    open val id: String = "",
    open val nome: String = "",
    open val avatarConfig: AvatarConfig = AvatarConfig()
) {
    abstract val tipo: TipoJogador

    open fun podeAcessarDashboard(): Boolean = false
    open fun podeCriarTurma(): Boolean = false
    open fun podeEntrarEmTurma(): Boolean = false

    override fun toString(): String = "[$tipo] $nome"
}

enum class TipoJogador { ALUNO, GESTOR }
