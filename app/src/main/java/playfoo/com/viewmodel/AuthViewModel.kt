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
import com.google.firebase.auth.FirebaseAuth
import playfoo.com.data.remote.FirebaseAuthRepository
import playfoo.com.data.remote.FirestoreRepository
import playfoo.com.domain.AuthProvedor
import playfoo.com.domain.AuthUser
import playfoo.com.domain.TipoUsuario
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Carregando : AuthUiState()
    data class Sucesso(val usuario: AuthUser, val precisaEscolherTipo: Boolean = false) : AuthUiState()
    data class Erro(val mensagem: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val firestoreRepository: FirestoreRepository
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
            firebaseAuthRepository.loginEmail(email, senha)
                .onSuccess { user ->
                    val tipo = firestoreRepository.getUsuario(user.id)
                        .getOrNull()?.get("tipo")?.toString() ?: TipoUsuario.ALUNO
                    _state.value = AuthUiState.Sucesso(user.copy(tipo = tipo))
                }
                .onFailure { _state.value = AuthUiState.Erro("Email ou senha incorretos") }
        }
    }

    fun registrar(nome: String, email: String, senha: String, confirmar: String, codigoProfessor: String = "") {
        val erro = validarCadastro(nome, email, senha, confirmar)
        if (erro != null) { _state.value = AuthUiState.Erro(erro); return }

        val tipo = if (codigoProfessor.trim().uppercase() == TipoUsuario.CODIGO_PROFESSOR)
            TipoUsuario.GESTOR else TipoUsuario.ALUNO
        viewModelScope.launch {
            _state.value = AuthUiState.Carregando
            firebaseAuthRepository.registrar(nome, email, senha)
                .onSuccess { user ->
                    firestoreRepository.salvarUsuario(
                        id = user.id,
                        nome = nome,
                        email = email,
                        tipo = tipo
                    )
                    _state.value = AuthUiState.Sucesso(user.copy(tipo = tipo))
                }
                .onFailure { _state.value = AuthUiState.Erro(it.localizedMessage ?: "Erro ao criar conta") }
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
                    firebaseAuthRepository.loginGoogle(googleCred.idToken)
                        .onSuccess { user ->
                            val usuarioData = firestoreRepository.getUsuario(user.id)
                            if (usuarioData.isSuccess) {
                                val tipo = usuarioData.getOrNull()?.get("tipo")?.toString() ?: TipoUsuario.ALUNO
                                _state.value = AuthUiState.Sucesso(user.copy(tipo = tipo))
                            } else {
                                _state.value = AuthUiState.Sucesso(user, precisaEscolherTipo = true)
                            }
                        }
                        .onFailure { _state.value = AuthUiState.Erro(it.localizedMessage ?: "Erro no login com Google") }
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

    fun entrarComoConvidado() {
        val convidado = AuthUser(
            id       = "convidado",
            nome     = "Convidado",
            email    = "",
            provedor = AuthProvedor.EMAIL,
            tipo     = TipoUsuario.CONVIDADO
        )
        _state.value = AuthUiState.Sucesso(convidado)
    }

    fun limparEstado() { _state.value = AuthUiState.Idle }

    fun finalizarCadastroGoogle(tipo: String, codigoProfessor: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val tipoFinal = if (tipo == TipoUsuario.GESTOR &&
                            codigoProfessor.trim().uppercase() == TipoUsuario.CODIGO_PROFESSOR)
            TipoUsuario.GESTOR else TipoUsuario.ALUNO
        viewModelScope.launch {
            _state.value = AuthUiState.Carregando
            firestoreRepository.salvarUsuario(
                id    = firebaseUser.uid,
                nome  = firebaseUser.displayName ?: "Usuário",
                email = firebaseUser.email ?: "",
                tipo  = tipoFinal
            )
            val authUser = AuthUser(
                id       = firebaseUser.uid,
                nome     = firebaseUser.displayName ?: "Usuário",
                email    = firebaseUser.email ?: "",
                provedor = AuthProvedor.GOOGLE,
                tipo     = tipoFinal
            )
            _state.value = AuthUiState.Sucesso(authUser)
        }
    }

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
