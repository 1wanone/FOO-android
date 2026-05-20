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
import javax.inject.Inject

data class TurmaUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val sucesso: String? = null,
    val turmaAtual: Map<String, Any>? = null,
    val membros: List<Map<String, Any>> = emptyList(),
    val partidas: List<Map<String, Any>> = emptyList(),
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

    fun limparMensagens() {
        _uiState.value = _uiState.value.copy(erro = null, sucesso = null)
    }

    private fun gerarCodigo(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
