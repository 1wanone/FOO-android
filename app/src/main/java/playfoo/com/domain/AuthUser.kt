package playfoo.com.domain

data class AuthUser(
    val id: String,
    val nome: String,
    val email: String,
    val provedor: AuthProvedor = AuthProvedor.EMAIL
)
