package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import playfoo.com.data.TemaDataSource
import playfoo.com.data.remote.FirestoreRepository
import playfoo.com.domain.*
import javax.inject.Inject

enum class TelaMultiplayer { INICIAL, CRIAR, AGUARDAR, ENTRAR, JOGAR, RESULTADO, ESCOLHER_TEMA }

data class MultiplayerUiState(
    val tela: TelaMultiplayer = TelaMultiplayer.INICIAL,
    val carregando: Boolean = false,
    val erro: String? = null,
    val salaId: String = "",
    val codigoSala: String = "",
    val jogadorNumero: Int = 0,
    val jogador1Nome: String = "",
    val jogador2Nome: String = "",
    val tema: String = "",
    val dificuldade: Dificuldade = Dificuldade.NORMAL,
    val temaIdFixo: Int? = null,
    // Estado local do jogador
    val progresso: String = "",
    val letrasCorretas: Set<Char> = emptySet(),
    val letrasErradas: Set<Char> = emptySet(),
    val tentativasRestantes: Int = 6,
    val terminei: Boolean = false,
    val venci: Boolean = false,
    val estadoAvatar: String = "NEUTRO",
    // Estado do oponente (vindo do Firestore)
    val progressoOponente: String = "",
    val tentativasOponente: Int = 6,
    // Turno
    val turnoAtual: Int = 1,
    val meuTurno: Boolean = false,
    val timerSegundos: Int = 20,
    val timerAtivo: Boolean = false,
    // Resultado
    val euVenci: Boolean = false,
    val palavraFinal: String = ""
)

