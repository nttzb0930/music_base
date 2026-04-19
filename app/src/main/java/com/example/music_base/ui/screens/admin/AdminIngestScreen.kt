package com.example.music_base.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary
import com.example.music_base.ui.theme.Secondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminIngestScreen(
    onUploadAudio: (String, String) -> Unit,
    onSyncUrl: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.paddingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
        contentPadding = PaddingValues(top = Dimens.paddingLarge, bottom = 120.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Ingestion Pipeline",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Inject new content into the system",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // --- URL Pulse ---
        item {
            ContentSection(title = "URL Pulse Pipeline") {
                UrlSyncModule(onSync = onSyncUrl)
            }
        }

        // --- Audio Stage ---
        item {
            ContentSection(title = "Manual Audio Stage") {
                AudioUploadModule(onUpload = onUploadAudio)
            }
        }
        
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(Dimens.radiusLarge),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(Modifier.padding(Dimens.paddingNormal)) {
                    Text(
                        "Ingest Note",
                        color = Secondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "All content synced via URL is automatically analyzed for metadata and uploaded to Cloudinary CDN.",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlSyncModule(onSync: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(SyncStage.IDLE) }
    var mockMetadata by remember { mutableStateOf<Map<String, String>?>(null) }
    var uploadProgress by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.animateContentSize()) {
        when (stage) {
            SyncStage.IDLE -> {
                Text("Process YouTube URLs via dlp-tool logic", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                Spacer(Modifier.height(Dimens.paddingNormal))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Source URL") },
                    placeholder = { Text("https://youtube.com/...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                Spacer(Modifier.height(Dimens.paddingNormal))
                Button(
                    onClick = {
                        if (url.isNotBlank()) {
                            stage = SyncStage.ANALYZING
                            scope.launch {
                                delay(1500)
                                mockMetadata = mapOf(
                                    "title" to "Metadata Extraction Success",
                                    "uploader" to "System Pipeline",
                                    "duration" to "Analyzed"
                                )
                                stage = SyncStage.REVIEWING
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(Dimens.radiusMedium),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text("Trigger dlp-tool Analysis", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            SyncStage.ANALYZING -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Secondary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Deconstructing URL & Extracting JSON...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
            
            SyncStage.REVIEWING -> {
                Column {
                    Text("Metadata Manifest", color = Secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(Dimens.paddingNormal)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape), Alignment.Center) {
                                Icon(Icons.Default.Info, null, tint = Color.White.copy(alpha = 0.4f))
                            }
                            Spacer(Modifier.width(Dimens.paddingMedium))
                            Column {
                                Text(mockMetadata?.get("title") ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${mockMetadata?.get("uploader")}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)) {
                        TextButton(onClick = { stage = SyncStage.IDLE }, modifier = Modifier.weight(1f)) {
                            Text("Reject", color = Color.White.copy(alpha = 0.4f))
                        }
                        Button(
                            onClick = {
                                stage = SyncStage.UPLOADING
                                scope.launch {
                                    for (i in 1..100) {
                                        delay(20)
                                        uploadProgress = i / 100f
                                    }
                                    stage = SyncStage.SUCCESS
                                    onSync(url)
                                }
                            },
                            modifier = Modifier.weight(2f),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                            shape = RoundedCornerShape(Dimens.radiusMedium)
                        ) {
                            Text("Inject to Cloudinary", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            SyncStage.UPLOADING -> {
                Column {
                    Text("Cloudinary Sinking...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = uploadProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Secondary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
            
            SyncStage.SUCCESS -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CloudDone, null, tint = Primary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Injection Complete", color = Color.White, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { stage = SyncStage.IDLE; url = "" }) {
                        Text("Finish", color = Primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioUploadModule(onUpload: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }

    Column {
        Text("Manual Metadata Injection", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
        Spacer(Modifier.height(Dimens.paddingNormal))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Track Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
        )
        Spacer(Modifier.height(Dimens.paddingSmall))
        OutlinedTextField(
            value = artist,
            onValueChange = { artist = it },
            label = { Text("Artist Identity") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
        )
        Spacer(Modifier.height(Dimens.paddingNormal))
        Button(
            onClick = { onUpload(title, artist) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(Dimens.radiusMedium)
        ) {
            Text("Select & Push File", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

enum class SyncStage { IDLE, ANALYZING, REVIEWING, UPLOADING, SUCCESS }
