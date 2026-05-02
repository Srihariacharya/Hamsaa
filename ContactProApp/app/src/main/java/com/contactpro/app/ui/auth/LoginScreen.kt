package com.contactpro.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.contactpro.app.network.ApiResult
import androidx.compose.ui.res.painterResource
import com.contactpro.app.R
import com.contactpro.app.ui.components.*
import com.contactpro.app.ui.theme.*
import com.contactpro.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError    by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val loginState by vm.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginState) {
        when (loginState) {
            is ApiResult.Success -> { vm.resetLoginState(); onLoginSuccess() }
            is ApiResult.Error   -> {
                snackbarHostState.showSnackbar((loginState as ApiResult.Error).message)
                vm.resetLoginState()
            }
            else -> Unit
        }
    }

    fun validate(): Boolean {
        var ok = true
        emailError = if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false; "Enter a valid email"
        } else null
        passwordError = if (password.length < 6) {
            ok = false; "Password must be at least 6 characters"
        } else null
        return ok
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hamsaa Logo
                Image(
                    painter = painterResource(R.drawable.logo_brand),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                
                Spacer(Modifier.height(24.dp))
                Text("HAMSAA", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold, color = HamsaaPrimary, letterSpacing = 2.sp)
                Text("Precision Contact Intelligence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary, textAlign = TextAlign.Center)

                Spacer(Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = LightSurface),
                    shape    = RoundedCornerShape(24.dp),
                    border   = BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Text("Welcome Back", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Login to your dashboard", style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary)
                        Spacer(Modifier.height(32.dp))

                        InputField(
                            value         = email,
                            onValueChange = { email = it; emailError = null },
                            label         = "Email Address",
                            leadingIcon   = Icons.Outlined.Email,
                            isError       = emailError != null,
                            errorMessage  = emailError
                        )
                        Spacer(Modifier.height(18.dp))

                        InputField(
                            value            = password,
                            onValueChange    = { password = it; passwordError = null },
                            label            = "Password",
                            leadingIcon      = Icons.Outlined.Lock,
                            isError          = passwordError != null,
                            errorMessage     = passwordError,
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            trailingIcon     = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle visibility",
                                        tint = TextHint
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(32.dp))

                        PrimaryButton(
                            text      = "Sign In",
                            onClick   = { if (validate()) vm.login(email, password) },
                            modifier  = Modifier.fillMaxWidth(),
                            isLoading = loginState is ApiResult.Loading
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New here?", color = TextSecondary)
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Create Account", fontWeight = FontWeight.Bold, color = HamsaaPrimary)
                    }
                }
            }
        }
    }
}
