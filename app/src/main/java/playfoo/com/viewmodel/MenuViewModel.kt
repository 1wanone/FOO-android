package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import playfoo.com.data.local.AvatarPreferences
import playfoo.com.data.remote.FirestoreRepository
import playfoo.com.domain.AvatarConfig
import javax.inject.Inject

data class MenuUiState(
    val avatarConfig: AvatarConfig = AvatarConfig()
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val avatarPrefs: AvatarPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState(avatarConfig = avatarPrefs.load()))
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        recarregar()
    }

    fun recarregar() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            firestoreRepository.getAvatar(userId).onSuccess { config ->
                avatarPrefs.save(config)
                _uiState.value = MenuUiState(avatarConfig = config)
            }.onFailure {
                _uiState.value = MenuUiState(avatarConfig = avatarPrefs.load())
            }
        }
    }
}
