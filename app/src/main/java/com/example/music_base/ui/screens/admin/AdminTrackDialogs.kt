package com.example.music_base.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackFormDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    initialArtistId: String = "",
    initialAlbumId: String = "",
    initialThumbnailUrl: String = "",
    initialYoutubeVideoId: String = "",
    isEditMode: Boolean = false,
    artists: List<com.example.music_base.data.model.Artist> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String?,
        artistId: String,
        albumId: String?,
        thumbnailUrl: String?,
        youtubeVideoId: String?,
        sourceType: String,
        youtubeUrl: String?,
        file: MultipartBody.Part?
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var artistId by remember { mutableStateOf(initialArtistId) }
    var albumId by remember { mutableStateOf(initialAlbumId) }
    var thumbnailUrl by remember { mutableStateOf(initialThumbnailUrl) }
    var youtubeVideoId by remember { mutableStateOf(initialYoutubeVideoId) }
    
    // Upload specific
    var sourceType by remember { mutableStateOf("youtube") } // 'file' | 'youtube'
    var youtubeUrl by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    var isArtistDropdownExpanded by remember { mutableStateOf(false) }
    val selectedArtistName = artists.find { it.id == artistId }?.name ?: "Select artist..."

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileUri = uri
        selectedFileName = uri?.lastPathSegment ?: "audio_file"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(Dimens.radiusLarge)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.paddingLarge)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (isEditMode) Icons.Default.Edit else Icons.Default.CloudUpload,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isEditMode) "Refine Metadata" else "Dispatch New Track",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                Divider(Modifier.padding(vertical = Dimens.paddingNormal), color = Color.White.copy(alpha = 0.05f))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
                ) {
                    // Title
                    AdminTextField(label = "Title", value = title, onValueChange = { title = it }, placeholder = "Nebula Dreams")
                    
                    // description
                    AdminTextField(label = "Description (Optional)", value = description, onValueChange = { description = it }, placeholder = "Optional context...")

                    // Artist ID Manual Input
                    AdminTextField(
                        label = "Artist ID (Manual Override)",
                        value = artistId,
                        onValueChange = { artistId = it },
                        placeholder = "e.g. artist-123"
                    )

                    // Artist Selection Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Artist (Selector Helper)", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (artistId.isEmpty()) Color.Transparent else Primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { isArtistDropdownExpanded = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = selectedArtistName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White.copy(alpha = 0.5f))
                            }
                        }

                        DropdownMenu(
                            expanded = isArtistDropdownExpanded,
                            onDismissRequest = { isArtistDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f).background(Color(0xFF1E1E1E))
                        ) {
                            artists.forEach { artist ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(artist.name, color = Color.White)
                                            Text(artist.id, color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                                        }
                                    },
                                    onClick = {
                                        artistId = artist.id
                                        isArtistDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    AdminTextField(label = "Album UUID (Optional)", value = albumId, onValueChange = { albumId = it })

                    AdminTextField(label = "Thumbnail URL (Optional)", value = thumbnailUrl, onValueChange = { thumbnailUrl = it })
                    AdminTextField(label = "Custom Video ID (Optional)", value = youtubeVideoId, onValueChange = { youtubeVideoId = it })

                    if (!isEditMode) {
                        Text(
                            "Source Configuration",
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        // Source Selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimens.radiusMedium))
                                .background(Color.White.copy(alpha = 0.03f))
                                .padding(4.dp)
                        ) {
                            SourceTab(
                                selected = sourceType == "youtube",
                                label = "YouTube Link",
                                icon = Icons.Default.Link,
                                modifier = Modifier.weight(1f)
                            ) { sourceType = "youtube" }
                            SourceTab(
                                selected = sourceType == "file",
                                label = "Local File",
                                icon = Icons.Default.Attachment,
                                modifier = Modifier.weight(1f)
                            ) { sourceType = "file" }
                        }

                        if (sourceType == "youtube") {
                            AdminTextField(
                                label = "YouTube URL",
                                value = youtubeUrl,
                                onValueChange = { youtubeUrl = it },
                                placeholder = "https://youtube.com/watch?v=..."
                            )
                        } else {
                            Button(
                                onClick = { filePickerLauncher.launch("audio/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Dimens.radiusMedium),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                            ) {
                                Icon(Icons.Default.UploadFile, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (selectedFileUri == null) "Select Audio File" else selectedFileName)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.paddingLarge))

                // Actions
                Button(
                    onClick = {
                        val multipartPart = if (sourceType == "file" && selectedFileUri != null) {
                            val file = uriToFile(context, selectedFileUri!!)
                            val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("file", file.name, requestFile)
                        } else null

                        onConfirm(
                            title,
                            description.ifBlank { null },
                            artistId,
                            albumId.ifBlank { null },
                            thumbnailUrl.ifBlank { null },
                            youtubeVideoId.ifBlank { null },
                            sourceType,
                            if (sourceType == "youtube") youtubeUrl else null,
                            multipartPart
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(Dimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = title.isNotBlank() && artistId.isNotBlank() && (isEditMode || (sourceType == "youtube" && youtubeUrl.isNotBlank()) || (sourceType == "file" && selectedFileUri != null))
                ) {
                    Text(if (isEditMode) "Save Changes" else "Initialize Upload", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    readOnly: Boolean = false
) {
    Column(modifier) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.15f), fontSize = 14.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                focusedIndicatorColor = if (readOnly) Color.Transparent else Primary,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = if (readOnly) Color.White.copy(alpha = 0.6f) else Color.White,
                unfocusedTextColor = if (readOnly) Color.White.copy(alpha = 0.6f) else Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Composable
fun SourceTab(selected: Boolean, label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusSmall))
            .background(if (selected) Primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (selected) Primary else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = if (selected) Color.White else Color.White.copy(alpha = 0.3f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Helper to convert URI to File (Multipart requires File)
private fun uriToFile(context: android.content.Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "upload_audio_${System.currentTimeMillis()}.tmp")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}

@Composable
fun TrackDeleteConfirmDialog(
    trackTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        icon = { Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp)) },
        title = { Text("Confirm Deletion", color = Color.White) },
        text = {
            Text(
                "Are you sure you want to remove '$trackTitle'?\n\nThis action will permanently delete the track from the core repository and all listener playlists.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Text("Delete Track", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Track", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}
