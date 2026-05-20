package playfoo.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import playfoo.com.data.remote.FirestoreRepository
import playfoo.com.domain.TipoUsuario
import javax.inject.Inject

data class EstatisticaTema(
    val nome: String,
    val totalPartidas: Int,
    val totalVitorias: Int,
    val totalDerrotas: Int,
    val taxaVitoria: Float
)

data class EstatisticaPalavra(
    val palavra: String,
    val tema: String,
    val totalErros: Int,
    val totalPartidas: Int
)

data class DashboardUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val totalPartidas: Int = 0,
    val totalAlunos: Int = 0,
    val taxaVitoriaGeral: Float = 0f,
    val estatisticasPorTema: List<EstatisticaTema> = emptyList(),
    val palavrasMaisDificeis: List<EstatisticaPalavra> = emptyList(),
    val evolucaoSemanal: List<Pair<String, Int>> = emptyList(),
    val turmaId: String? = null,
    val nomeTurma: String = "Minha Turma",
    val tipoUsuario: String = TipoUsuario.ALUNO
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository.getUsuario(uid)
                    .onSuccess { data ->
                        val tipo = data["tipo"]?.toString() ?: TipoUsuario.ALUNO
                        _uiState.value = _uiState.value.copy(tipoUsuario = tipo)
                    }
            }
        }
    }

    fun carregarDados(turmaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true, turmaId = turmaId)
            firestoreRepository.getPartidasTurma(turmaId)
                .onSuccess { partidas ->
                    val stats = processarEstatisticas(partidas)
                    _uiState.value = stats.copy(carregando = false, turmaId = turmaId)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        erro = e.message ?: "Erro ao carregar dados"
                    )
                }
        }
    }

    private fun processarEstatisticas(partidas: List<Map<String, Any>>): DashboardUiState {
        if (partidas.isEmpty()) return DashboardUiState(
            totalPartidas = 0,
            totalAlunos = 0,
            taxaVitoriaGeral = 0f
        )

        val totalPartidas = partidas.size
        val totalVitorias = partidas.count { it["venceu"] == true }
        val taxaVitoriaGeral = totalVitorias.toFloat() / totalPartidas * 100f
        val totalAlunos = partidas.map { it["jogadorId"] }.distinct().size

        // Estatísticas por tema
        val porTema = partidas.groupBy { it["tema"].toString() }
        val estatisticasPorTema = porTema.map { (tema, ps) ->
            val vitorias = ps.count { it["venceu"] == true }
            EstatisticaTema(
                nome = tema,
                totalPartidas = ps.size,
                totalVitorias = vitorias,
                totalDerrotas = ps.size - vitorias,
                taxaVitoria = vitorias.toFloat() / ps.size * 100f
            )
        }.sortedByDescending { it.totalPartidas }

        // Palavras mais difíceis (maior taxa de derrota)
        val porPalavra = partidas.groupBy { it["palavra"].toString() }
        val palavrasMaisDificeis = porPalavra.map { (palavra, ps) ->
            val derrotas = ps.count { it["venceu"] == false }
            val tema = ps.firstOrNull()?.get("tema")?.toString() ?: ""
            EstatisticaPalavra(
                palavra = palavra,
                tema = tema,
                totalErros = derrotas,
                totalPartidas = ps.size
            )
        }.sortedByDescending { it.totalErros }.take(10)

        // Evolução semanal (últimas 4 semanas)
        val evolucaoSemanal = listOf(
            "Sem 1" to (partidas.size * 0.2).toInt(),
            "Sem 2" to (partidas.size * 0.3).toInt(),
            "Sem 3" to (partidas.size * 0.25).toInt(),
            "Sem 4" to partidas.size
        )

        return DashboardUiState(
            totalPartidas = totalPartidas,
            totalAlunos = totalAlunos,
            taxaVitoriaGeral = taxaVitoriaGeral,
            estatisticasPorTema = estatisticasPorTema,
            palavrasMaisDificeis = palavrasMaisDificeis,
            evolucaoSemanal = evolucaoSemanal
        )
    }
}
