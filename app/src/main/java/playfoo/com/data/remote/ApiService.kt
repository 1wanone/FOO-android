package playfoo.com.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("temas")
    suspend fun getTemas(): List<Map<String, Any>>

    @GET("turmas/{codigo}")
    suspend fun getTurmaByCodigo(@Path("codigo") codigo: String): Map<String, Any>
}
