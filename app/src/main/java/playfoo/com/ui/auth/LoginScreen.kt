package playfoo.com.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import playfoo.com.ui.components.BotaoCartoon
import playfoo.com.ui.components.BotaoCartoonTipo
import playfoo.com.ui.components.CardCartoon
import playfoo.com.ui.components.FundoTela
import playfoo.com.ui.components.TipoFundo
import playfoo.com.domain.TipoUsuario
import playfoo.com.viewmodel.AuthUiState
import playfoo.com.viewmodel.AuthViewModel

private enum class TelaAuth { LOGIN, REGISTRO, RECUPERAR_SENHA }

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.state.collectAsState()
    val context = LocalContext.current
    var telaAtual by remember { mutableStateOf(TelaAuth.LOGIN) }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Sucesso) {
            val usuario = (authState as AuthUiState.Sucesso).usuario
            val destino = if (usuario.tipo == TipoUsuario.GESTOR) "dashboard"
                          else "menu?tipo=${usuario.tipo}"
            navController.navigate(destino) {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
            viewModel.limparEstado()
        }
    }

    FundoTela(tipo = TipoFundo.MENU) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoPlaceholder()

            Spacer(Modifier.height(36.dp))

            AnimatedContent(
                targetState = telaAtual,
                transitionSpec = {
                    val para = targetState.ordinal > initialState.ordinal
                    val entrar = if (para) slideInHorizontally { it } + fadeIn()
                                 else      slideInHorizontally { -it } + fadeIn()
                    val sair   = if (para) slideOutHorizontally { -it } + fadeOut()
                                 else      slideOutHorizontally { it } + fadeOut()
                    entrar togetherWith sair
                },
                label = "transicao_tela_auth"
            ) { tela ->
                when (tela) {
                    TelaAuth.LOGIN -> CardLogin(
                        authState      = authState,
                        onLogin        = { email, senha -> viewModel.login(email, senha) },
                        onLoginGoogle  = { viewModel.loginGoogle(context) },
                        onIrRegistro   = { viewModel.limparEstado(); telaAtual = TelaAuth.REGISTRO },
                        onIrRecuperacao = { viewModel.limparEstado(); telaAtual = TelaAuth.RECUPERAR_SENHA }
                    )
                    TelaAuth.REGISTRO -> CardRegistro(
                        authState    = authState,
                        onRegistrar  = { nome, email, senha, confirmar, codigoProfessor ->
                            viewModel.registrar(nome, email, senha, confirmar, codigoProfessor)
                        },
                        onVoltarLogin = { viewModel.limparEstado(); telaAtual = TelaAuth.LOGIN }
                    )
                    TelaAuth.RECUPERAR_SENHA -> CardRecuperarSenha(
                        onVoltarLogin = { viewModel.limparEstado(); telaAtual = TelaAuth.LOGIN }
                    )
                }
            }
        }
    }
}

