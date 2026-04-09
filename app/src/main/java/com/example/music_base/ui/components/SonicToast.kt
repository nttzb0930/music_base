package com.example.music_base.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ToastType { Success, Error, Info, Warning }

@Composable
fun SonicToast(
    message: String,
    type: ToastType = ToastType.Info,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        LaunchedEffect(message, isVisible) {
            kotlinx.coroutines.delay(4000)
            onDismiss()
        }
    }

    val statusColor = when (type) {
        ToastType.Success -> Color(0xFF1DB954)
        ToastType.Error -> Color(0xFFFF4444)
        ToastType.Warning -> Color(0xFFFFBF00)
        else -> Color.White.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .widthIn(min = 100.dp, max = 500.dp),
                shape = CircleShape,
                color = Color.Transparent, // Surface layer transparent
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1A1A1A).copy(alpha = 0.85f), // True dark glass
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Styled Logo
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.music_base.R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    if (message.isNotBlank()) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = message,
                            color = Color.White, // Absolute high contrast
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    if (type != ToastType.Info) {
                         Spacer(Modifier.width(8.dp))
                         Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                         Spacer(Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}
