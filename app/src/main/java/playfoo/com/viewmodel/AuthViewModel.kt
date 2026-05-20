package playfoo.com.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import playfoo.com.data.remote.FirebaseAuthRepository
import playfoo.com.domain.AuthUser
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Carregando : AuthUiState()
    data class Sucesso(val usuario: AuthUser) : AuthUiState()
    data class Erro(val mensagem: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        val usuario = firebaseAuthRepository.getUsuarioAtual()
        if (usuario != null) {
            _state.value = AuthUiState.Sucesso(usuario)
        }
    }

    fun login(email: String, senha: String) {
        val erro = validarLogin(email, senha)
        if (erro != null) { _state.value = AuthUiState.Erro(erro); return }

        viewModelScope.launch {
            _state.value = AuthUiState.Carregando
            val result = firebaseAuthRepository.loginEmail(email, senha)
            _state.value = result.fold(
                onSuccess = { AuthUiState.Sucesso(it) },
                onFailure = { AuthUiState.Erro("Email ou senha incorretos") }
            )
        }
    }

    fun registrar(nome: String, email: String, senha: String, confirmar: String) {
        val erro = validarCadastro(nome, email, senha, confirmar)
        if (erro != null) { _state.value = AuthUiState.Erro(erro); return }

        viewModelScope.launch {
            _state.value = AuthUiState.Carregando
            val result = firebaseAuthRepository.registrar(nome, email, senha)
            _state.value = result.fold(
                onSuccess = { AuthUiState.Sucesso(it) },
                onFailure = { AuthUiState.Erro(it.localizedMessage ?: "Erro ao criar conta") }
            )
        }
    }

    fun loginGoogle(context: Context) {
        viewModelScope.launch {
            _state.value = AuthUiState.Carregando
            try {
                val credentialManager = CredentialManager.create(context)
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()
                val response = credentialManager.getCredential(context = context, request = request)
                val cred = response.credential
                if (cred is CustomCredential &&
                    cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCred = GoogleIdTokenCredential.createFrom(cred.data)
                    val result = firebaseAuthRepository.loginGoogle(googleCred.idToken)
                    _state.value = result.fold(
                        onSuccess = { AuthUiState.Sucesso(it) },
                        onFailure = { AuthUiState.Erro(it.localizedMessage ?: "Erro no login com Google") }
                    )
                } else {
                    _state.value = AuthUiState.Erro("Tipo de credencial não suportado")
                }
            } catch (e: GetCredentialException) {
                _state.value = AuthUiState.Erro(
                    "Login com Google indisponível. Configure o WEB_CLIENT_ID no AuthViewModel."
                )
            } catch (e: Exception) {
                _state.value = AuthUiState.Erro("Erro inesperado: ${e.localizedMessage}")
            }
        }
    }

    fun recuperarSenha(email: String) {
        if (email.isBlank()) { _state.value = AuthUiState.Erro("Informe o email"); return }
        viewModelScope.launch {
            _state.value = AuthUiState.Carregando
            val result = firebaseAuthRepository.recuperarSenha(email)
            _state.value = result.fold(
                onSuccess = { AuthUiState.Idle },
                onFailure = { AuthUiState.Erro(it.localizedMessage ?: "Erro ao enviar email") }
            )
        }
    }

    fun logout() {
        firebaseAuthRepository.logout()
        _state.value = AuthUiState.Idle
    }

    fun limparEstado() { _state.value = AuthUiState.Idle }

    private fun validarLogin(email: String, senha: String): String? {
        if (email.isBlank() || senha.isBlank()) return "Preencha todos os campos"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Email inválido"
        if (senha.length < 8) return "A senha deve ter pelo menos 8 caracteres"
        return null
    }

    private fun validarCadastro(nome: String, email: String, senha: String, confirmar: String): String? {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) return "Preencha todos os campos"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Email inválido"
        if (senha.length < 8) return "A senha deve ter pelo menos 8 caracteres"
        if (senha != confirmar) return "As senhas não coincidem"
        return null
    }

    companion object {
        const val WEB_CLIENT_ID = "655065082008-o6bsql5hmocp4u2klp7ckk3bq0a9odhf.apps.googleusercontent.com"
    }
}
