package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.data.remote.FirebaseAuthRepository
import javax.inject.Inject

@HiltViewModel
class OpcoesViewModel @Inject constructor(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _altoContraste = MutableStateFlow(false)
    val altoContraste: StateFlow<Boolean> = _altoContraste.asStateFlow()

    fun toggleAltoContraste() { _altoContraste.value = !_altoContraste.value }
    fun sair() { authRepository.logout() }
}