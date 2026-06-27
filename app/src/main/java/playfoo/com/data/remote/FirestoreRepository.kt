package playfoo.com.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class Aluno(
    val id: String,
    val nome: String,
    val partidas: Int,
    val vitorias: Int
)

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
            .get()
            .await()
        Result.success(snapshot.documents.mapNotNull { it.data })
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Buscar partidas da turma
    suspend fun getPartidasTurma(turmaId: String): Result<List<Map<String, Any>>> = try {
        android.util.Log.d("DASHBOARD", "Buscando partidas da turma: $turmaId")
        val snapshot = partidas
            .whereEqualTo("turmaId", turmaId)
            .get()
            .await()
        android.util.Log.d("DASHBOARD", "Partidas encontradas: ${snapshot.size()}")
        Result.success(snapshot.documents.mapNotNull { it.data })
    } catch (e: Exception) {
        android.util.Log.e("DASHBOARD", "Erro: ${e.message}")
        Result.failure(e)
    }

    // Buscar partidas de todos os membros (fallback para partidas sem turmaId)
    suspend fun getPartidasDosMembros(membros: List<String>): Result<List<Map<String, Any>>> {
        if (membros.isEmpty()) return Result.success(emptyList())
        return try {
            val todasPartidas = mutableListOf<Map<String, Any>>()
            membros.chunked(10).forEach { chunk ->
                val snapshot = partidas
                    .whereIn("jogadorId", chunk)
                    .get()
                    .await()
                todasPartidas.addAll(snapshot.documents.mapNotNull { it.data })
            }
            Result.success(todasPartidas)
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    suspend fun atualizarNomeUsuario(userId: String, novoNome: String): Result<Unit> = try {
        usuarios.document(userId).update("nome", novoNome).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun buscarNomeUsuario(uid: String): String {
        return try {
            val doc = usuarios.document(uid).get().await()
            doc.getString("nome")
                ?: doc.getString("email")?.substringBefore("@")
                ?: "Jogador ${uid.take(4)}"
        } catch (e: Exception) {
            "Jogador"
        }
    }

    suspend fun buscarNomesAlunos(uids: List<String>): Map<String, String> {
        if (uids.isEmpty()) return emptyMap()
        return uids.associate { uid -> uid to buscarNomeUsuario(uid) }
    }

    suspend fun buscarNomeProfessor(turmaId: String): String {
        return try {
            val turmaDoc = turmas.document(turmaId).get().await()
            val gestorId = turmaDoc.getString("gestorId") ?: return "Professor"
            buscarNomeUsuario(gestorId)
        } catch (e: Exception) {
            "Professor"
        }
    }

    suspend fun buscarAlunosDaTurma(turmaId: String): List<Aluno> {
        return try {
            val turmaDoc = turmas.document(turmaId).get().await()
            @Suppress("UNCHECKED_CAST")
            val membrosIds = turmaDoc.get("membros") as? List<String> ?: emptyList()

            membrosIds.mapNotNull { uid ->
                try {
                    val userDoc = usuarios.document(uid).get().await()
                    Aluno(
                        id       = uid,
                        nome     = buscarNomeUsuario(uid),
                        partidas = (userDoc.getLong("partidas") ?: 0L).toInt(),
                        vitorias = (userDoc.getLong("vitorias") ?: 0L).toInt()
                    )
                } catch (e: Exception) {
                    Aluno(id = uid, nome = "Jogador ${uid.take(4)}", partidas = 0, vitorias = 0)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarRankingTurma(turmaId: String): List<playfoo.com.domain.RankingJogador> {
        val alunos = buscarAlunosDaTurma(turmaId)
        return alunos
            .sortedByDescending { it.vitorias }
            .map { aluno ->
                playfoo.com.domain.RankingJogador(
                    id          = aluno.id,
                    nome        = aluno.nome,
                    vitorias    = aluno.vitorias,
                    partidas    = aluno.partidas,
                    taxaVitoria = if (aluno.partidas > 0)
                        aluno.vitorias.toFloat() / aluno.partidas * 100f
                    else 0f
                )
            }
    }

    suspend fun debugUsuario(uid: String) {
        try {
            val doc = usuarios.document(uid).get().await()
            android.util.Log.d("FIRESTORE_DEBUG", "=== Campos do usuário $uid ===")
            doc.data?.forEach { (key, value) ->
                android.util.Log.d("FIRESTORE_DEBUG", "  $key = $value")
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_DEBUG", "Erro ao ler usuário: ${e.message}")
        }
    }

    suspend fun debugTurma(turmaId: String) {
        try {
            val doc = turmas.document(turmaId).get().await()
            android.util.Log.d("FIRESTORE_DEBUG", "=== Campos da turma $turmaId ===")
            doc.data?.forEach { (key, value) ->
                android.util.Log.d("FIRESTORE_DEBUG", "  $key = $value")
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_DEBUG", "Erro ao ler turma: ${e.message}")
        }
    }

    suspend fun getTurmasDoGestor(gestorId: String): Result<List<Map<String, Any>>> = try {
        val snapshot = turmas
            .whereEqualTo("gestorId", gestorId)
            .get()
            .await()
        Result.success(snapshot.documents.mapNotNull { doc ->
            android.util.Log.d("TURMA", "membros: ${doc.data?.get("membros")}")
            doc.data?.plus("id" to doc.id)
        })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTurmaDoAluno(jogadorId: String): Result<Map<String, Any>?> = try {
        android.util.Log.d("FIRESTORE", "Buscando turma do aluno: $jogadorId")
        val snapshot = turmas
            .whereArrayContains("membros", jogadorId)
            .get()
            .await()
        val turma = snapshot.documents.firstOrNull()
        val result = turma?.data?.plus("id" to turma.id)
        android.util.Log.d("FIRESTORE", "Turma encontrada: ${result?.get("nome")}, id: ${result?.get("id")}")
        Result.success(result)
    } catch (e: Exception) {
        android.util.Log.e("FIRESTORE", "Erro ao buscar turma do aluno: ${e.message}")
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

    suspend fun getEstatisticasPorTemaAluno(jogadorId: String): Result<List<Map<String, Any>>> = try {
        val snapshot = partidas
            .whereEqualTo("jogadorId", jogadorId)
            .get()
            .await()
        val porTema = snapshot.documents
            .groupBy { it.getString("tema") ?: "" }
            .filterKeys { it.isNotBlank() }
            .map { (tema, docs) ->
                val vitorias = docs.count { it.getBoolean("venceu") == true }
                mapOf(
                    "tema" to tema,
                    "total" to docs.size,
                    "vitorias" to vitorias,
                    "derrotas" to (docs.size - vitorias)
                )
            }
        Result.success(porTema)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Multiplayer ──────────────────────────────────────────────────────────

    suspend fun criarSalaMultiplayer(
        jogador1Id: String,
        jogador1Nome: String,
        tema: String,
        palavra: String,
        dificuldade: String
    ): Result<Map<String, Any>> = try {
        val codigo = (100000..999999).random().toString()
        val sala = hashMapOf<String, Any?>(
            "codigo"        to codigo,
            "jogador1Id"    to jogador1Id,
            "jogador1Nome"  to jogador1Nome,
            "jogador2Id"    to null,
            "jogador2Nome"  to null,
            "tema"          to tema,
            "palavra"       to palavra,
            "dificuldade"   to dificuldade,
            "status"          to "aguardando",
            "turnoAtual"      to 1,
            "letrasReveladas" to "",
            "progresso"       to "",
            "tentativas1"     to null,
            "tentativas2"     to null,
            "letrasErradas1"  to "",
            "letrasErradas2"  to "",
            "vencedor"        to null,
            "versao"          to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "timestamp"       to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        val doc = db.collection("salas").add(sala).await()
        Result.success(mapOf("id" to doc.id, "codigo" to codigo))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSalaPorCodigo(codigo: String): Result<Pair<String, Map<String, Any>>> = try {
        val snapshot = db.collection("salas")
            .whereEqualTo("codigo", codigo)
            .whereEqualTo("status", "aguardando")
            .get()
            .await()
        if (snapshot.isEmpty)
            Result.failure(Exception("Sala não encontrada ou já iniciada"))
        else {
            val doc = snapshot.documents.first()
            Result.success(Pair(doc.id, doc.data!!))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun entrarNaSala(
        salaId: String,
        jogador2Id: String,
        jogador2Nome: String
    ): Result<Unit> = try {
        db.collection("salas").document(salaId).update(
            mapOf(
                "jogador2Id"   to jogador2Id,
                "jogador2Nome" to jogador2Nome,
                "status"       to "jogando"
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun atualizarProgressoSala(
        salaId: String,
        numero: Int,
        progresso: String,
        tentativas: Int,
        letrasErradas: String
    ): Result<Unit> = try {
        db.collection("salas").document(salaId).update(
            mapOf(
                "progresso$numero"    to progresso,
                "tentativas$numero"   to tentativas,
                "letrasErradas$numero" to letrasErradas
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun finalizarSala(salaId: String, vencedorId: String): Result<Unit> = try {
        db.collection("salas").document(salaId).update(
            mapOf(
                "status"   to "finalizada",
                "vencedor" to vencedorId
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun revelarLetra(
        salaId: String,
        novoProgresso: String,
        letrasReveladas: String,
        jogadorNumero: Int,
        tentativas: Int,
        letrasErradas: String
    ): Result<Unit> = try {
        db.collection("salas").document(salaId).update(
            mapOf(
                "letrasReveladas"           to letrasReveladas,
                "progresso"                 to novoProgresso,
                "tentativas$jogadorNumero"  to tentativas,
                "letrasErradas$jogadorNumero" to letrasErradas
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun reiniciarSala(
        salaId: String,
        novaPalavra: String,
        novoTema: String
    ): Result<Unit> = try {
        val delete = com.google.firebase.firestore.FieldValue.delete()
        db.collection("salas").document(salaId).update(
            mapOf(
                "palavra"         to novaPalavra,
                "tema"            to novoTema,
                "status"          to "jogando",
                "letrasReveladas" to "",
                "progresso"       to "",
                "tentativas1"     to delete,
                "tentativas2"     to delete,
                "letrasErradas1"  to "",
                "letrasErradas2"  to "",
                "turnoAtual"      to 1,
                "versao"          to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun passarTurno(salaId: String, proximoTurno: Int): Result<Unit> = try {
        db.collection("salas").document(salaId)
            .update("turnoAtual", proximoTurno)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cancelarSala(salaId: String): Result<Unit> = try {
        db.collection("salas").document(salaId)
            .update("status", "cancelada")
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun aceitarReversa(salaId: String, jogadorNumero: Int): Result<Unit> = try {
        db.collection("salas").document(salaId)
            .update("aceiteRevanche$jogadorNumero", true)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun resetarAceites(salaId: String): Result<Unit> = try {
        db.collection("salas").document(salaId)
            .update(mapOf(
                "aceiteRevanche1" to false,
                "aceiteRevanche2" to false
            ))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun escutarSala(salaId: String): Flow<Map<String, Any>> = callbackFlow {
        val listener = db.collection("salas").document(salaId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                snap?.data?.let { trySend(it) }
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────────────────────────────────────

    // Avatar

    suspend fun salvarAvatar(
        userId: String,
        avatarConfig: playfoo.com.domain.AvatarConfig
    ): Result<Unit> = try {
        usuarios.document(userId).update(mapOf(
            "avatar_tonDePele" to avatarConfig.tonDePele,
            "avatar_cabelo"    to avatarConfig.cabelo,
            "avatar_corCabelo" to avatarConfig.corCabelo,
            "avatar_camisa"    to avatarConfig.camisa,
            "avatar_corCamisa" to avatarConfig.corCamisa
        )).await()
        Result.success(Unit)
    } catch (e: Exception) {
        try {
            usuarios.document(userId).set(mapOf(
                "avatar_tonDePele" to avatarConfig.tonDePele,
                "avatar_cabelo"    to avatarConfig.cabelo,
                "avatar_corCabelo" to avatarConfig.corCabelo,
                "avatar_camisa"    to avatarConfig.camisa,
                "avatar_corCamisa" to avatarConfig.corCamisa
            ), com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e2: Exception) {
            Result.failure(e2)
        }
    }

    suspend fun getAvatar(userId: String): Result<playfoo.com.domain.AvatarConfig> = try {
        val doc = usuarios.document(userId).get().await()
        if (doc.exists()) {
            val config = playfoo.com.domain.AvatarConfig(
                tonDePele = doc.getString("avatar_tonDePele") ?: "medio",
                cabelo    = doc.getString("avatar_cabelo")    ?: "curto",
                corCabelo = doc.getString("avatar_corCabelo") ?: "preto",
                camisa    = doc.getString("avatar_camisa")    ?: "maniva",
                corCamisa = doc.getString("avatar_corCamisa") ?: "preto"
            )
            Result.success(config)
        } else {
            Result.success(playfoo.com.domain.AvatarConfig())
        }
    } catch (e: Exception) {
        Result.success(playfoo.com.domain.AvatarConfig())
    }

    // ─────────────────────────────────────────────────────────────────────────

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
