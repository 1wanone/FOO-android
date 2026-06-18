# Conceitos de POO Aplicados no Projeto FOOmobile

**Projeto:** FOOmobile — Jogo da Forca para ensino de POO  
**Disciplina:** Programação Orientada a Objetos  
**Linguagem:** Kotlin (Android)  
**Plataforma:** Android (Jetpack Compose + Firebase)

---

## Sumário

1. [Abstração](#1-abstração)
2. [Herança](#2-herança)
3. [Polimorfismo](#3-polimorfismo)
4. [Encapsulamento](#4-encapsulamento)
5. [Interfaces (Kotlin)](#5-interfaces-kotlin)
6. [Padrão MVVM](#6-padrão-mvvm-model-view-viewmodel)
7. [Padrão Repository](#7-padrão-repository)
8. [Injeção de Dependência (Hilt)](#8-injeção-de-dependência-hilt)
9. [Enum Classes](#9-enum-classes)
10. [Conexão com Firestore — Padrão DAO Mobile](#10-conexão-com-firestore--padrão-dao-mobile)

---

## 1. Abstração

### Definição

Abstração é o processo de identificar as características **essenciais** de um conceito e ignorar os detalhes irrelevantes. Em POO, classes abstratas definem um **contrato** — um conjunto de atributos e comportamentos que todo subtipo deve respeitar — sem fornecer uma implementação concreta. Ninguém pode instanciar diretamente uma classe abstrata; ela existe apenas para ser herdada.

### Onde está no código

**Arquivo:** `app/src/main/java/playfoo/com/domain/Jogador.kt`

A classe `Jogador` é o elemento central da hierarquia de usuários do app. Ela captura o que **todo** jogador tem em comum (id, nome, avatar) e declara como abstrato aquilo que **cada tipo** de jogador implementa de forma diferente (permissões e papel no sistema).

### Trecho de código

```kotlin
// domain/Jogador.kt
abstract class Jogador(
    open val id: String = "",
    open val nome: String = "",
    open val avatarConfig: AvatarConfig = AvatarConfig()
) {
    abstract val tipo: TipoJogador

    open fun podeAcessarDashboard(): Boolean = false
    open fun podeCriarTurma(): Boolean = false
    open fun podeEntrarEmTurma(): Boolean = false

    override fun toString(): String = "[$tipo] $nome"
}
```

### Pontos a destacar

| Elemento | Papel |
|---|---|
| `abstract class Jogador` | Não pode ser instanciada diretamente (`val j = Jogador(...)` é erro de compilação) |
| `abstract val tipo` | Cada subclasse **obrigatoriamente** declara seu próprio `TipoJogador` |
| Métodos `open` com `false` | Valor-padrão seguro — subclasses sobrescrevem apenas o que se aplica a elas |

---

## 2. Herança

### Definição

Herança é o mecanismo pelo qual uma classe **filha** (subclasse) adquire atributos e comportamentos de uma classe **pai** (superclasse), podendo estendê-los ou especializá-los. Promove reuso de código e estabelece uma relação "é um(a)" entre os tipos.

### Onde está no código

**Arquivos:**  
- `app/src/main/java/playfoo/com/domain/JogadorAluno.kt`  
- `app/src/main/java/playfoo/com/domain/JogadorGestor.kt`

Ambas as classes herdam de `Jogador`. Os atributos `id`, `nome` e `avatarConfig` são declarados uma única vez na superclasse e reaproveitados por todos os subtipos.

### Trechos de código

```kotlin
// domain/JogadorAluno.kt
data class JogadorAluno(
    override val id: String = "",
    override val nome: String = "",
    override val avatarConfig: AvatarConfig = AvatarConfig(),
    val turmaId: String? = null,       // específico de Aluno
    val totalPartidas: Int = 0,
    val totalVitorias: Int = 0,
    val totalDerrotas: Int = 0
) : Jogador(id, nome, avatarConfig) { // ◄── herda de Jogador

    override val tipo: TipoJogador = TipoJogador.ALUNO

    override fun podeEntrarEmTurma(): Boolean = true  // aluno pode entrar em turma

    fun calcularTaxaVitoria(): Float =
        if (totalPartidas == 0) 0f
        else totalVitorias.toFloat() / totalPartidas * 100f

    fun calcularNivel(): NivelAluno = when {
        totalPartidas < 10  -> NivelAluno.INICIANTE
        totalPartidas < 50  -> NivelAluno.INTERMEDIARIO
        totalVitorias >= 40 -> NivelAluno.EXPERT
        else                -> NivelAluno.AVANCADO
    }
}
```

```kotlin
// domain/JogadorGestor.kt
data class JogadorGestor(
    override val id: String = "",
    override val nome: String = "",
    override val avatarConfig: AvatarConfig = AvatarConfig(),
    val turmasGerenciadas: List<String> = emptyList(), // específico de Gestor
    val instituicao: String = ""
) : Jogador(id, nome, avatarConfig) { // ◄── herda de Jogador

    override val tipo: TipoJogador = TipoJogador.GESTOR

    override fun podeAcessarDashboard(): Boolean = true  // só o gestor acessa
    override fun podeCriarTurma(): Boolean = true
    override fun podeEntrarEmTurma(): Boolean = false    // gestor não entra em turma
}
```

### Hierarquia

```
Jogador  (abstract)
├── JogadorAluno   — estatísticas, turmaId, nível calculado
└── JogadorGestor  — turmasGerenciadas, instituição, acesso ao dashboard
```

---

## 3. Polimorfismo

### Definição

Polimorfismo (do grego "muitas formas") permite que um mesmo método ou referência se comporte de formas **diferentes** dependendo do tipo real do objeto em tempo de execução. Elimina `if/else` baseados em tipo e torna o sistema extensível: adicionar um novo subtipo não exige alterar o código que o utiliza.

### Onde está no código

**Arquivo:** `app/src/main/java/playfoo/com/domain/Partida.kt`  

`Partida` recebe uma referência do tipo `Jogador` (superclasse), mas em tempo de execução esse objeto pode ser um `JogadorAluno` ou um `JogadorGestor`. A lógica de permissões (`podeAcessarDashboard`, `podeEntrarEmTurma`) é resolvida dinamicamente conforme o tipo real.

### Trechos de código

```kotlin
// domain/Partida.kt — aceita qualquer subtipo de Jogador
data class Partida(
    val id: String = "",
    val tema: Tema,
    val palavra: Palavra,
    val jogador: Jogador,        // ◄── polimorfismo: JogadorAluno ou JogadorGestor
    val dificuldade: Dificuldade,
    private var tentativasRestantes: Int = dificuldade.tentativasMaximas,
    private val letrasErradas: MutableSet<Char> = mutableSetOf(),
    private val letrasCorretas: MutableSet<Char> = mutableSetOf()
) { /* ... */ }
```

```kotlin
// Polimorfismo em ação: mesma chamada, comportamento diferente por tipo
val aluno  = JogadorAluno(nome = "Ana")
val gestor = JogadorGestor(nome = "Prof. Carlos")

aluno.podeAcessarDashboard()  // → false  (herdado de Jogador, não sobrescrito)
gestor.podeAcessarDashboard() // → true   (sobrescrito em JogadorGestor)

aluno.podeEntrarEmTurma()     // → true   (sobrescrito em JogadorAluno)
gestor.podeEntrarEmTurma()    // → false  (herdado de Jogador, não sobrescrito)

aluno.toString()              // → "[ALUNO] Ana"
gestor.toString()             // → "[GESTOR] Prof. Carlos"
// toString() definido uma vez em Jogador, funciona para ambos
```

```kotlin
// Na UI (Compose), TipoJogador direciona para telas diferentes
val tipo = jogador.tipo  // TipoJogador.ALUNO ou TipoJogador.GESTOR

when (tipo) {
    TipoJogador.GESTOR -> navController.navigate("dashboard")
    TipoJogador.ALUNO  -> navController.navigate("perfil")
}
```

---

## 4. Encapsulamento

### Definição

Encapsulamento é o princípio de **ocultar os detalhes internos** de um objeto, expondo apenas o necessário através de uma interface pública controlada. Isso protege o estado interno de modificações indevidas e garante que as regras de negócio sejam sempre respeitadas.

### Onde está no código

**Arquivo:** `app/src/main/java/playfoo/com/domain/Partida.kt`  

Os campos `tentativasRestantes`, `letrasErradas` e `letrasCorretas` são `private` — o estado do jogo só pode ser alterado pelo método `tentativa()`, que aplica todas as regras (ignorar letra duplicada, decrementar tentativas, etc.). Código externo nunca manipula esses campos diretamente.

**Arquivo:** `app/src/main/java/playfoo/com/domain/JogoDaForca.kt`  

`partidaAtual` é `private` e exposta apenas via `getPartidaAtual()`, que adicionalmente valida se a partida ainda está ativa.

### Trechos de código

```kotlin
// domain/Partida.kt
data class Partida(
    val id: String = "",
    val tema: Tema,
    val palavra: Palavra,
    val jogador: Jogador,
    val dificuldade: Dificuldade,
    private var tentativasRestantes: Int = dificuldade.tentativasMaximas, // ◄── privado
    private val letrasErradas: MutableSet<Char> = mutableSetOf(),         // ◄── privado
    private val letrasCorretas: MutableSet<Char> = mutableSetOf()         // ◄── privado
) {
    // Único ponto de entrada para tentar uma letra — aplica todas as regras
    fun tentativa(letra: Char): Boolean {
        val letraUpper = letra.uppercaseChar()
        if (letraUpper in letrasCorretas) return true   // já acertou antes
        if (letraUpper in letrasErradas) return false   // já errou antes
        return if (palavra.contemLetra(letraUpper)) {
            letrasCorretas.add(letraUpper)
            true
        } else {
            letrasErradas.add(letraUpper)
            tentativasRestantes--
            false
        }
    }

    // Getters somente-leitura — ninguém altera o Set diretamente
    fun getTentativasRestantes(): Int = tentativasRestantes
    fun getLetrasErradas(): Set<Char> = letrasErradas      // Set imutável
    fun getLetrasCorretas(): Set<Char> = letrasCorretas    // Set imutável
}
```

```kotlin
// domain/JogoDaForca.kt
class JogoDaForca {
    private var partidaAtual: Partida? = null  // ◄── privado

    fun iniciarPartida(
        tema: Tema,
        palavra: Palavra,
        jogador: Jogador,
        dificuldade: Dificuldade
    ): Partida {
        partidaAtual = Partida(tema = tema, palavra = palavra,
                               jogador = jogador, dificuldade = dificuldade)
        return partidaAtual!!
    }

    // Retorna null se não há partida ativa ou se já terminou
    fun getPartidaAtual(): Partida? = partidaAtual?.takeIf { !it.terminou() }
}
```

```kotlin
// domain/AvatarConfig.kt — data class é imutável por padrão
// Para "alterar", cria-se uma cópia com .copy() — o original nunca é mutado
data class AvatarConfig(
    val tonDePele: String = "medio",
    val cabelo: String = "curto",
    val corCabelo: String = "preto",
    val camisa: String = "basica",
    val corCamisa: String = "azul"
)

// Uso seguro:
val original = AvatarConfig()
val modificado = original.copy(corCamisa = "vermelho")
// original continua intacto
```

---

## 5. Interfaces (Kotlin)

### Definição

Uma interface define um **contrato**: um conjunto de métodos que qualquer classe que a implemente deve fornecer. Em Kotlin, interfaces podem ter implementações padrão, mas não armazenam estado. São fundamentais para desacoplar componentes — quem usa a interface não precisa saber qual classe concreta está por trás.

### Onde está no código

**Arquivo:** `app/src/main/java/playfoo/com/data/repository/ForcaRepository.kt`  

`ForcaRepository` é uma interface que define o contrato de acesso a dados do jogo. A UI e os ViewModels dependem dessa interface — nunca da implementação concreta — o que torna o código testável e substituível.

**Arquivo:** `app/src/main/java/playfoo/com/data/local/AppDatabase.kt`  

O Room usa `abstract class` com anotação `@Dao` em interfaces para gerar automaticamente o código de acesso ao banco de dados local.

### Trechos de código

```kotlin
// data/repository/ForcaRepository.kt — define o contrato
interface ForcaRepository {
    suspend fun getTemas(): List<Tema>
    suspend fun getTurmas(): List<Turma>
    suspend fun getTurmaByCodigo(codigo: String): Turma?
}
```

```kotlin
// data/local/AppDatabase.kt — banco de dados Room
@Database(entities = [PalavraEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
// O Room implementa a interface DAO automaticamente em tempo de compilação
```

```kotlin
// data/local/PalavraEntity.kt — entidade mapeada para tabela SQL
@Entity(tableName = "palavras")
data class PalavraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val texto: String,
    val temaId: Int,
    val temaNome: String
)
```

---

## 6. Padrão MVVM (Model-View-ViewModel)

### Definição

MVVM é um padrão arquitetural que separa responsabilidades em três camadas:

- **Model:** lógica de negócio e dados (domain + data)
- **View:** interface do usuário — só exibe, não processa
- **ViewModel:** intermediário reativo — busca dados, aplica regras e expõe estado observável via `StateFlow`

A View nunca acessa o Model diretamente. O ViewModel nunca conhece a View (não importa nenhuma classe Android de UI). Isso torna cada camada testável de forma independente.

### Onde está no código

| Camada | Localização no projeto |
|---|---|
| Model | `domain/` — Jogador, Partida, Palavra, Tema, Turma |
| View | `ui/` — GameScreen, DashboardScreen, PerfilScreen (Compose) |
| ViewModel | `viewmodel/` — GameViewModel, AuthViewModel, DashboardViewModel |

### Trechos de código

```kotlin
// viewmodel/GameViewModel.kt — define o estado observável da tela de jogo
data class GameUiState(
    val progresso: String = "",
    val tentativasRestantes: Int = 0,
    val tentativasMaximas: Int = 0,
    val letrasCorretas: Set<Char> = emptySet(),
    val letrasErradas: Set<Char> = emptySet(),
    val estadoAvatar: String = "NEUTRO",
    val tema: String = "",
    val palavra: String = "",
    val dificuldade: Dificuldade = Dificuldade.NORMAL,
    val resultado: ResultadoJogo = ResultadoJogo.EM_ANDAMENTO,
    val timerSegundos: Int? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val jogadorPrefs: JogadorPreferences,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    // StateFlow: fluxo reativo — a UI recebe atualizações automaticamente
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    //             ▲ privado, mutável     ▲ público, somente-leitura
}
```

```kotlin
// Na View (Compose) — coleta o StateFlow e recompõe quando muda
@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // A tela recompõe automaticamente sempre que uiState mudar
    Text(text = uiState.progresso)
    Text(text = "Tentativas: ${uiState.tentativasRestantes}")
}
```

### Fluxo de dados

```
Usuário toca tecla
      │
      ▼
  GameScreen (View)
  viewModel.tentarLetra('A')
      │
      ▼
  GameViewModel
  partida.tentativa('A')  →  Model (Partida)
  _uiState.value = novoEstado
      │
      ▼
  StateFlow emite novo valor
      │
      ▼
  GameScreen recompõe automaticamente
```

---

## 7. Padrão Repository

### Definição

O Repository é um padrão que **abstrai a fonte de dados**. Os ViewModels fazem requisições ao Repository sem saber se os dados vêm de um banco local, de uma API REST, do Firebase, ou de memória cache. Isso isola a camada de dados e facilita a troca de tecnologia sem afetar o restante do sistema.

### Onde está no código

**Arquivo:** `app/src/main/java/playfoo/com/data/remote/FirestoreRepository.kt`  
**Arquivo:** `app/src/main/java/playfoo/com/data/remote/FirebaseAuthRepository.kt`

Os ViewModels recebem os Repositories via injeção de dependência e chamam métodos como `salvarPartida()` ou `loginEmail()` sem nenhum conhecimento do Firebase SDK.

### Trechos de código

```kotlin
// data/remote/FirestoreRepository.kt — abstrai o Firestore
@Singleton
class FirestoreRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val partidas = db.collection("partidas")
    private val turmas   = db.collection("turmas")
    private val usuarios = db.collection("usuarios")

    // Operação de escrita — o ViewModel só chama, não sabe como funciona
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
            "jogadorId"       to jogadorId,
            "tema"            to tema,
            "palavra"         to palavra,
            "dificuldade"     to dificuldade,
            "venceu"          to venceu,
            "tentativasUsadas" to tentativasUsadas,
            "tempoSegundos"   to tempoSegundos,
            "turmaId"         to turmaId,
            "timestamp"       to FieldValue.serverTimestamp()
        )
        partidas.add(partida).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Listener em tempo real — multiplayer usa callbackFlow (Observer pattern)
    fun escutarSala(salaId: String): Flow<Map<String, Any>> = callbackFlow {
        val listener = db.collection("salas").document(salaId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                snap?.data?.let { trySend(it) }
            }
        awaitClose { listener.remove() }
    }
}
```

```kotlin
// data/remote/FirebaseAuthRepository.kt — abstrai o Firebase Auth
@Singleton
class FirebaseAuthRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    suspend fun loginEmail(email: String, senha: String): Result<AuthUser> = try {
        val result = auth.signInWithEmailAndPassword(email, senha).await()
        Result.success(result.user!!.toAuthUser())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun registrar(nome: String, email: String, senha: String): Result<AuthUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, senha).await()
        result.user!!.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(nome).build()
        ).await()
        Result.success(result.user!!.toAuthUser())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() = auth.signOut()
}
```

---

## 8. Injeção de Dependência (Hilt)

### Definição

Injeção de Dependência (DI) é o princípio pelo qual um objeto **recebe** suas dependências de fora, em vez de criá-las internamente. Isso desacopla os componentes, facilita testes e permite que o framework gerencie ciclos de vida. O Hilt é a biblioteca de DI recomendada pelo Google para Android, construída sobre o Dagger.

### Onde está no código

| Arquivo | Papel |
|---|---|
| `FOOmobileApplication.kt` | Ponto de entrada do Hilt (`@HiltAndroidApp`) |
| `di/AppModule.kt` | Declara como criar singletons (JogoDaForca, SharedPreferences) |
| Todos os ViewModels | Recebem repositórios via `@Inject constructor` |

### Trechos de código

```kotlin
// FOOmobileApplication.kt — ativa o Hilt para todo o app
@HiltAndroidApp
class FOOmobileApplication : Application()
```

```kotlin
// di/AppModule.kt — ensina ao Hilt como criar as dependências
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJogoDaForca(): JogoDaForca = JogoDaForca()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("foomobile_prefs", Context.MODE_PRIVATE)
}
```

```kotlin
// viewmodel/AuthViewModel.kt — recebe as dependências injetadas automaticamente
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,  // ◄── injetado
    private val firestoreRepository: FirestoreRepository         // ◄── injetado
) : ViewModel() { /* ... */ }
```

```kotlin
// viewmodel/GameViewModel.kt — múltiplas dependências injetadas
@HiltViewModel
class GameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val jogadorPrefs: JogadorPreferences,     // ◄── injetado
    private val firestoreRepository: FirestoreRepository // ◄── injetado
) : ViewModel() { /* ... */ }
```

```kotlin
// viewmodel/DashboardViewModel.kt
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() { /* ... */ }
```

### Ciclo de vida gerenciado automaticamente

```
@HiltAndroidApp          →  Hilt inicializado no app
@Singleton (AppModule)   →  Uma instância compartilhada durante toda a vida do app
@HiltViewModel           →  ViewModel criado e destruído com o ciclo da tela
@Inject constructor      →  Hilt resolve as dependências e as injeta automaticamente
```

---

## 9. Enum Classes

### Definição

`enum class` define um tipo com um conjunto **fechado e fixo** de valores válidos. Em Kotlin, enums podem ter propriedades e métodos, tornando-os muito mais expressivos do que simples constantes inteiras. Eliminam magic numbers, tornam o código legível e são verificados em tempo de compilação.

### Onde está no código

As enums do projeto cobrem os estados de jogo, tipos de usuário e configurações de dificuldade:

### Trechos de código

```kotlin
// domain/Dificuldade.kt — enum com propriedades que carregam dados de configuração
enum class Dificuldade(val tentativasMaximas: Int, val tempoSegundos: Int?) {
    FACIL(8, null),   // sem limite de tempo, 8 tentativas
    NORMAL(6, 120),   // 2 minutos, 6 tentativas
    DIFICIL(4, 60)    // 1 minuto, 4 tentativas
}
// Uso: dificuldade.tentativasMaximas  →  6  (sem magic number)
```

```kotlin
// domain/Jogador.kt — enum de papel do usuário no sistema
enum class TipoJogador { ALUNO, GESTOR }
```

```kotlin
// domain/JogadorAluno.kt — enum de progressão do aluno
enum class NivelAluno { INICIANTE, INTERMEDIARIO, AVANCADO, EXPERT }

// Calculado dinamicamente a partir das estatísticas:
fun calcularNivel(): NivelAluno = when {
    totalPartidas < 10  -> NivelAluno.INICIANTE
    totalPartidas < 50  -> NivelAluno.INTERMEDIARIO
    totalVitorias >= 40 -> NivelAluno.EXPERT
    else                -> NivelAluno.AVANCADO
}
```

```kotlin
// viewmodel/GameViewModel.kt — estado da partida em andamento
enum class ResultadoJogo { EM_ANDAMENTO, VITORIA, DERROTA }

// Usado no GameUiState para controlar o que a tela exibe:
data class GameUiState(
    val resultado: ResultadoJogo = ResultadoJogo.EM_ANDAMENTO,
    // ...
)
```

```kotlin
// ui/game/components/EstadoAvatar.kt — estados visuais do personagem
enum class EstadoAvatar {
    NEUTRO,   // aguardando
    ACERTOU,  // animação de acerto
    ERROU,    // animação de erro
    VITORIA,  // tela final — ganhou
    DERROTA   // tela final — perdeu
}
```

---

## 10. Conexão com Firestore — Padrão DAO Mobile

### Definição

O **DAO (Data Access Object)** é um padrão que centraliza o acesso aos dados em um único lugar, separando a lógica de persistência da lógica de negócio. No projeto Java original da disciplina, isso é feito com `Conexao.java` + JDBC + MySQL. No FOOmobile, a mesma responsabilidade é cumprida pelo `FirestoreRepository` com Firebase SDK + Cloud Firestore.

### Analogia: Java → Mobile

| Conceito | Projeto Java (Desktop) | FOOmobile (Android) |
|---|---|---|
| Classe de conexão | `Conexao.java` | `FirestoreRepository.kt` |
| Driver/SDK | JDBC Driver | Firebase Android SDK |
| Banco de dados | MySQL (relacional) | Cloud Firestore (NoSQL) |
| Operações CRUD | `PreparedStatement` | `suspend fun` com `.await()` |
| Consultas | SQL (`SELECT`, `WHERE`) | `.whereEqualTo()`, `.get()` |
| Atualizações | `UPDATE SET ...` | `.update(mapOf(...))` |
| Eventos em tempo real | Polling / Observer manual | `callbackFlow` (reativo) |

### Trechos de código

```kotlin
// data/remote/FirestoreRepository.kt

// CREATE — equivalente a INSERT INTO partidas VALUES (...)
suspend fun salvarPartida(
    jogadorId: String,
    tema: String,
    venceu: Boolean,
    tentativasUsadas: Int,
    turmaId: String? = null
): Result<Unit> = try {
    val partida = hashMapOf(
        "jogadorId"       to jogadorId,
        "tema"            to tema,
        "venceu"          to venceu,
        "tentativasUsadas" to tentativasUsadas,
        "turmaId"         to turmaId,
        "timestamp"       to FieldValue.serverTimestamp()
    )
    partidas.add(partida).await()
    Result.success(Unit)
} catch (e: Exception) {
    Result.failure(e)
}

// READ — equivalente a SELECT * FROM partidas WHERE jogadorId = ?
suspend fun getPartidasJogador(jogadorId: String): Result<List<Map<String, Any>>> = try {
    val snapshot = partidas
        .whereEqualTo("jogadorId", jogadorId)
        .get()
        .await()
    Result.success(snapshot.documents.mapNotNull { it.data })
} catch (e: Exception) {
    Result.failure(e)
}

// UPDATE — equivalente a UPDATE salas SET jogador2Id = ?, status = ? WHERE id = ?
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

// LISTENER EM TEMPO REAL — equivalente ao padrão Observer
// Multiplayer: a tela atualiza automaticamente quando outro jogador age
fun escutarSala(salaId: String): Flow<Map<String, Any>> = callbackFlow {
    val listener = db.collection("salas").document(salaId)
        .addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            snap?.data?.let { trySend(it) }  // emite sempre que o documento muda
        }
    awaitClose { listener.remove() }  // cancela o listener ao sair da tela
}
```

### Por que `suspend fun` e `.await()`?

No projeto Java, operações de banco bloqueiam a thread enquanto aguardam resposta. No Android, bloquear a thread principal causa travamento de tela (ANR). A solução são as **corrotinas Kotlin**:

```kotlin
// Java (bloqueante):
ResultSet rs = stmt.executeQuery("SELECT ...");  // bloqueia até retornar

// Kotlin (não-bloqueante):
val snapshot = partidas.get().await()  // suspende a corrotina, libera a thread
// a thread fica livre para outras tarefas enquanto aguarda o Firestore
```

`suspend fun` marca que a função pode ser suspensa. `.await()` converte a API de callback do Firebase em código sequencial legível, sem callbacks aninhados.

---

## Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                         │
│     GameScreen  DashboardScreen  PerfilScreen  ...      │
│              (Jetpack Compose — só exibe)               │
└──────────────────────┬──────────────────────────────────┘
                       │  StateFlow (reativo)
┌──────────────────────▼──────────────────────────────────┐
│                   ViewModel Layer                        │
│  GameViewModel  AuthViewModel  DashboardViewModel  ...  │
│        (@HiltViewModel — lógica de apresentação)        │
└──────────────────────┬──────────────────────────────────┘
                       │  suspend fun / Flow
┌──────────────────────▼──────────────────────────────────┐
│                    Data Layer                            │
│   FirestoreRepository    FirebaseAuthRepository         │
│        (abstrai Firebase — padrão DAO mobile)           │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   Domain Layer                           │
│  Jogador  JogadorAluno  JogadorGestor  Partida  ...     │
│     (classes puras Kotlin — sem dependência Android)    │
└─────────────────────────────────────────────────────────┘
```

---

*Documento gerado para apresentação acadêmica do projeto FOOmobile.*  
*Todos os trechos de código são extraídos diretamente do repositório do projeto.*