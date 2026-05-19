package playfoo.com.data.local

import android.content.SharedPreferences
import playfoo.com.domain.AuthProvedor
import playfoo.com.domain.AuthUser
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_LOGGED = "auth_logged_in"
        private const val KEY_EMAIL  = "auth_email"
        private const val KEY_NOME   = "auth_nome"
        private const val KEY_PROV   = "auth_provedor"
        private const val KEY_HASH   = "auth_senha_hash"
    }

    fun estaLogado(): Boolean = prefs.getBoolean(KEY_LOGGED, false)

    fun getUsuario(): AuthUser? {
        if (!estaLogado()) return null
        return AuthUser(
            email    = prefs.getString(KEY_EMAIL, "") ?: "",
            nome     = prefs.getString(KEY_NOME, "")  ?: "",
            provedor = AuthProvedor.valueOf(prefs.getString(KEY_PROV, "EMAIL") ?: "EMAIL")
        )
    }

    fun temContaEmail(email: String): Boolean =
        prefs.getString(KEY_EMAIL, null) == email.trim() &&
        prefs.getString(KEY_PROV, null) == AuthProvedor.EMAIL.name

    fun registrar(email: String, nome: String, senha: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED, true)
            .putString(KEY_EMAIL, email.trim())
            .putString(KEY_NOME, nome.trim())
            .putString(KEY_PROV, AuthProvedor.EMAIL.name)
            .putString(KEY_HASH, hash(senha))
            .apply()
    }

    fun loginEmail(email: String, senha: String): Boolean {
        if (prefs.getString(KEY_EMAIL, null) != email.trim()) return false
        if (prefs.getString(KEY_PROV, null) != AuthProvedor.EMAIL.name) return false
        if (prefs.getString(KEY_HASH, null) != hash(senha)) return false
        prefs.edit().putBoolean(KEY_LOGGED, true).apply()
        return true
    }

    fun loginGoogle(email: String, nome: String): AuthUser {
        prefs.edit()
            .putBoolean(KEY_LOGGED, true)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NOME, nome)
            .putString(KEY_PROV, AuthProvedor.GOOGLE.name)
            .remove(KEY_HASH)
            .apply()
        return getUsuario()!!
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED, false).apply()
    }

    private fun hash(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}