package com.example.music_base.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import com.example.music_base.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.music_base.ui.viewmodel.AuthState
import com.example.music_base.ui.viewmodel.AuthViewModel
import com.example.music_base.ui.components.SocialLoginButton
import coil.compose.AsyncImage

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }
    val authState = viewModel.authState.collectAsState().value
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.resetToIdle()
    }

    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastType by remember { mutableStateOf(com.example.music_base.ui.components.ToastType.Info) }

    // Field-specific error states
    var emailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Loading) {
            // Clear all errors when the user clicks the button
            emailError = null
            usernameError = null
            passwordError = null
            confirmPasswordError = null
        }

        when (authState) {
            is AuthState.Error -> {
                val fullMessage = authState.message
                val messages = fullMessage.split(". ")
                var hasFieldSpecificError = false

                messages.forEach { msg ->
                    val lowerMsg = msg.lowercase()
                    when {
                        lowerMsg.contains("email") -> {
                            emailError = "Please enter a valid email address"
                            hasFieldSpecificError = true
                        }
                        lowerMsg.contains("username") || lowerMsg.contains("tên đăng nhập") -> {
                            usernameError = "Username must be at least 3 characters"
                            hasFieldSpecificError = true
                        }
                        lowerMsg.contains("password") || lowerMsg.contains("mật khẩu") -> {
                            if (lowerMsg.contains("confirm") || lowerMsg.contains("confirmpassword")) {
                                confirmPasswordError = "Passwords must be at least 6 characters"
                            } else {
                                passwordError = "Password must be at least 6 characters"
                            }
                            hasFieldSpecificError = true
                        }
                        lowerMsg.contains("confirmpassword") -> {
                            confirmPasswordError = "Passwords must be at least 6 characters"
                            hasFieldSpecificError = true
                        }
                    }
                }

                // ONLY show toast if we couldn't map ANY error to a field
                if (!hasFieldSpecificError) {
                    toastMessage = fullMessage
                    toastType = com.example.music_base.ui.components.ToastType.Error
                    showToast = true
                }
            }
            is AuthState.RegistrationSuccess -> {
                toastMessage = "Registration successful!"
                toastType = com.example.music_base.ui.components.ToastType.Success
                showToast = true
                kotlinx.coroutines.delay(2000L)
                viewModel.resetToIdle()
                onNavigateToLogin()
            }
            else -> {}
        }
    }

    Box(Modifier.fillMaxSize()) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )


        // Heading
        Text(
            text = "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-1).sp
        )
        Text(
            text = "JOIN THE SONIC REVOLUTION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Glass Panel Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A).copy(alpha = 0.6f), MaterialTheme.shapes.large)
                .border(1.dp, Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.large)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

             // Username
            Column {
                Text(
                    text = "USERNAME",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
                TextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        usernameError = null // Clear error when typing
                    },
                    placeholder = { Text("alex_sonic", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).clip(MaterialTheme.shapes.medium),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                if (usernameError != null) {
                    Text(
                        text = usernameError!!,
                        color = Color(0xFFFF4444),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                }
            }

            // Email
            Column {
                Text(
                    text = "EMAIL ADDRESS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
                TextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = null // Clear error when typing
                    },
                    placeholder = { Text("alex@sonicgallery.com", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).clip(MaterialTheme.shapes.medium),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = Color(0xFFFF4444),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                }
            }

            // Password
            Column {
                Text(
                    text = "PASSWORD",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = null // Clear error when typing
                    },
                    placeholder = { Text("••••••••••••", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).clip(MaterialTheme.shapes.medium),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = Color(0xFFFF4444),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                }
            }

            // Confirm Password
            Column {
                Text(
                    text = "CONFIRM PASSWORD",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
                TextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        confirmPasswordError = null // Clear error when typing
                    },
                    placeholder = { Text("••••••••••••", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).clip(MaterialTheme.shapes.medium),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (confirmPasswordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                if (confirmPasswordError != null) {
                    Text(
                        text = confirmPasswordError!!,
                        color = Color(0xFFFF4444),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                }
            }

            // Terms
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { agreeToTerms = !agreeToTerms }
            ) {
                Icon(
                    imageVector = if (agreeToTerms) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (agreeToTerms) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "I agree to the Terms of Service.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Register Button
            Button(
                onClick = { 
                    if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                        toastMessage = "Please fill all fields"
                        toastType = com.example.music_base.ui.components.ToastType.Warning
                        showToast = true
                    } else if (password != confirmPassword) {
                        toastMessage = "Passwords do not match"
                        toastType = com.example.music_base.ui.components.ToastType.Warning
                        showToast = true
                    } else if (!agreeToTerms) {
                        toastMessage = "Please agree to the Terms and Conditions"
                        toastType = com.example.music_base.ui.components.ToastType.Warning
                        showToast = true
                    } else {
                        viewModel.register(email, username, password, confirmPassword)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ),
                shape = CircleShape,
                enabled = authState !is AuthState.Loading && 
                          authState !is AuthState.RegistrationSuccess
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Register Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Footer
        Row(
            modifier = Modifier.padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Text(
                text = "Log In",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }

        if (authState is AuthState.Error) {
            // Error handling moved to SonicToast below
        }
    }

    // Success Overlay
    AnimatedVisibility(
        visible = authState is AuthState.RegistrationSuccess,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(enabled = false) {}, // Block interaction with background
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "REGISTRATION COMPLETED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Welcome to Sonic Gallery. Redirecting you to login screen...",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }

    com.example.music_base.ui.components.SonicToast(
        message = toastMessage,
        type = toastType,
        isVisible = showToast,
        onDismiss = { showToast = false }
    )
}
}
