package playfoo.com.data.repository

import playfoo.com.domain.Tema
import playfoo.com.domain.Turma

interface ForcaRepository {
    suspend fun getTemas(): List<Tema>
    suspend fun getTurmas(): List<Turma>
    suspend fun getTurmaByCodigo(codigo: String): Turma?
}
