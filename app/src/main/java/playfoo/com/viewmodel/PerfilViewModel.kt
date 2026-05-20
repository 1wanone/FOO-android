package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import playfoo.com.data.local.AvatarPreferences
import playfoo.com.data.remote.FirebaseAuthRepository
import playfoo.com.data.remote.FirestoreRepository
import playfoo.com.domain.AuthUser
import playfoo.com.domain.AvatarConfig
import playfoo.com.domain.TipoUsuario
import javax.inject.Inject

data class PerfilUiState(
    val carregando: Boolean = false,
    val nomeUsuario: String = "",
    val emailUsuario: String = "",
    val tipoUsuario: String = TipoUsuario.ALUNO,
    val avatarConfig: AvatarConfig = AvatarConfig(),
    val turmaId: String? = null,
    val totalPartidas: Int = 0,
    val totalVitorias: Int = 0,
    val totalDerrotas: Int = 0,
    val taxaVitoria: Float = 0f,
    val temaFavorito: String = "",
    val palavrasErradas: Map<String, Int> = emptyMap(),
    val nivel: String = "INICIANTE",
    val modoEdicao: Boolean = false,
    val usuarioLogado: AuthUser? = null
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val firestoreRepository: FirestoreRepository,
    private val avatarPrefs: AvatarPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState(avatarConfig = avatarPrefs.load()))
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        val usuario = firebaseAuthRepository.getUsuarioAtual()
        _uiState.value = _uiState.value.copy(
            usuarioLogado = usuario,
            nomeUsuario   = usuario?.nome ?: "",
            emailUsuario  = usuario?.email ?: ""
        )
        if (usuario == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true)

            firestoreRepository.getUsuario(usuario.id)
                .onSuccess { data ->
                    val tipo = data["tipo"]?.toString() ?: TipoUsuario.ALUNO
                    _uiState.value = _uiState.value.copy(
                        tipoUsuario   = tipo,
                        usuarioLogado = usuario.copy(tipo = tipo)
                    )
                }

            firestoreRepository.getEstatisticasAluno(usuario.id)
                .onSuccess { stats ->
                    val total    = (stats["totalPartidas"] as? Int) ?: 0
                    val vitorias = (stats["totalVitorias"] as? Int) ?: 0
                    val derrotas = (stats["totalDerrotas"] as? Int) ?: 0
                    val taxa     = (stats["taxaVitoria"] as? Float) ?: 0f
                    val tema     = (stats["temaFavorito"] as? String) ?: ""
                    @Suppress("UNCHECKED_CAST")
                    val palavras = (stats["palavrasErradas"] as? Map<String, Int>) ?: emptyMap()
                    _uiState.value = _uiState.value.copy(
                        carregando      = false,
                        totalPartidas   = total,
                        totalVitorias   = vitorias,
                        totalDerrotas   = derrotas,
                        taxaVitoria     = taxa,
                        temaFavorito    = tema,
                        palavrasErradas = palavras,
                        nivel           = calcularNivel(total, vitorias)
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(carregando = false)
                }
        }
    }

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

    fun logout() {
        firebaseAuthRepository.logout()
        _uiState.value = PerfilUiState(avatarConfig = avatarPrefs.load())
    }

    private fun calcularNivel(totalPartidas: Int, totalVitorias: Int): String = when {
        totalPartidas < 10  -> "INICIANTE"
        totalPartidas < 50  -> "INTERMEDIARIO"
        totalVitorias >= 40 -> "EXPERT"
        else                -> "AVANCADO"
    }
}
