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
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDatabaseScreen(
    tracks: List<Track>,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    onSearch: (String) -> Unit,
    onEditTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Search & Filter Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(Dimens.paddingLarge)) {
                Text(
                    text = "Track Repository",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.paddingNormal))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        onSearch(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by title or artist...", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = ""; onSearch("") }) {
                                Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    },
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

        // --- Tracks List ---
        if (isLoading && tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicOff, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tracks found in database", color = Color.White.copy(alpha = 0.4f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimens.paddingLarge)
            ) {
                items(tracks) { track ->
                    DatabaseTrackItem(
                        track = track,
                        onEdit = { onEditTrack(track) },
                        onDelete = { onDeleteTrack(track) }
                    )
                    Spacer(modifier = Modifier.height(Dimens.paddingNormal))
                }

                item {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(Dimens.paddingLarge), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        }
                    } else {
                        Button(
                            onClick = onLoadMore,
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.paddingLarge),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(Dimens.radiusMedium)
                        ) {
                            Text("Load More Tracks", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
                
                // Extra padding for bottom navigation
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun DatabaseTrackItem(
    track: Track,
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
                model = track.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(Dimens.radiusSmall))
            )
            
            Spacer(modifier = Modifier.width(Dimens.paddingNormal))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artistName ?: "Unknown Artist",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.RemoveRedEye, 
                        null, 
                        tint = Color.White.copy(alpha = 0.3f), 
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = track.formattedViews,
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit, 
                        null, 
                        tint = Primary, 
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete, 
                        null, 
                        tint = Color.Red.copy(alpha = 0.7f), 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
