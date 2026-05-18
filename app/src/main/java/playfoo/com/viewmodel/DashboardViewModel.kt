package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.data.local.JogadorPreferences
import playfoo.com.domain.JogadorAluno
import javax.inject.Inject

data class DashboardUiState(
    val jogador: JogadorAluno = JogadorAluno()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val jogadorPrefs: JogadorPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _uiState.value = DashboardUiState(jogador = jogadorPrefs.load())
    }
}
