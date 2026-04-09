package com.example.music_base.ui.screens.auth

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.music_base.ui.viewmodel.AuthState
import com.example.music_base.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.AccountCircle // Using AccountCircle for Apple placeholder
import com.example.music_base.ui.components.SocialLoginButton
import coil.compose.AsyncImage

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState = viewModel.authState.collectAsState().value
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.resetToIdle()
    }

    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastType by remember { mutableStateOf(com.example.music_base.ui.components.ToastType.Info) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                toastMessage = authState.message
                toastType = com.example.music_base.ui.components.ToastType.Error
                showToast = true
            }
            is AuthState.Authenticated -> {
                toastMessage = "Login successful!"
                toastType = com.example.music_base.ui.components.ToastType.Success
                showToast = true
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

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
                .size(100.dp)
                .padding(bottom = 24.dp)
        )


        // Welcome Text
        Text(
            text = "Welcome Back",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = (-1).sp
        )
        Text(
            text = "THE SONIC IMMERSIVE EXPERIENCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Email Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "EMAIL ADDRESS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("name@domain.com", color = MaterialTheme.colorScheme.outline) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(MaterialTheme.shapes.medium),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Password Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "PASSWORD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "FORGOT PASSWORD?",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp).clickable { /* TODO */ }
                )
            }
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••", color = MaterialTheme.colorScheme.outline) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(MaterialTheme.shapes.medium),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
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
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Login Button
        Button(
            onClick = { 
                if (email.isEmpty() || password.isEmpty()) {
                    toastMessage = "Please enter both email and password"
                    toastType = com.example.music_base.ui.components.ToastType.Warning
                    showToast = true
                } else {
                    viewModel.login(email, password) 
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            ),
            shape = CircleShape,
            enabled = authState !is AuthState.Loading && authState !is AuthState.Authenticated
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text("LOG IN", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }

        // Divider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Text(
                text = "OR CONTINUE WITH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                letterSpacing = 2.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }

        // Social Logins
        SocialLoginButton(
            text = "Continue with Google",
            iconUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCziJBw5qLrAo8v-8TUq2691nhktelhm7EsINoVrI9rn0uAcYgq-aQXkUyL74wRuCDkYqpUVO6fDb5N2RXK4NXQn75x4seKeS6flQP98pBsEJn8fPjdQbdPrPadZD3XbqIqNAeiuFd0HFwcQDdqiXPyD6P5-utrSiW1LEkqyUrtqMdaSI5vHyIAMviF8kVCetSlWIuAJXYPWdDILbR2eCg4ef9x_0KjSpRgx5nN3Bs-8017aADtYIfQA6pZHAk-y_gxJAtNrmopy_A",
            onClick = { /* TODO */ }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SocialLoginButton(
            text = "Continue with Apple",
            icon = Icons.Default.AccountCircle, // Placeholder for Apple
            onClick = { /* TODO */ }
        )

        // Footer
        Row(
            modifier = Modifier.padding(top = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Text(
                text = "Register",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
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

