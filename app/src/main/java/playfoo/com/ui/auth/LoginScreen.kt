package playfoo.com.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import playfoo.com.viewmodel.AuthUiState
import playfoo.com.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current

    var modoLogin by remember { mutableStateOf(true) }
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Sucesso) {
            navController.navigateUp()
        }
    }

    val erroMensagem = (uiState as? AuthUiState.Erro)?.mensagem
    val carregando = uiState is AuthUiState.Carregando

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (modoLogin) "Entrar" else "Criar Conta") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = modoLogin,
                    onClick = { modoLogin = true; viewModel.limparEstado() },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    label = { Text("Entrar") }
                )
                SegmentedButton(
                    selected = !modoLogin,
                    onClick = { modoLogin = false; viewModel.limparEstado() },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    label = { Text("Criar Conta") }
                )
            }

            if (!modoLogin) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it; viewModel.limparEstado() },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.limparEstado() },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it; viewModel.limparEstado() },
                label = { Text("Senha (mínimo 8 caracteres)") },
                singleLine = true,
                visualTransformation = if (senhaVisivel) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            if (senhaVisivel) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (!modoLogin) {
                OutlinedTextField(
                    value = confirmarSenha,
                    onValueChange = { confirmarSenha = it; viewModel.limparEstado() },
                    label = { Text("Confirmar Senha") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            erroMensagem?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (modoLogin) viewModel.login(email, senha)
                    else viewModel.registrar(nome, email, senha, confirmarSenha)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !carregando
            ) {
                if (carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (modoLogin) "Entrar" else "Criar Conta")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("ou", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            OutlinedButton(
                onClick = { viewModel.loginGoogle(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !carregando
            ) {
                Text("Continuar com Google")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}