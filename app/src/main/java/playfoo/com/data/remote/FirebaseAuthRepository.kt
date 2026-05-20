package playfoo.com.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import playfoo.com.domain.AuthUser
import playfoo.com.domain.AuthProvedor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    fun getUsuarioAtual(): AuthUser? = auth.currentUser?.toAuthUser()

    suspend fun loginEmail(email: String, senha: String): Result<AuthUser> = try {
        val result = auth.signInWithEmailAndPassword(email, senha).await()
        Result.success(result.user!!.toAuthUser())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun registrar(nome: String, email: String, senha: String): Result<AuthUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, senha).await()
        result.user!!.updateProfile(
            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(nome)
                .build()
        ).await()
        Result.success(result.user!!.toAuthUser())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun loginGoogle(idToken: String): Result<AuthUser> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        Result.success(result.user!!.toAuthUser())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun recuperarSenha(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() = auth.signOut()

    private fun FirebaseUser.toAuthUser() = AuthUser(
        id = uid,
        nome = displayName ?: "Usuário",
        email = email ?: "",
        provedor = if (providerData.any { it.providerId == "google.com" })
            AuthProvedor.GOOGLE else AuthProvedor.EMAIL
    )
}
