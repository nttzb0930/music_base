package com.example.music_base.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.ui.viewmodel.AuthState
import com.example.music_base.ui.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            SettingsContent(
                viewModel = viewModel,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToRegister = onNavigateToRegister
            )
        }
    }
}

@Composable
fun SettingsContent(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState = viewModel.authState.collectAsState().value

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        if (authState is AuthState.Unauthenticated || authState is AuthState.Idle) {
            // Guest Settings
            item {
                GuestProfileSection(
                    onLoginClick = onNavigateToLogin,
                    onRegisterClick = onNavigateToRegister
                )
            }

            item {
                SettingSection(title = "Account") {
                    SettingItem(icon = Icons.Rounded.Sync, title = "Login to sync your data")
                    SettingItem(icon = Icons.Rounded.AutoAwesome, title = "Join the Sonic Revolution")
                }
            }
        } else if (authState is AuthState.Authenticated) {
            val user = authState.user
            // Authenticated Settings
            item {
                AuthProfileSection(user = user)
            }

            item {
                SettingSection(title = "Account") {
                    SettingItem(icon = Icons.Rounded.Badge, title = "Username", value = user.displayName)
                    SettingItem(icon = Icons.Rounded.Person, title = "Account details")
                    SettingItem(icon = Icons.Rounded.Mail, title = "Email address", value = user.email)
                }
            }
        }

        // Shared Sections
        item {
            SettingSection(title = "Audio Quality") {
                SettingItemWithBadge(title = "WiFi Streaming", subtitle = "Highest fidelity for home listening", badgeText = "Very High")
                SettingItemWithBadge(title = "Cellular Streaming", subtitle = "Optimized for mobile connections", badgeText = "Normal", badgeColor = Color(0xFF262626), badgeTextColor = Color.White.copy(alpha = 0.6f))
            }
        }

        item {
            SettingSection(title = "Data Saver") {
                var dataSaver by remember { mutableStateOf(false) }
                SettingToggle(
                    title = "Audio Quality Data Saver",
                    subtitle = "Sets audio to low and disables canvases",
                    checked = dataSaver,
                    onCheckedChange = { dataSaver = it }
                )
            }
        }

        item {
            SettingSection(title = "About") {
                SettingItem(title = "Version 8.8.12.544", hideIcon = true)
                SettingItem(title = "Terms and Conditions", showExternalIcon = true)
                SettingItem(title = "Privacy Policy", showExternalIcon = true)
            }
        }

        item {
            ExperimentalBentoCard()
        }

        if (authState is AuthState.Authenticated) {
            item {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF7351).copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF7351))
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun GuestProfileSection(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131313), RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFF262626), CircleShape)
                    .border(2.dp, Color(0xFF72FE8F).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF72FE8F), modifier = Modifier.size(48.dp))
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF72FE8F), CircleShape)
                    .padding(4.dp)
                    .border(4.dp, Color(0xFF131313), CircleShape)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF004A1C), modifier = Modifier.size(12.dp))
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Guest", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Sign in to save your library and sync across devices.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954), contentColor = Color(0xFF002A0C)),
                shape = CircleShape
            ) {
                Text("Log In", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier.weight(1f).height(56.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = CircleShape
            ) {
                Text("Register", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AuthProfileSection(user: com.example.music_base.data.model.User) {
    val displayName = user.displayName
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131313), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuA7xal4FTjdxhcr4VeU_ycZe11Zq8Tn5g_dJsVlseg8vjYS5o0OzM2c4GWAjgs1C3hUipNo414Y6UGmCs61-T-34v_Olxo5c7XMsKrJ2YclmiIXWlVL6c1vfjbF7_0rcNAgUR_JMq4k08eWxgW4MQoU2lbIzlSLlaehywWBZM46KtSZ3_Zasc434U2_VBRMIT2jz1_PKEtmNwxHTJm6L3v1oJZS2K-kYaCeA9VQrKTn3bSSQtKOX4tE5i5K_uEA6PkQyhuz_ZgZZH8",
            contentDescription = "User",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, Color(0xFF72FE8F).copy(alpha = 0.2f), CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("PREMIUM MEMBER", color = Color(0xFF72FE8F), fontSize = 10.sp, letterSpacing = 1.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
    }
}

@Composable
fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            color = Color(0xFF72FE8F),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131313), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            content()
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    value: String? = null,
    showExternalIcon: Boolean = false,
    hideIcon: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
            }
            Text(title, color = if (hideIcon) Color.White.copy(alpha = 0.6f) else Color.White, fontSize = 16.sp)
        }
        if (value != null) {
            Text(value, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        } else if (showExternalIcon) {
            Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
        } else if (!hideIcon) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SettingItemWithBadge(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color = Color(0xFF72FE8F).copy(alpha = 0.1f),
    badgeTextColor: Color = Color(0xFF72FE8F)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, color = Color.White, fontSize = 16.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        Surface(
            color = badgeColor,
            shape = CircleShape
        ) {
            Text(
                text = badgeText,
                color = badgeTextColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, color = Color.White, fontSize = 16.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF1DB954),
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color(0xFF262626)
            )
        )
    }
}

@Composable
fun ExperimentalBentoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCTusd4sfNhY-bS49fNgJUSH9ALDmAp1XHWmO8qAGX0oKi7C1gCH4mJNb1WS7Mf-67zfjmDtmgSwUGEjg3zb2sg1vSdYbf1sgSn4cRZaAlZ5yDJvDWG00souYfL7SGQzaqhT25lshzWeeFMkvKgRGWJDTlZhovQFynXbZAyl_kAp5O1es7nmDLho1A7SOdwEvauEmb-VF3Ht8GKGwIKtS195wl024T1kWPyl50nuVDVN17bmAhtStngkAWB7-u0g7Kb_v4kqsiFHmI",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 100f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text("UPGRADE", color = Color(0xFF72FE8F), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("Unleash the Sound", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
