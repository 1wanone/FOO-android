package playfoo.com.domain

data class JogadorAluno(
    override val id: String = "",
    override val nome: String = "",
    override val avatarConfig: AvatarConfig = AvatarConfig(),
    val turmaId: String? = null,
    val totalPartidas: Int = 0,
    val totalVitorias: Int = 0,
    val totalDerrotas: Int = 0
) : Jogador(id, nome, avatarConfig) {

    override val tipo: TipoJogador = TipoJogador.ALUNO

    override fun podeEntrarEmTurma(): Boolean = true //sobreescrita, aluno não pode criar turma e so visualiza seu dashboard

    fun calcularTaxaVitoria(): Float =
        if (totalPartidas == 0) 0f
        else totalVitorias.toFloat() / totalPartidas * 100f

    fun calcularNivel(): NivelAluno = when {
        totalPartidas < 10  -> NivelAluno.INICIANTE
        totalPartidas < 50  -> NivelAluno.INTERMEDIARIO
        totalVitorias >= 40 -> NivelAluno.EXPERT
        else                -> NivelAluno.AVANCADO
    }
}

enum class NivelAluno { INICIANTE, INTERMEDIARIO, AVANCADO, EXPERT }
