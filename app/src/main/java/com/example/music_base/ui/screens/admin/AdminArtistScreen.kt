package com.example.music_base.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import coil.compose.AsyncImage
import com.example.music_base.data.model.Artist
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminArtistScreen(
    artists: List<Artist>,
    totalCount: Int,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    onCreateArtist: (name: String, channelId: String, uploaderId: String?, description: String?, thumbnails: List<String>?) -> Unit,
    onUpdateArtist: (id: String, name: String?, channelId: String?, uploaderId: String?, description: String?, thumbnails: List<String>?) -> Unit,
    onDeleteArtist: (String) -> Unit,
    onSyncArtist: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var editingArtist by remember { mutableStateOf<Artist?>(null) }
    var deletingArtist by remember { mutableStateOf<Artist?>(null) }

    val filteredArtists = if (searchQuery.isBlank()) artists 
    else artists.filter { it.name.contains(searchQuery, ignoreCase = true) || it.uploaderId.contains(searchQuery, ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Header & Search ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(Dimens.paddingLarge)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Artist Registry",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$totalCount registered entities",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                        
                        Row {
                            IconButton(
                                onClick = { showSyncDialog = true },
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Icon(Icons.Default.Sync, "Sync Artist", tint = Primary)
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Primary.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.PersonAdd, "New Artist", tint = Primary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingNormal))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Filter by name or handle...", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                            focusedIndicatorColor = Primary,
                            cursorColor = Primary,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(Dimens.radiusMedium),
                        singleLine = true
                    )
                }
            }

            // --- List ---
            if (isLoading && artists.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (filteredArtists.isEmpty() && searchQuery.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No local matches. Try searching globally.", color = Color.White.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimens.paddingLarge)
                ) {
                    items(filteredArtists) { artist ->
                        ArtistDatabaseItem(
                            artist = artist,
                            onEdit = { editingArtist = artist },
                            onDelete = { deletingArtist = artist }
                        )
                        Spacer(modifier = Modifier.height(Dimens.paddingNormal))
                    }

                    item {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(Dimens.paddingLarge), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                            }
                        } else if (artists.size < totalCount) {
                            Button(
                                onClick = onLoadMore,
                                modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.paddingLarge),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(Dimens.radiusMedium)
                            ) {
                                Text("Load More Artists", color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }
        }

        // --- Dialogs ---
        if (showCreateDialog) {
            ArtistFormDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, channelId, uploaderId, desc, thumbs ->
                    onCreateArtist(name, channelId, uploaderId, desc, thumbs)
                    showCreateDialog = false
                }
            )
        }

        editingArtist?.let { artist ->
            ArtistFormDialog(
                initialArtist = artist,
                onDismiss = { editingArtist = null },
                onConfirm = { name, channelId, uploaderId, desc, thumbs ->
                    onUpdateArtist(artist.id, name, channelId, uploaderId, desc, thumbs)
                    editingArtist = null
                }
            )
        }

        if (showSyncDialog) {
            ArtistSyncDialog(
                onDismiss = { showSyncDialog = false },
                onConfirm = { id ->
                    onSyncArtist(id)
                    showSyncDialog = false
                }
            )
        }

        deletingArtist?.let { artist ->
            ArtistDeleteConfirmDialog(
                artistName = artist.name,
                onDismiss = { deletingArtist = null },
                onConfirm = {
                    onDeleteArtist(artist.id)
                    deletingArtist = null
                }
            )
        }
    }
}

@Composable
fun ArtistDatabaseItem(
    artist: Artist,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(Dimens.radiusMedium)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(Dimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(Dimens.radiusSmall))
            )
            
            Spacer(modifier = Modifier.width(Dimens.paddingNormal))
            
            val context = LocalContext.current
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.id,
                    color = Primary.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = artist.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist.uploaderId,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = "YouTube: ${artist.youtubeChannelId}",
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
