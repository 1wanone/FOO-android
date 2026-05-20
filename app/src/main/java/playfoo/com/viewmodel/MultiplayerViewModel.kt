package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import playfoo.com.data.TemaDataSource
import playfoo.com.data.remote.FirestoreRepository
import playfoo.com.domain.*
import javax.inject.Inject

enum class TelaMultiplayer { INICIAL, CRIAR, AGUARDAR, ENTRAR, JOGAR, RESULTADO }

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

    private val _uiState = MutableStateFlow(MultiplayerUiState())
    val uiState: StateFlow<MultiplayerUiState> = _uiState.asStateFlow()

    fun irPara(tela: TelaMultiplayer) {
        _uiState.value = _uiState.value.copy(tela = tela, erro = null)
    }

    fun criarSala(dificuldade: Dificuldade) {
        val userId = auth.currentUser?.uid ?: return
        val nome = auth.currentUser?.displayName ?: "Jogador 1"
        val tema = TemaDataSource.temas.random()
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
                        carregando        = false,
                        salaId            = salaId,
                        codigoSala        = dados["codigo"].toString(),
                        jogadorNumero     = 1,
                        jogador1Nome      = nome,
                        tema              = tema.nome,
                        dificuldade       = dificuldade,
                        palavraFinal      = palavra.texto,
                        tentativasRestantes = dificuldade.tentativasMaximas,
                        tentativasOponente  = dificuldade.tentativasMaximas,
                        tela              = TelaMultiplayer.AGUARDAR
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
                }

                // Sincronizar estado do oponente
                val progressoOp   = dados["progresso$numOponente"]?.toString() ?: ""
                val tentativasOp  = (dados["tentativas$numOponente"] as? Long)?.toInt()
                    ?: dif.tentativasMaximas

                _uiState.value = _uiState.value.copy(
                    progressoOponente  = progressoOp,
                    tentativasOponente = tentativasOp
                )

                // Resultado final
                if (status == "finalizada" && _uiState.value.tela == TelaMultiplayer.JOGAR) {
                    val vencedorId = dados["vencedor"]?.toString() ?: ""
                    _uiState.value = _uiState.value.copy(
                        tela     = TelaMultiplayer.RESULTADO,
                        euVenci  = vencedorId == auth.currentUser?.uid
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
        val acertou = p.tentativa(letra)
        atualizarEstadoLocal()

        // Avatar temporário — só muda se o jogo não terminou
        if (!_uiState.value.terminei) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    estadoAvatar = if (acertou) "ACERTOU" else "ERROU"
                )
                delay(800L)
                if (!_uiState.value.terminei) {
                    _uiState.value = _uiState.value.copy(estadoAvatar = "NEUTRO")
                }
            }
        }

        val salaId = _uiState.value.salaId
        val num    = _uiState.value.jogadorNumero
        viewModelScope.launch {
            firestoreRepository.atualizarProgressoSala(
                salaId       = salaId,
                numero       = num,
                progresso    = p.getProgresso(),
                tentativas   = p.getTentativasRestantes(),
                letrasErradas = p.getLetrasErradas().joinToString("")
            )
            if (p.venceu()) {
                firestoreRepository.finalizarSala(salaId, auth.currentUser?.uid ?: "")
            }
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

    fun reiniciar() {
        partida = null
        _uiState.value = MultiplayerUiState()
    }

    fun limparErro() {
        _uiState.value = _uiState.value.copy(erro = null)
    }
}