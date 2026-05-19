package playfoo.com.domain

data class AuthUser(
    val email: String,
    val nome: String,
    val provedor: AuthProvedor
)

enum class AuthProvedor { EMAIL, GOOGLE }