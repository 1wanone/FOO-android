package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.data.TurmaDataSource
import playfoo.com.data.local.TurmaPreferences
import playfoo.com.domain.Turma
import javax.inject.Inject

data class TurmaUiState(
    val turmasInscritas: List<Turma> = emptyList(),
    val codigo: String = "",
    val erro: String? = null,
    val mensagem: String? = null
)

@HiltViewModel
class TurmaViewModel @Inject constructor(
    private val turmaPrefs: TurmaPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(TurmaUiState())
    val uiState: StateFlow<TurmaUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun onCodigoChange(codigo: String) {
        _uiState.value = _uiState.value.copy(codigo = codigo, erro = null)
    }

    fun entrarTurma() {
        val codigo = _uiState.value.codigo.trim()
        if (codigo.isBlank()) {
            _uiState.value = _uiState.value.copy(erro = "Digite o código da turma")
            return
        }
        val turma = TurmaDataSource.getByCodigo(codigo)
        if (turma == null) {
            _uiState.value = _uiState.value.copy(erro = "Turma não encontrada. Verifique o código.")
            return
        }
        if (turmaPrefs.getCodigos().contains(turma.codigo.uppercase())) {
            _uiState.value = _uiState.value.copy(erro = "Você já está nessa turma")
            return
        }
        turmaPrefs.entrar(turma.codigo)
        carregar()
        _uiState.value = _uiState.value.copy(codigo = "", mensagem = "Você entrou em \"${turma.nome}\"!")
    }

    fun sairTurma(turma: Turma) {
        turmaPrefs.sair(turma.codigo)
        carregar()
    }

    fun limparMensagem() {
        _uiState.value = _uiState.value.copy(mensagem = null)
    }

    private fun carregar() {
        val turmas = turmaPrefs.getCodigos()
            .mapNotNull { TurmaDataSource.getByCodigo(it) }
            .sortedBy { it.nome }
        _uiState.value = _uiState.value.copy(turmasInscritas = turmas, erro = null)
    }
}
