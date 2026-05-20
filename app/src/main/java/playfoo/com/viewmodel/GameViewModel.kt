package playfoo.com.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import playfoo.com.data.TemaDataSource
import playfoo.com.data.local.JogadorPreferences
import playfoo.com.data.remote.FirestoreRepository
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
    val palavra: String = "",
    val dificuldade: Dificuldade = Dificuldade.NORMAL,
    val resultado: ResultadoJogo = ResultadoJogo.EM_ANDAMENTO,
    val timerSegundos: Int? = null
)

enum class ResultadoJogo { EM_ANDAMENTO, VITORIA, DERROTA }

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val jogadorPrefs: JogadorPreferences,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val temaId: Int = savedStateHandle.get<String>("temaId")?.toIntOrNull() ?: 1
    private val dificuldade: Dificuldade = savedStateHandle.get<String>("dificuldade")
        ?.let { runCatching { Dificuldade.valueOf(it) }.getOrNull() } ?: Dificuldade.NORMAL

    private val jogoDaForca = JogoDaForca()
    private var partidaAtual: Partida? = null
    private var resultadoContabilizado = false

    private var timerJob: Job? = null
    private var avatarJob: Job? = null
    private var tempoInicioPartida = 0L

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        novaPartida()
    }

    fun reiniciar() {
        timerJob?.cancel()
        avatarJob?.cancel()
        resultadoContabilizado = false
        novaPartida()
    }

    fun tentarLetra(letra: Char) {
        val partida = partidaAtual ?: return
        if (partida.terminou()) return

        val acertou = partida.tentativa(letra)

        avatarJob?.cancel()
        val estadoImediato = if (acertou) "ACERTOU" else "ERROU"
        atualizarEstado(partida, estadoImediato)

        if (!partida.terminou()) {
            avatarJob = viewModelScope.launch {
                delay(1000L)
                atualizarEstado(partida, "NEUTRO")
            }
        }
    }

    private fun novaPartida() {
        val tema = TemaDataSource.getById(temaId) ?: TemaDataSource.temas.first()
        val palavra = tema.palavras.random()
        val jogador = JogadorAluno(id = "1", nome = "Jogador")
        val partida = jogoDaForca.iniciarPartida(tema, palavra, jogador, dificuldade)
        partidaAtual = partida
        tempoInicioPartida = System.currentTimeMillis()

        _uiState.value = GameUiState(
            progresso           = partida.getProgresso(),
            tentativasRestantes = partida.dificuldade.tentativasMaximas,
            tentativasMaximas   = partida.dificuldade.tentativasMaximas,
            tema                = partida.tema.nome,
            palavra             = partida.palavra.texto,
            dificuldade         = partida.dificuldade,
            timerSegundos       = dificuldade.tempoSegundos
        )
        iniciarTimer()
    }

    private fun iniciarTimer() {
        timerJob?.cancel()
        val segundos = dificuldade.tempoSegundos ?: return
        timerJob = viewModelScope.launch {
            var restante = segundos
            while (restante > 0) {
                delay(1000L)
                restante--
                _uiState.value = _uiState.value.copy(timerSegundos = restante)
            }
            // Tempo esgotado — forçar derrota
            avatarJob?.cancel()
            val partida = partidaAtual ?: return@launch
            if (!partida.terminou()) {
                partida.forcarDerrota()
                atualizarEstado(partida, "DERROTA")
            }
        }
    }

    private fun atualizarEstado(partida: Partida, estadoAvatar: String) {
        val resultado = when {
            partida.venceu()   -> ResultadoJogo.VITORIA
            partida.terminou() -> ResultadoJogo.DERROTA
            else               -> ResultadoJogo.EM_ANDAMENTO
        }
        if (!resultadoContabilizado && resultado != ResultadoJogo.EM_ANDAMENTO) {
            resultadoContabilizado = true
            timerJob?.cancel()
            if (resultado == ResultadoJogo.VITORIA) jogadorPrefs.registrarVitoria()
            else jogadorPrefs.registrarDerrota()
            salvarPartidaFirestore(resultado == ResultadoJogo.VITORIA)
        }
        val avatarFinal = when (resultado) {
            ResultadoJogo.VITORIA      -> "VITORIA"
            ResultadoJogo.DERROTA      -> "DERROTA"
            ResultadoJogo.EM_ANDAMENTO -> estadoAvatar
        }
        // .copy() preserva timerSegundos gerenciado pelo timerJob
        _uiState.value = _uiState.value.copy(
            progresso           = partida.getProgresso(),
            tentativasRestantes = partida.getTentativasRestantes(),
            tentativasMaximas   = partida.dificuldade.tentativasMaximas,
            letrasCorretas      = partida.getLetrasCorretas(),
            letrasErradas       = partida.getLetrasErradas(),
            estadoAvatar        = avatarFinal,
            tema                = partida.tema.nome,
            dificuldade         = partida.dificuldade,
            resultado           = resultado
        )
    }

    private fun salvarPartidaFirestore(venceu: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val state = _uiState.value
        viewModelScope.launch {
            val turmaId = try {
                firestoreRepository.getTurmaDoAluno(userId)
                    .getOrNull()
                    ?.get("id")
                    ?.toString()
            } catch (e: Exception) {
                null
            }
            android.util.Log.d("GAME", "Salvando partida - jogadorId: $userId, turmaId: $turmaId")
            firestoreRepository.salvarPartida(
                jogadorId        = userId,
                tema             = state.tema,
                palavra          = state.palavra,
                dificuldade      = state.dificuldade.name,
                venceu           = venceu,
                tentativasUsadas = state.dificuldade.tentativasMaximas - state.tentativasRestantes,
                tempoSegundos    = 0,
                turmaId          = turmaId
            )
        }
    }
}