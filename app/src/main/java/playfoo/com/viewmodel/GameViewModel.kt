package playfoo.com.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.data.TemaDataSource
import playfoo.com.data.local.JogadorPreferences
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
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val jogadorPrefs: JogadorPreferences
) : ViewModel() {

    private val temaId: Int = savedStateHandle.get<String>("temaId")?.toIntOrNull() ?: 1
    private val dificuldade: Dificuldade = savedStateHandle.get<String>("dificuldade")
        ?.let { runCatching { Dificuldade.valueOf(it) }.getOrNull() } ?: Dificuldade.NORMAL

    private val jogoDaForca = JogoDaForca()
    private var partidaAtual: Partida? = null
    private var resultadoContabilizado = false

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        novaPartida()
    }

    fun reiniciar() {
        resultadoContabilizado = false
        novaPartida()
    }

    fun tentarLetra(letra: Char) {
        val partida = partidaAtual ?: return
        if (partida.terminou()) return
        partida.tentativa(letra)
        atualizarEstado(partida)
    }

    private fun novaPartida() {
        val tema = TemaDataSource.getById(temaId) ?: TemaDataSource.temas.first()
        val palavra = tema.palavras.random()
        val jogador = JogadorAluno(id = "1", nome = "Jogador")
        val partida = jogoDaForca.iniciarPartida(tema, palavra, jogador, dificuldade)
        partidaAtual = partida
        atualizarEstado(partida)
    }

    private fun atualizarEstado(partida: Partida) {
        val resultado = when {
            partida.venceu()   -> ResultadoJogo.VITORIA
            partida.terminou() -> ResultadoJogo.DERROTA
            else               -> ResultadoJogo.EM_ANDAMENTO
        }
        if (!resultadoContabilizado && resultado != ResultadoJogo.EM_ANDAMENTO) {
            resultadoContabilizado = true
            if (resultado == ResultadoJogo.VITORIA) jogadorPrefs.registrarVitoria()
            else jogadorPrefs.registrarDerrota()
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
