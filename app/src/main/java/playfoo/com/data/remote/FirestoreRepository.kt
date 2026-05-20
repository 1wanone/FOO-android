package playfoo.com.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    // Coleções
    private val partidas = db.collection("partidas")
    private val turmas = db.collection("turmas")
    private val usuarios = db.collection("usuarios")

    // Salvar partida finalizada
    suspend fun salvarPartida(
        jogadorId: String,
        tema: String,
        palavra: String,
        dificuldade: String,
        venceu: Boolean,
        tentativasUsadas: Int,
        tempoSegundos: Int,
        turmaId: String? = null
    ): Result<Unit> = try {
        val partida = hashMapOf(
            "jogadorId" to jogadorId,
            "tema" to tema,
            "palavra" to palavra,
            "dificuldade" to dificuldade,
            "venceu" to venceu,
            "tentativasUsadas" to tentativasUsadas,
            "tempoSegundos" to tempoSegundos,
            "turmaId" to turmaId,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        partidas.add(partida).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Buscar partidas do jogador
    suspend fun getPartidasJogador(jogadorId: String): Result<List<Map<String, Any>>> = try {
        val snapshot = partidas
            .whereEqualTo("jogadorId", jogadorId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        Result.success(snapshot.documents.mapNotNull { it.data })
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Buscar partidas da turma (para dashboard do gestor)
    suspend fun getPartidasTurma(turmaId: String): Result<List<Map<String, Any>>> = try {
        val snapshot = partidas
            .whereEqualTo("turmaId", turmaId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        Result.success(snapshot.documents.mapNotNull { it.data })
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Criar turma
    suspend fun criarTurma(
        nome: String,
        codigo: String,
        gestorId: String
    ): Result<String> = try {
        val turma = hashMapOf(
            "nome" to nome,
            "codigo" to codigo,
            "gestorId" to gestorId,
            "membros" to emptyList<String>(),
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        val doc = turmas.add(turma).await()
        Result.success(doc.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Entrar em turma via código
    suspend fun entrarNaTurma(
        codigo: String,
        jogadorId: String
    ): Result<Map<String, Any>> = try {
        val snapshot = turmas
            .whereEqualTo("codigo", codigo)
            .get()
            .await()
        if (snapshot.isEmpty) {
            Result.failure(Exception("Turma não encontrada"))
        } else {
            val doc = snapshot.documents.first()
            turmas.document(doc.id)
                .update("membros", com.google.firebase.firestore.FieldValue.arrayUnion(jogadorId))
                .await()
            Result.success(doc.data!!.plus("id" to doc.id))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Salvar perfil do usuário
    suspend fun salvarUsuario(
        id: String,
        nome: String,
        email: String,
        tipo: String
    ): Result<Unit> = try {
        usuarios.document(id).set(
            hashMapOf(
                "nome" to nome,
                "email" to email,
                "tipo" to tipo,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Buscar perfil do usuário
    suspend fun getUsuario(id: String): Result<Map<String, Any>> = try {
        val doc = usuarios.document(id).get().await()
        if (doc.exists()) Result.success(doc.data!!)
        else Result.failure(Exception("Usuário não encontrado"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTurmasDoGestor(gestorId: String): Result<List<Map<String, Any>>> = try {
        val snapshot = turmas
            .whereEqualTo("gestorId", gestorId)
            .get()
            .await()
        Result.success(snapshot.documents.mapNotNull {
            it.data?.plus("id" to it.id)
        })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTurmaDoAluno(jogadorId: String): Result<Map<String, Any>?> = try {
        val snapshot = turmas
            .whereArrayContains("membros", jogadorId)
            .get()
            .await()
        val turma = snapshot.documents.firstOrNull()
        Result.success(turma?.data?.plus("id" to turma.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sairDaTurma(turmaId: String, jogadorId: String): Result<Unit> = try {
        turmas.document(turmaId)
            .update("membros", com.google.firebase.firestore.FieldValue.arrayRemove(jogadorId))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Estatísticas pessoais do aluno
    suspend fun getEstatisticasAluno(jogadorId: String): Result<Map<String, Any>> = try {
        val snapshot = partidas
            .whereEqualTo("jogadorId", jogadorId)
            .get()
            .await()

        val total = snapshot.size()
        val vitorias = snapshot.documents.count { it.getBoolean("venceu") == true }
        val derrotas = total - vitorias
        val taxaVitoria = if (total == 0) 0f else vitorias.toFloat() / total * 100f

        val palavrasErradas = snapshot.documents
            .filter { it.getBoolean("venceu") == false }
            .groupBy { it.getString("palavra") ?: "" }
            .filterKeys { it.isNotBlank() }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .associate { it.key to it.value }

        val temaFavorito = snapshot.documents
            .groupBy { it.getString("tema") ?: "" }
            .maxByOrNull { it.value.size }
            ?.key ?: "Nenhum"

        Result.success(mapOf(
            "totalPartidas"  to total,
            "totalVitorias"  to vitorias,
            "totalDerrotas"  to derrotas,
            "taxaVitoria"    to taxaVitoria,
            "palavrasErradas" to palavrasErradas,
            "temaFavorito"   to temaFavorito
        ))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