@HiltViewModel
class MultiplayerViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val jogoDaForca = JogoDaForca()
    private var partida: Partida? = null
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(MultiplayerUiState())
    val uiState: StateFlow<MultiplayerUiState> = _uiState.asStateFlow()

    fun irPara(tela: TelaMultiplayer) {
        _uiState.value = _uiState.value.copy(tela = tela, erro = null)
    }

    fun criarSala(dificuldade: Dificuldade) {
        val userId = auth.currentUser?.uid ?: return
        val nome = auth.currentUser?.displayName ?: "Jogador 1"
        val temaIdFixo = _uiState.value.temaIdFixo
        val tema = if (temaIdFixo != null)
            TemaDataSource.getTemaById(temaIdFixo) ?: TemaDataSource.getTemaAleatorio()
        else
            TemaDataSource.getTemaAleatorio()
        val palavra = tema.palavras.random()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true, erro = null)
            firestoreRepository.criarSalaMultiplayer(
                jogador1Id   = userId,
                jogador1Nome = nome,
                tema         = tema.nome,
                palavra      = palavra.texto,
                dificuldade  = dificuldade.name
            ).fold(
                onSuccess = { dados ->
                    val salaId = dados["id"].toString()
                    _uiState.value = _uiState.value.copy(
                        carregando          = false,
                        salaId              = salaId,
                        codigoSala          = dados["codigo"].toString(),
                        jogadorNumero       = 1,
                        jogador1Nome        = nome,
                        tema                = tema.nome,
                        dificuldade         = dificuldade,
                        palavraFinal        = palavra.texto,
                        tentativasRestantes = dificuldade.tentativasMaximas,
                        tentativasOponente  = dificuldade.tentativasMaximas,
                        tela                = TelaMultiplayer.AGUARDAR
                    )
                    escutarSala(salaId, palavra.texto, dificuldade)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = e.message ?: "Erro ao criar sala"
                    )
                }
            )
        }
    }

    fun entrarNaSala(codigo: String) {
        val userId = auth.currentUser?.uid ?: return
        val nome = auth.currentUser?.displayName ?: "Jogador 2"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true, erro = null)
            firestoreRepository.getSalaPorCodigo(codigo).fold(
                onSuccess = { (salaId, dados) ->
                    firestoreRepository.entrarNaSala(salaId, userId, nome).fold(
                        onSuccess = {
                            val dif = runCatching {
                                Dificuldade.valueOf(dados["dificuldade"].toString())
                            }.getOrDefault(Dificuldade.NORMAL)
                            val palavraTexto = dados["palavra"].toString()
                            _uiState.value = _uiState.value.copy(
                                carregando          = false,
                                salaId              = salaId,
                                jogadorNumero       = 2,
                                jogador1Nome        = dados["jogador1Nome"].toString(),
                                jogador2Nome        = nome,
                                tema                = dados["tema"].toString(),
                                dificuldade         = dif,
                                palavraFinal        = palavraTexto,
                                tentativasRestantes = dif.tentativasMaximas,
                                tentativasOponente  = dif.tentativasMaximas,
                                tela                = TelaMultiplayer.JOGAR
                            )
                            iniciarPartidaLocal(palavraTexto, dif)
                            escutarSala(salaId, palavraTexto, dif)
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                carregando = false,
                                erro = e.message ?: "Erro ao entrar na sala"
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = e.message ?: "Sala não encontrada"
                    )
                }
            )
        }
    }

    private fun escutarSala(salaId: String, palavraTexto: String, dif: Dificuldade) {
        viewModelScope.launch {
            firestoreRepository.escutarSala(salaId).collect { dados ->
                val status = dados["status"]?.toString() ?: ""
                val numOponente = if (_uiState.value.jogadorNumero == 1) 2 else 1

                // Jogador 1: inicia a partida quando jogador 2 entra
                if (_uiState.value.jogadorNumero == 1 &&
                    _uiState.value.tela == TelaMultiplayer.AGUARDAR &&
                    dados["jogador2Id"] != null
                ) {
                    _uiState.value = _uiState.value.copy(
                        jogador2Nome = dados["jogador2Nome"]?.toString() ?: "Oponente",
                        tela = TelaMultiplayer.JOGAR
                    )
                    iniciarPartidaLocal(palavraTexto, dif)
                    iniciarTimer() // Jogador 1 sempre começa
                }

                // Turno atual
                val turnoAnterior = _uiState.value.turnoAtual
                val turnoAtual = (dados["turnoAtual"] as? Long)?.toInt() ?: 1
                val meuTurno = turnoAtual == _uiState.value.jogadorNumero

                // Sincronizar estado do oponente
                val progressoOp  = dados["progresso$numOponente"]?.toString() ?: ""
                val tentativasOp = (dados["tentativas$numOponente"] as? Long)?.toInt()
                    ?: dif.tentativasMaximas

                _uiState.value = _uiState.value.copy(
                    turnoAtual         = turnoAtual,
                    meuTurno           = meuTurno,
                    progressoOponente  = progressoOp,
                    tentativasOponente = tentativasOp
                )

                // Quando o turno muda, reinicia o timer
                if (turnoAtual != turnoAnterior && _uiState.value.tela == TelaMultiplayer.JOGAR) {
                    if (meuTurno && !_uiState.value.terminei) {
                        iniciarTimer()
                    } else {
                        cancelarTimer()
                    }
                }

                // Resultado final
                if (status == "finalizada" && _uiState.value.tela == TelaMultiplayer.JOGAR) {
                    cancelarTimer()
                    val vencedorId = dados["vencedor"]?.toString() ?: ""
                    _uiState.value = _uiState.value.copy(
                        tela    = TelaMultiplayer.RESULTADO,
                        euVenci = vencedorId == auth.currentUser?.uid
                    )
                }
            }
        }
    }

    private fun iniciarPartidaLocal(palavraTexto: String, dif: Dificuldade) {
        val tema = Tema(nome = _uiState.value.tema)
        val palavra = Palavra(palavraTexto)
        val jogador = JogadorAluno(
            id   = auth.currentUser?.uid ?: "",
            nome = auth.currentUser?.displayName ?: ""
        )
        partida = jogoDaForca.iniciarPartida(tema, palavra, jogador, dif)
        atualizarEstadoLocal()
    }

    fun tentarLetra(letra: Char) {
        val p = partida ?: return
        if (p.terminou()) return
        if (!_uiState.value.meuTurno) return

        val acertou = p.tentativa(letra)
        atualizarEstadoLocal()

        val salaId = _uiState.value.salaId
        val num    = _uiState.value.jogadorNumero

        viewModelScope.launch {
            firestoreRepository.atualizarProgressoSala(
                salaId        = salaId,
                numero        = num,
                progresso     = p.getProgresso(),
                tentativas    = p.getTentativasRestantes(),
                letrasErradas = p.getLetrasErradas().joinToString("")
            )

            when {
                p.venceu() -> {
                    cancelarTimer()
                    firestoreRepository.finalizarSala(salaId, auth.currentUser?.uid ?: "")
                }
                p.terminou() -> {
                    // Acabaram as tentativas — passa a vez definitivamente
                    cancelarTimer()
                    val proximo = if (num == 1) 2 else 1
                    firestoreRepository.passarTurno(salaId, proximo)
                }
                !acertou -> {
                    // Errou — passa a vez
                    cancelarTimer()
                    passarVez()
                }
                // Acertou — mantém o turno, reinicia timer
                else -> iniciarTimer()
            }
        }
    }

    private fun iniciarTimer() {
        timerJob?.cancel()
        val segundos = when (_uiState.value.dificuldade) {
            Dificuldade.FACIL   -> 30
            Dificuldade.NORMAL  -> 20
            Dificuldade.DIFICIL -> 10
        }
        _uiState.value = _uiState.value.copy(timerSegundos = segundos, timerAtivo = true)
        timerJob = viewModelScope.launch {
            for (i in segundos downTo 1) {
                _uiState.value = _uiState.value.copy(timerSegundos = i)
                delay(1000)
            }
            if (!_uiState.value.terminei) {
                passarVez()
            }
        }
    }

    private fun cancelarTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(timerAtivo = false)
    }

    private fun passarVez() {
        val proximoTurno = if (_uiState.value.turnoAtual == 1) 2 else 1
        viewModelScope.launch {
            firestoreRepository.passarTurno(_uiState.value.salaId, proximoTurno)
        }
    }

    private fun atualizarEstadoLocal() {
        val p = partida ?: return
        val estadoAvatar = when {
            p.venceu()   -> "VITORIA"
            p.terminou() -> "DERROTA"
            else         -> _uiState.value.estadoAvatar
        }
        _uiState.value = _uiState.value.copy(
            progresso           = p.getProgresso(),
            letrasCorretas      = p.getLetrasCorretas(),
            letrasErradas       = p.getLetrasErradas(),
            tentativasRestantes = p.getTentativasRestantes(),
            terminei            = p.terminou(),
            venci               = p.venceu(),
            estadoAvatar        = estadoAvatar
        )
    }

    fun jogarNovamenteMesmoTema() {
        val temaAtual = TemaDataSource.temas.find { it.nome == _uiState.value.tema }
        _uiState.value = _uiState.value.copy(temaIdFixo = temaAtual?.id)
        partida = null
        irPara(TelaMultiplayer.CRIAR)
    }

    fun escolherTema(temaId: Int) {
        _uiState.value = _uiState.value.copy(temaIdFixo = temaId)
        partida = null
        irPara(TelaMultiplayer.CRIAR)
    }

    fun reiniciar() {
        timerJob?.cancel()
        partida = null
        _uiState.value = MultiplayerUiState()
    }

    fun limparErro() {
        _uiState.value = _uiState.value.copy(erro = null)
    }
}
