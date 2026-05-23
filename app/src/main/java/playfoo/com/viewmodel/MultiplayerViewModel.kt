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
import playfoo.com.domain.Dificuldade
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
    // Palavra compartilhada
    val palavra: String = "",
    val progresso: String = "",
    val letrasReveladas: Set<Char> = emptySet(),
    // Estado do próprio jogador
    val letrasErradasEu: Set<Char> = emptySet(),
    val tentativasEu: Int = 6,
    val terminei: Boolean = false,
    // Estado do oponente
    val letrasErradasOponente: Set<Char> = emptySet(),
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
                        carregando        = false,
                        salaId            = salaId,
                        codigoSala        = dados["codigo"].toString(),
                        jogadorNumero     = 1,
                        jogador1Nome      = nome,
                        tema              = tema.nome,
                        dificuldade       = dificuldade,
                        palavra           = palavra.texto,
                        palavraFinal      = palavra.texto,
                        tentativasEu      = dificuldade.tentativasMaximas,
                        tentativasOponente = dificuldade.tentativasMaximas,
                        tela              = TelaMultiplayer.AGUARDAR
                    )
                    escutarSala(salaId)
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
                                carregando         = false,
                                salaId             = salaId,
                                jogadorNumero      = 2,
                                jogador1Nome       = dados["jogador1Nome"].toString(),
                                jogador2Nome       = nome,
                                tema               = dados["tema"].toString(),
                                dificuldade        = dif,
                                palavra            = palavraTexto,
                                palavraFinal       = palavraTexto,
                                tentativasEu       = dif.tentativasMaximas,
                                tentativasOponente = dif.tentativasMaximas,
                                tela               = TelaMultiplayer.JOGAR
                            )
                            escutarSala(salaId)
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

    private fun escutarSala(salaId: String) {
        viewModelScope.launch {
            firestoreRepository.escutarSala(salaId).collect { dados ->
                val status = dados["status"]?.toString() ?: ""
                val jogadorNumero = _uiState.value.jogadorNumero
                val numOponente = if (jogadorNumero == 1) 2 else 1
                val dif = _uiState.value.dificuldade

                // Jogador 1: inicia quando jogador 2 entra
                if (jogadorNumero == 1 &&
                    _uiState.value.tela == TelaMultiplayer.AGUARDAR &&
                    dados["jogador2Id"] != null
                ) {
                    _uiState.value = _uiState.value.copy(
                        jogador2Nome = dados["jogador2Nome"]?.toString() ?: "Oponente",
                        tela = TelaMultiplayer.JOGAR
                    )
                    iniciarTimer() // Jogador 1 sempre começa
                }

                // Retornar ao jogo se oponente reiniciou a sala
                if (status == "jogando" && _uiState.value.tela == TelaMultiplayer.RESULTADO) {
                    val novaPalavra = dados["palavra"]?.toString() ?: _uiState.value.palavra
                    val novoTema    = dados["tema"]?.toString() ?: _uiState.value.tema
                    _uiState.value = _uiState.value.copy(
                        tela               = TelaMultiplayer.JOGAR,
                        palavra            = novaPalavra,
                        palavraFinal       = novaPalavra,
                        tema               = novoTema,
                        progresso          = "",
                        letrasReveladas    = emptySet(),
                        letrasErradasEu    = emptySet(),
                        letrasErradasOponente = emptySet(),
                        tentativasEu       = dif.tentativasMaximas,
                        tentativasOponente = dif.tentativasMaximas,
                        terminei           = false,
                        turnoAtual         = 1,
                        meuTurno           = jogadorNumero == 1,
                        euVenci            = false
                    )
                    if (jogadorNumero == 1) iniciarTimer()
                    return@collect
                }

                // Turno atual
                val turnoAnterior = _uiState.value.turnoAtual
                val turnoAtual = (dados["turnoAtual"] as? Long)?.toInt() ?: 1
                val meuTurno = turnoAtual == jogadorNumero

                // Estado compartilhado
                val letrasReveladas = (dados["letrasReveladas"]?.toString() ?: "")
                    .filter { it.isLetter() }.toSet()
                val progresso = dados["progresso"]?.toString() ?: ""

                // Tentativas e erros individuais
                val tentativasEu = (dados["tentativas$jogadorNumero"] as? Long)?.toInt()
                    ?: dif.tentativasMaximas
                val tentativasOponente = (dados["tentativas$numOponente"] as? Long)?.toInt()
                    ?: dif.tentativasMaximas
                val letrasErradasEu = (dados["letrasErradas$jogadorNumero"]?.toString() ?: "")
                    .filter { it.isLetter() }.toSet()
                val letrasErradasOponente = (dados["letrasErradas$numOponente"]?.toString() ?: "")
                    .filter { it.isLetter() }.toSet()

                val terminei = tentativasEu <= 0

                _uiState.value = _uiState.value.copy(
                    turnoAtual             = turnoAtual,
                    meuTurno               = meuTurno,
                    letrasReveladas        = letrasReveladas,
                    progresso              = progresso,
                    tentativasEu           = tentativasEu,
                    tentativasOponente     = tentativasOponente,
                    letrasErradasEu        = letrasErradasEu,
                    letrasErradasOponente  = letrasErradasOponente,
                    terminei               = terminei
                )

                // Quando o turno muda, reinicia o timer
                if (turnoAtual != turnoAnterior && _uiState.value.tela == TelaMultiplayer.JOGAR) {
                    if (meuTurno && !terminei) iniciarTimer() else cancelarTimer()
                }

                // Resultado final
                if (status == "finalizada" && _uiState.value.tela == TelaMultiplayer.JOGAR) {
                    cancelarTimer()
                    val vencedorId = dados["vencedor"]?.toString() ?: ""
                    _uiState.value = _uiState.value.copy(
                        tela         = TelaMultiplayer.RESULTADO,
                        euVenci      = vencedorId == auth.currentUser?.uid,
                        palavraFinal = _uiState.value.palavra
                    )
                }
            }
        }
    }

    fun tentarLetra(letra: Char) {
        if (!_uiState.value.meuTurno) return
        val state = _uiState.value
        val letraUpper = letra.uppercaseChar()

        // Bloqueia letra já usada
        if (letraUpper in state.letrasReveladas) return
        if (letraUpper in state.letrasErradasEu) return
        if (letraUpper in state.letrasErradasOponente) return

        val acertou = state.palavra.uppercase().contains(letraUpper)
        val novasLetrasReveladas = if (acertou) state.letrasReveladas + letraUpper
                                   else state.letrasReveladas

        val novoProgresso = state.palavra.uppercase().map { c ->
            if (!c.isLetter()) c
            else if (c in novasLetrasReveladas) c
            else '_'
        }.joinToString(" ")

        val novasTentativas = if (acertou) state.tentativasEu else state.tentativasEu - 1
        val novasLetrasErradas = if (acertou) state.letrasErradasEu
                                 else state.letrasErradasEu + letraUpper

        val palavraRevelada = !novoProgresso.contains('_')
        val semTentativas   = novasTentativas <= 0

        viewModelScope.launch {
            firestoreRepository.revelarLetra(
                salaId          = state.salaId,
                novoProgresso   = novoProgresso,
                letrasReveladas = novasLetrasReveladas.joinToString(""),
                jogadorNumero   = state.jogadorNumero,
                tentativas      = novasTentativas,
                letrasErradas   = novasLetrasErradas.joinToString("")
            )

            when {
                palavraRevelada -> {
                    cancelarTimer()
                    firestoreRepository.finalizarSala(state.salaId, auth.currentUser?.uid ?: "")
                }
                semTentativas -> {
                    cancelarTimer()
                    val proximo = if (state.turnoAtual == 1) 2 else 1
                    firestoreRepository.passarTurno(state.salaId, proximo)
                }
                !acertou -> {
                    cancelarTimer()
                    passarVez()
                }
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
            if (!_uiState.value.terminei) passarVez()
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

    fun jogarNovamenteMesmoTema() {
        val state = _uiState.value
        val temaAtual = TemaDataSource.temas.find { it.nome == state.tema }
            ?: TemaDataSource.getTemaAleatorio()
        val novaPalavra = temaAtual.palavras.random()

        viewModelScope.launch {
            firestoreRepository.reiniciarSala(
                salaId      = state.salaId,
                novaPalavra = novaPalavra.texto,
                novoTema    = temaAtual.nome
            )
            _uiState.value = state.copy(
                tela               = TelaMultiplayer.JOGAR,
                palavra            = novaPalavra.texto,
                palavraFinal       = novaPalavra.texto,
                tema               = temaAtual.nome,
                progresso          = "",
                letrasReveladas    = emptySet(),
                letrasErradasEu    = emptySet(),
                letrasErradasOponente = emptySet(),
                tentativasEu       = state.dificuldade.tentativasMaximas,
                tentativasOponente = state.dificuldade.tentativasMaximas,
                terminei           = false,
                euVenci            = false,
                turnoAtual         = 1,
                meuTurno           = state.jogadorNumero == 1
            )
            if (state.jogadorNumero == 1) iniciarTimer()
        }
    }

    fun escolherTema(temaId: Int) {
        val state = _uiState.value
        val tema = TemaDataSource.getTemaById(temaId) ?: TemaDataSource.getTemaAleatorio()
        val novaPalavra = tema.palavras.random()

        viewModelScope.launch {
            firestoreRepository.reiniciarSala(
                salaId      = state.salaId,
                novaPalavra = novaPalavra.texto,
                novoTema    = tema.nome
            )
            _uiState.value = state.copy(
                tela               = TelaMultiplayer.JOGAR,
                palavra            = novaPalavra.texto,
                palavraFinal       = novaPalavra.texto,
                tema               = tema.nome,
                progresso          = "",
                letrasReveladas    = emptySet(),
                letrasErradasEu    = emptySet(),
                letrasErradasOponente = emptySet(),
                tentativasEu       = state.dificuldade.tentativasMaximas,
                tentativasOponente = state.dificuldade.tentativasMaximas,
                terminei           = false,
                euVenci            = false,
                turnoAtual         = 1,
                meuTurno           = state.jogadorNumero == 1
            )
            if (state.jogadorNumero == 1) iniciarTimer()
        }
    }

    fun reiniciar() {
        timerJob?.cancel()
        _uiState.value = MultiplayerUiState()
    }

    fun limparErro() {
        _uiState.value = _uiState.value.copy(erro = null)
    }
}
