package playfoo.com.domain

data class JogadorGestor(
    override val id: String = "",
    override val nome: String = "",
    override val avatarConfig: AvatarConfig = AvatarConfig(),
    val turmasGerenciadas: List<String> = emptyList(),
    val instituicao: String = ""
) : Jogador(id, nome, avatarConfig) {

    override val tipo: TipoJogador = TipoJogador.GESTOR

    override fun podeAcessarDashboard(): Boolean = true //sobreescrita
    override fun podeCriarTurma(): Boolean = true //sobreescrita
    override fun podeEntrarEmTurma(): Boolean = false //sobreescrita
}