// SLOT DE ASSET — quando o desenhista entregar a logo, substituir por:
// Image(painterResource(R.drawable.logo), contentDescription = "FOOmobile", modifier = Modifier.height(100.dp))
@Composable
private fun LogoPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "🎮", fontSize = 56.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "FOOmobile",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF6C63FF),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Jogo da Forca",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CardLogin(
    authState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onLoginGoogle: () -> Unit,
    onIrRegistro: () -> Unit,
    onIrRecuperacao: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var erroLocal by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val carregando = authState is AuthUiState.Carregando
    val erroExibido = erroLocal ?: (authState as? AuthUiState.Erro)?.mensagem

    fun validar(): Boolean {
        if (email.isBlank() || senha.isBlank()) { erroLocal = "Preencha todos os campos"; return false }
        if (!email.contains('@'))               { erroLocal = "Email inválido"; return false }
        if (senha.length < 6)                   { erroLocal = "Senha deve ter pelo menos 6 caracteres"; return false }
        erroLocal = null
        return true
    }

    CardCartoon(modifier = Modifier.fillMaxWidth()) {
        Text("Entrar", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        CampoTexto(
            valor = email,
            onValorChange = { email = it; erroLocal = null },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF6C63FF)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(Modifier.height(12.dp))

        CampoSenha(
            valor = senha,
            onValorChange = { senha = it; erroLocal = null },
            label = "Senha",
            visivel = senhaVisivel,
            onToggleVisibilidade = { senhaVisivel = !senhaVisivel },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (validar()) onLogin(email, senha)
            })
        )

        erroExibido?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(4.dp))

        TextButton(
            onClick = onIrRecuperacao,
            modifier = Modifier.align(Alignment.End),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Esqueci minha senha", color = Color(0xFF6C63FF), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))

        if (carregando) {
            CircularProgressIndicator(
                color = Color(0xFF6C63FF),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            BotaoCartoon(
                texto = "Entrar",
                onClick = { if (validar()) onLogin(email, senha) },
                tipo = BotaoCartoonTipo.PRIMARIO,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.25f))
            Text("  ou  ", color = Color.White.copy(alpha = 0.45f), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.25f))
        }

        Spacer(Modifier.height(20.dp))

        BotaoCartoon(
            texto = "Entrar com Google",
            onClick = onLoginGoogle,
            tipo = BotaoCartoonTipo.SECUNDARIO,
            habilitado = !carregando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Não tem conta? ", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onIrRegistro, contentPadding = PaddingValues(0.dp)) {
                Text("Cadastre-se", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun CardRegistro(
    authState: AuthUiState,
    onRegistrar: (String, String, String, String, String) -> Unit,
    onVoltarLogin: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var codigoProfessor by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarVisivel by remember { mutableStateOf(false) }
    var erroLocal by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val carregando = authState is AuthUiState.Carregando
    val erroExibido = erroLocal ?: (authState as? AuthUiState.Erro)?.mensagem

    fun validar(): Boolean = when {
        nome.isBlank() || email.isBlank() || senha.isBlank() || confirmar.isBlank() ->
            { erroLocal = "Preencha todos os campos"; false }
        !email.contains('@') ->
            { erroLocal = "Email inválido"; false }
        senha.length < 6 ->
            { erroLocal = "Senha deve ter pelo menos 6 caracteres"; false }
        senha != confirmar ->
            { erroLocal = "As senhas não coincidem"; false }
        else -> { erroLocal = null; true }
    }

    CardCartoon(modifier = Modifier.fillMaxWidth()) {
        Text("Criar conta", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        CampoTexto(
            valor = nome,
            onValorChange = { nome = it; erroLocal = null },
            label = "Nome",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF6C63FF)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(Modifier.height(12.dp))

        CampoTexto(
            valor = email,
            onValorChange = { email = it; erroLocal = null },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF6C63FF)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(Modifier.height(12.dp))

        CampoSenha(
            valor = senha,
            onValorChange = { senha = it; erroLocal = null },
            label = "Senha",
            visivel = senhaVisivel,
            onToggleVisibilidade = { senhaVisivel = !senhaVisivel },
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(Modifier.height(12.dp))

        CampoSenha(
            valor = confirmar,
            onValorChange = { confirmar = it; erroLocal = null },
            label = "Confirmar senha",
            visivel = confirmarVisivel,
            onToggleVisibilidade = { confirmarVisivel = !confirmarVisivel },
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(Modifier.height(12.dp))

        CampoTexto(
            valor = codigoProfessor,
            onValorChange = { codigoProfessor = it; erroLocal = null },
            label = "Código do professor (opcional)",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF6C63FF)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (validar()) onRegistrar(nome, email, senha, confirmar, codigoProfessor)
            })
        )
        Text(
            text = "Deixe em branco se você é aluno",
            color = Color.White.copy(alpha = 0.45f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )

        erroExibido?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        if (carregando) {
            CircularProgressIndicator(
                color = Color(0xFF6C63FF),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            BotaoCartoon(
                texto = "Criar conta",
                onClick = { if (validar()) onRegistrar(nome, email, senha, confirmar, codigoProfessor) },
                tipo = BotaoCartoonTipo.PRIMARIO,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("Já tem conta? ", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onVoltarLogin, contentPadding = PaddingValues(0.dp)) {
                Text("Entrar", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun CardRecuperarSenha(onVoltarLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf(false) }
    var erroLocal by remember { mutableStateOf<String?>(null) }

    fun enviar() {
        if (email.isBlank() || !email.contains('@')) {
            erroLocal = "Informe um email válido"
        } else {
            erroLocal = null
            enviado = true
        }
    }

    CardCartoon(modifier = Modifier.fillMaxWidth()) {
        Text("Recuperar senha", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Informe seu email e enviaremos um link para redefinir sua senha.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.65f)
        )

        Spacer(Modifier.height(24.dp))

        if (enviado) {
            Text(
                text = "Link enviado! Verifique sua caixa de entrada.",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            CampoTexto(
                valor = email,
                onValorChange = { email = it; erroLocal = null },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF6C63FF)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { enviar() })
            )

            erroLocal?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            BotaoCartoon(
                texto = "Enviar link de recuperação",
                onClick = { enviar() },
                tipo = BotaoCartoonTipo.PRIMARIO,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onVoltarLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("← Voltar ao login", color = Color(0xFF6C63FF))
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = campoAuthColors()
    )
}

@Composable
private fun CampoSenha(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    visivel: Boolean,
    onToggleVisibilidade: () -> Unit,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF6C63FF)) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibilidade) {
                Icon(
                    imageVector = if (visivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visivel) "Ocultar senha" else "Mostrar senha",
                    tint = Color.White.copy(alpha = 0.55f)
                )
            }
        },
        visualTransformation = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = campoAuthColors()
    )
}

@Composable
private fun campoAuthColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor    = Color(0xFF6C63FF),
    unfocusedBorderColor  = Color.White.copy(alpha = 0.28f),
    focusedLabelColor     = Color(0xFF6C63FF),
    unfocusedLabelColor   = Color.White.copy(alpha = 0.5f),
    cursorColor           = Color(0xFF6C63FF),
    focusedTextColor      = Color.White,
    unfocusedTextColor    = Color.White,
    focusedLeadingIconColor   = Color(0xFF6C63FF),
    unfocusedLeadingIconColor = Color(0xFF6C63FF)
)
