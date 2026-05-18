package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.data.TemaDataSource
import playfoo.com.domain.Dificuldade
import playfoo.com.domain.Tema
import javax.inject.Inject

data class SelecionarTemaUiState(
    val temas: List<Tema> = emptyList(),
    val dificuldade: Dificuldade = Dificuldade.NORMAL
)

@HiltViewModel
class SelecionarTemaViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SelecionarTemaUiState(temas = TemaDataSource.temas))
    val uiState: StateFlow<SelecionarTemaUiState> = _uiState.asStateFlow()

    fun selecionarDificuldade(dificuldade: Dificuldade) {
        _uiState.value = _uiState.value.copy(dificuldade = dificuldade)
    }
}
