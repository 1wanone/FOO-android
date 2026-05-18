package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.data.local.AvatarPreferences
import playfoo.com.domain.AvatarConfig
import javax.inject.Inject

data class PerfilUiState(
    val avatarConfig: AvatarConfig = AvatarConfig(),
    val modoEdicao: Boolean = false
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val avatarPrefs: AvatarPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState(avatarConfig = avatarPrefs.load()))
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun abrirEditor() {
        _uiState.value = _uiState.value.copy(modoEdicao = true)
    }

    fun fecharEditor() {
        _uiState.value = _uiState.value.copy(modoEdicao = false)
    }

    fun salvarAvatar(config: AvatarConfig) {
        avatarPrefs.save(config)
        _uiState.value = _uiState.value.copy(avatarConfig = config, modoEdicao = false)
    }
}
