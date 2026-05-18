package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.domain.*
import javax.inject.Inject

data class GameUiState(
    val progresso: String = "",
    val tentativasRestantes: Int = 0,
    val tentativasMaximas: Int = 0,
    val letrasCorretas: Set<Char> = emptySet(),
    val letrasErradas: Set<Char> = emptySet(),
    val estadoAvatar: String = "NEUTRO",
    val tema: String = "",
    val dificuldade: Dificuldade = Dificuldade.NORMAL,
    val resultado: ResultadoJogo = ResultadoJogo.EM_ANDAMENTO
)

enum class ResultadoJogo { EM_ANDAMENTO, VITORIA, DERROTA }

@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {

    private val jogoDaForca = JogoDaForca()
    private var partidaAtual: Partida? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun iniciarPartida(
        tema: Tema,
        palavra: Palavra,
        jogador: Jogador,
        dificuldade: Dificuldade
    ) {
        val partida = jogoDaForca.iniciarPartida(tema, palavra, jogador, dificuldade)
        partidaAtual = partida
        atualizarEstado(partida)
    }

    fun tentarLetra(letra: Char) {
        val partida = partidaAtual ?: return
        if (partida.terminou()) return
        partida.tentativa(letra)
        atualizarEstado(partida)
    }

    private fun atualizarEstado(partida: Partida) {
        val resultado = when {
            partida.venceu()   -> ResultadoJogo.VITORIA
            partida.terminou() -> ResultadoJogo.DERROTA
            else               -> ResultadoJogo.EM_ANDAMENTO
        }
        _uiState.value = GameUiState(
            progresso           = partida.getProgresso(),
            tentativasRestantes = partida.getTentativasRestantes(),
            tentativasMaximas   = partida.dificuldade.tentativasMaximas,
            letrasCorretas      = partida.getLetrasCorretas(),
            letrasErradas       = partida.getLetrasErradas(),
            estadoAvatar        = resultado.name,
            tema                = partida.tema.nome,
            dificuldade         = partida.dificuldade,
            resultado           = resultado
        )
    }
}