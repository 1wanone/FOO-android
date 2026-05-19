package playfoo.com.data.local

import android.content.SharedPreferences
import playfoo.com.domain.AuthProvedor
import playfoo.com.domain.AuthUser
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    private val prefs: SharedPreferences
) {

    fun getUsuario(): AuthUser? {
        val id = prefs.getString("auth_id", null) ?: return null
        val provedorStr = prefs.getString("auth_provedor", AuthProvedor.EMAIL.name)
        return AuthUser(
            id       = id,
            nome     = prefs.getString("auth_nome", "") ?: "",
            email    = prefs.getString("auth_email", "") ?: "",
            provedor = runCatching { AuthProvedor.valueOf(provedorStr ?: "") }
                           .getOrDefault(AuthProvedor.EMAIL)
        )
    }

    fun temContaEmail(email: String): Boolean =
        prefs.contains("conta_${email.trim().lowercase()}_id")

    fun registrar(email: String, nome: String, senha: String) {
        val key = email.trim().lowercase()
        val id = UUID.randomUUID().toString()
        prefs.edit()
            .putString("conta_${key}_id", id)
            .putString("conta_${key}_nome", nome)
            .putString("conta_${key}_senha", senha)
            .putString("auth_id", id)
            .putString("auth_nome", nome)
            .putString("auth_email", email.trim())
            .putString("auth_provedor", AuthProvedor.EMAIL.name)
            .apply()
    }

    fun loginEmail(email: String, senha: String): Boolean {
        val key = email.trim().lowercase()
        val senhaSalva = prefs.getString("conta_${key}_senha", null) ?: return false
        if (senhaSalva != senha) return false
        val id   = prefs.getString("conta_${key}_id", null) ?: return false
        val nome = prefs.getString("conta_${key}_nome", "") ?: ""
        prefs.edit()
            .putString("auth_id", id)
            .putString("auth_nome", nome)
            .putString("auth_email", email.trim())
            .putString("auth_provedor", AuthProvedor.EMAIL.name)
            .apply()
        return true
    }

    fun loginGoogle(email: String, nome: String): AuthUser {
        val key = email.trim().lowercase()
        val id = prefs.getString("conta_${key}_id", null) ?: UUID.randomUUID().toString()
        prefs.edit()
            .putString("conta_${key}_id", id)
            .putString("conta_${key}_nome", nome)
            .putString("auth_id", id)
            .putString("auth_nome", nome)
            .putString("auth_email", email.trim())
            .putString("auth_provedor", AuthProvedor.GOOGLE.name)
            .apply()
        return AuthUser(id = id, nome = nome, email = email.trim(), provedor = AuthProvedor.GOOGLE)
    }

    fun logout() {
        prefs.edit()
            .remove("auth_id")
            .remove("auth_nome")
            .remove("auth_email")
            .remove("auth_provedor")
            .apply()
    }
}
