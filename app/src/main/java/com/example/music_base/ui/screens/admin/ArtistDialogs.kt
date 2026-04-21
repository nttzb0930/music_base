package com.example.music_base.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.music_base.data.model.Artist
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary

@Composable
fun ArtistFormDialog(
    initialArtist: Artist? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        youtubeChannelId: String,
        uploaderId: String?,
        description: String?,
        thumbnails: List<String>?
    ) -> Unit
) {
    val isEditMode = initialArtist != null
    var name by remember { mutableStateOf(initialArtist?.name ?: "") }
    var youtubeChannelId by remember { mutableStateOf(initialArtist?.youtubeChannelId ?: "") }
    var uploaderId by remember { mutableStateOf(initialArtist?.uploaderId ?: "") }
    var description by remember { mutableStateOf(initialArtist?.description ?: "") }
    var thumbnailsStr by remember { 
        mutableStateOf(initialArtist?.thumbnails?.map { it.url }?.joinToString("\n") ?: "") 
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
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
                        if (isEditMode) Icons.Default.PersonOutline else Icons.Default.PersonAdd,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isEditMode) "Identify Artist" else "Register New Artist",
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

                // Form Scrollable part
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
                ) {
                    if (isEditMode) {
                        AdminTextField(
                            label = "Artist ID (System UUID)",
                            value = initialArtist?.id ?: "",
                            onValueChange = {},
                            readOnly = true
                        )
                    }

                    AdminTextField(label = "Artist Name", value = name, onValueChange = { name = it }, placeholder = "e.g. Lofi Girl")
                    AdminTextField(label = "YouTube Channel ID", value = youtubeChannelId, onValueChange = { youtubeChannelId = it }, placeholder = "UCxxxxxxxxxxxxxxx")
                    AdminTextField(label = "Uploader ID / Handle", value = uploaderId, onValueChange = { uploaderId = it }, placeholder = "@lofigirl")
                    AdminTextField(label = "Description", value = description, onValueChange = { description = it }, placeholder = "Artist bio...")
                    
                    Column {
                        Text("Thumbnails (One URL per line)", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        TextField(
                            value = thumbnailsStr,
                            onValueChange = { thumbnailsStr = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            placeholder = { Text("https://example.com/image.jpg", color = Color.White.copy(alpha = 0.15f), fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                focusedIndicatorColor = Primary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.paddingLarge))

                Button(
                    onClick = {
                        val thumbnails = thumbnailsStr.split("\n").filter { it.isNotBlank() }
                        onConfirm(
                            name,
                            youtubeChannelId,
                            uploaderId.ifBlank { null },
                            description.ifBlank { null },
                            if (thumbnails.isEmpty()) null else thumbnails
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(Dimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = name.isNotBlank() && youtubeChannelId.isNotBlank()
                ) {
                    Text(if (isEditMode) "Save Artist" else "Create Artist", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ArtistSyncDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var artistId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        icon = { Icon(Icons.Default.Refresh, null, tint = Primary, modifier = Modifier.size(48.dp)) },
        title = { 
            Text(
                "Synchronize Artist Data", 
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)) {
                Text(
                    "Re-index the artist registry. Leave the ID field blank to refresh the entire catalog, or enter a specific Artist ID to sync only that entity.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                
                AdminTextField(
                    label = "Target Artist ID (Optional)",
                    value = artistId,
                    onValueChange = { artistId = it },
                    placeholder = "e.g. artist-123 (Blank for all)"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(artistId) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (artistId.isBlank()) "Full Repository Sync" else "Sync Specific Artist", color = Color.Black, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = true)
    )
}

@Composable
fun ArtistDeleteConfirmDialog(
    artistName: String,
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
                "Are you sure you want to remove '$artistName'?\n\nNote: Backend will block deletion if this artist still has tracks or albums linked to them.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Text("Delete Artist", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Artist", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}
