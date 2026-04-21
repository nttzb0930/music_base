package com.example.music_base.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music_base.ui.theme.Primary
import com.example.music_base.ui.theme.Dimens

@Composable
fun PulseSyncCard(
    onSyncRequested: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.paddingLarge),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(Dimens.radiusMedium),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingLarge)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Publish, null, tint = Primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(Dimens.paddingNormal))
                Text(
                    "Track Request Portal",
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                "Can't find your favorite music? Submit a YouTube link and our admins will review and sync it for you.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = Dimens.paddingSmall)
            )

            Spacer(Modifier.height(Dimens.paddingSmall))
            
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Paste YouTube Link here...", fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                    focusedIndicatorColor = Primary,
                    cursorColor = Primary,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(Dimens.radiusSmall),
                singleLine = true
            )
            
            Spacer(Modifier.height(Dimens.paddingNormal))
            
            Button(
                onClick = { 
                    if (url.isNotBlank()) {
                        onSyncRequested(url)
                        url = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(Dimens.radiusPill),
                enabled = url.isNotBlank()
            ) {
                Text("Submit for Review", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.Black)
            }
        }
    }
}
