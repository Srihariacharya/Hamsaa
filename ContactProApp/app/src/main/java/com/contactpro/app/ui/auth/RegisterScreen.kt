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
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var nameError     by remember { mutableStateOf<String?>(null) }
    var emailError    by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError  by remember { mutableStateOf<String?>(null) }

    val registerState by vm.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registerState) {
        when (registerState) {
            is ApiResult.Success -> { vm.resetRegisterState(); onRegisterSuccess() }
            is ApiResult.Error   -> {
                snackbarHostState.showSnackbar((registerState as ApiResult.Error).message)
                vm.resetRegisterState()
            }
            else -> Unit
        }
    }

    fun validate(): Boolean {
        var ok = true
        nameError  = if (name.isBlank()) { ok = false; "Name is required" } else null
        emailError = if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false; "Enter a valid email" } else null
        passwordError = if (password.length < 6) { ok = false; "Min 6 characters" } else null
        confirmError  = if (confirm != password) { ok = false; "Passwords don't match" } else null
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
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_brand),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(Modifier.height(16.dp))
                Text("Join Hamsaa", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold, color = HamsaaPrimary, letterSpacing = 2.sp)
                Text("Precision Network Intelligence", style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary, textAlign = TextAlign.Center)

                Spacer(Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = LightSurface),
                    shape    = RoundedCornerShape(24.dp),
                    border   = BorderStroke(0.5.dp, LightBorder.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InputField(name, { name = it; nameError = null }, "Full Name",
                            leadingIcon = Icons.Outlined.Person,
                            isError = nameError != null, errorMessage = nameError)

                        InputField(email, { email = it; emailError = null }, "Email Address",
                            leadingIcon = Icons.Outlined.Email,
                            isError = emailError != null, errorMessage = emailError,
                            keyboardType = KeyboardType.Email)

                        InputField(
                            value = password, onValueChange = { password = it; passwordError = null },
                            label = "Password", leadingIcon = Icons.Outlined.Lock,
                            isError = passwordError != null, errorMessage = passwordError,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = TextHint)
                                }
                            })

                        InputField(
                            value = confirm, onValueChange = { confirm = it; confirmError = null },
                            label = "Confirm Password", leadingIcon = Icons.Outlined.Lock,
                            isError = confirmError != null, errorMessage = confirmError,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = PasswordVisualTransformation())

                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = "Create Account",
                            onClick = { if (validate()) vm.register(name, email, password) },
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = registerState is ApiResult.Loading,
                            icon = Icons.Outlined.HowToReg
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already have an account?", color = TextSecondary)
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Sign In", fontWeight = FontWeight.Bold, color = HamsaaPrimary)
                    }
                }
            }
        }
    }
}
