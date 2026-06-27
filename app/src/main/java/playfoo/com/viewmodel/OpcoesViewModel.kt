package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import playfoo.com.data.local.SoundPreferences
import playfoo.com.data.remote.FirebaseAuthRepository
import javax.inject.Inject

@HiltViewModel
class OpcoesViewModel @Inject constructor(
    private val authRepository: FirebaseAuthRepository,
    private val soundPrefs: SoundPreferences
) : ViewModel() {

    private val _altoContraste = MutableStateFlow(false)
    val altoContraste: StateFlow<Boolean> = _altoContraste.asStateFlow()

    private val _efeitosSonoros = MutableStateFlow(soundPrefs.getEfeitosSonoros())
    val efeitosSonoros: StateFlow<Boolean> = _efeitosSonoros.asStateFlow()

    private val _musicaFundo = MutableStateFlow(soundPrefs.getMusicaFundo())
    val musicaFundo: StateFlow<Boolean> = _musicaFundo.asStateFlow()

    fun toggleAltoContraste() { _altoContraste.value = !_altoContraste.value }

    fun toggleEfeitosSonoros(): Boolean {
        val novo = !_efeitosSonoros.value
        _efeitosSonoros.value = novo
        soundPrefs.setEfeitosSonoros(novo)
        return novo
    }

    fun toggleMusicaFundo(): Boolean {
        val novo = !_musicaFundo.value
        _musicaFundo.value = novo
        soundPrefs.setMusicaFundo(novo)
        return novo
    }

    fun sair() { authRepository.logout() }
}
