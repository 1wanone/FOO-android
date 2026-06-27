package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import playfoo.com.data.remote.FirestoreRepository
import playfoo.com.domain.RankingJogador
import playfoo.com.domain.TipoUsuario
import javax.inject.Inject

data class TurmaUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val sucesso: String? = null,
    val turmaAtual: Map<String, Any>? = null,
    val turmasDoGestor: List<Map<String, Any>> = emptyList(),
    val tipoUsuario: String = TipoUsuario.ALUNO,
    val membros: List<Map<String, Any>> = emptyList(),
    val partidas: List<Map<String, Any>> = emptyList(),
    val ranking: List<RankingJogador> = emptyList(),
    val rankingPorTurma: Map<String, List<RankingJogador>> = emptyMap(),
    val nomes: Map<String, String> = emptyMap(),
    val nomeProfessor: String = "",
    val telaTurma: TelaTurma = TelaTurma.INICIAL
)

enum class TelaTurma { INICIAL, CRIAR, ENTRAR, DETALHES }

@HiltViewModel
class TurmaViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(TurmaUiState())
    val uiState: StateFlow<TurmaUiState> = _uiState.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository.getUsuario(uid)
                    .onSuccess { data ->
                        val tipo = data["tipo"]?.toString() ?: TipoUsuario.ALUNO
                        _uiState.value = _uiState.value.copy(tipoUsuario = tipo)
                        if (tipo == TipoUsuario.GESTOR) carregarTurmasGestor()
                        else carregarTurmaAluno()
                    }
            }
        }
    }

    fun irPara(tela: TelaTurma) {
        _uiState.value = _uiState.value.copy(
            telaTurma = tela,
            erro = null,
            sucesso = null
        )
    }

    fun criarTurma(nome: String) {
        val gestorId = auth.currentUser?.uid ?: return
        if (nome.isBlank()) {
            _uiState.value = _uiState.value.copy(erro = "Nome da turma não pode ser vazio")
            return
        }
        val codigo = gerarCodigo()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true, erro = null)
            firestoreRepository.criarTurma(nome.trim(), codigo, gestorId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        sucesso = "Turma criada! Código: $codigo",
                        telaTurma = TelaTurma.INICIAL
                    )
                    carregarTurmasGestor()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = e.message ?: "Erro ao criar turma"
                    )
                }
        }
    }

    fun entrarNaTurma(codigo: String) {
        val jogadorId = auth.currentUser?.uid ?: return
        if (codigo.isBlank()) {
            _uiState.value = _uiState.value.copy(erro = "Digite o código da turma")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true, erro = null)
            firestoreRepository.entrarNaTurma(codigo.trim().uppercase(), jogadorId)
                .onSuccess { turma ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        sucesso = "Entrou na turma: ${turma["nome"]}",
                        turmaAtual = turma,
                        telaTurma = TelaTurma.INICIAL
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = e.message ?: "Turma não encontrada"
                    )
                }
        }
    }

    fun carregarTurmasGestor() {
        val gestorId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestoreRepository.getTurmasDoGestor(gestorId)
                .onSuccess { lista ->
                    _uiState.value = _uiState.value.copy(turmasDoGestor = lista)
                    val todosUids = lista.flatMap { turma ->
                        (turma["membros"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    }.distinct()
                    if (todosUids.isNotEmpty()) {
                        val novosNomes = firestoreRepository.buscarNomesAlunos(todosUids)
                        _uiState.value = _uiState.value.copy(nomes = _uiState.value.nomes + novosNomes)
                    }
                    lista.forEach { turma ->
                        val turmaId = turma["id"]?.toString() ?: return@forEach
                        carregarRanking(turmaId)
                    }
                }
        }
    }

    fun carregarTurmaAluno() {
        val alunoId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestoreRepository.getTurmaDoAluno(alunoId)
                .onSuccess { turma ->
                    _uiState.value = _uiState.value.copy(turmaAtual = turma)
                    val turmaId = turma?.get("id")?.toString() ?: return@onSuccess
                    val nomeProfessor = firestoreRepository.buscarNomeProfessor(turmaId)
                    _uiState.value = _uiState.value.copy(nomeProfessor = nomeProfessor)
                    carregarRanking(turmaId)
                }
        }
    }

    fun carregarRanking(turmaId: String) {
        viewModelScope.launch {
            firestoreRepository.getPartidasTurma(turmaId).onSuccess { partidas ->
                val porJogador = partidas.groupBy { it["jogadorId"].toString() }
                val uidsNovos = porJogador.keys.filter { !_uiState.value.nomes.containsKey(it) }
                if (uidsNovos.isNotEmpty()) {
                    val novosNomes = firestoreRepository.buscarNomesAlunos(uidsNovos)
                    _uiState.value = _uiState.value.copy(nomes = _uiState.value.nomes + novosNomes)
                }
                val nomes = _uiState.value.nomes
                val lista = porJogador.map { (id, ps) ->
                    val vitorias = ps.count { it["venceu"] == true }
                    RankingJogador(
                        id          = id,
                        nome        = nomes[id] ?: id.take(8),
                        vitorias    = vitorias,
                        partidas    = ps.size,
                        taxaVitoria = if (ps.isEmpty()) 0f else vitorias.toFloat() / ps.size * 100f
                    )
                }.sortedByDescending { it.vitorias }
                _uiState.value = _uiState.value.copy(
                    ranking         = lista,
                    rankingPorTurma = _uiState.value.rankingPorTurma + (turmaId to lista)
                )
            }
        }
    }

    fun removerAluno(turmaId: String, alunoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true)
            firestoreRepository.sairDaTurma(turmaId, alunoId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(carregando = false)
                    carregarTurmasGestor()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = e.message ?: "Erro ao remover aluno"
                    )
                }
        }
    }

    fun sairDaTurma() {
        val alunoId = auth.currentUser?.uid ?: return
        val turmaId = _uiState.value.turmaAtual?.get("id")?.toString() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true)
            firestoreRepository.sairDaTurma(turmaId, alunoId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        turmaAtual = null,
                        sucesso = "Saiu da turma com sucesso"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = e.message ?: "Erro ao sair da turma"
                    )
                }
        }
    }

    fun limparMensagens() {
        _uiState.value = _uiState.value.copy(erro = null, sucesso = null)
    }

    private fun gerarCodigo(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}