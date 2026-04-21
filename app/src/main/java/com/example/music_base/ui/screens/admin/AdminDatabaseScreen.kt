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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.music_base.data.model.Track
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary

enum class AdminDatabaseTab { Tracks, Artists }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDatabaseScreen(
    // Tracks
    tracks: List<Track>,
    isTracksLoading: Boolean,
    onLoadMoreTracks: () -> Unit,
    onSearchTracks: (String) -> Unit,
    onEditTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    
    // Artists
    artists: List<com.example.music_base.data.model.Artist>,
    totalArtistCount: Int,
    isArtistsLoading: Boolean,
    onLoadMoreArtists: () -> Unit,
    onCreateArtist: (name: String, channelId: String, uploaderId: String?, description: String?, thumbnails: List<String>?) -> Unit,
    onUpdateArtist: (id: String, name: String?, channelId: String?, uploaderId: String?, description: String?, thumbnails: List<String>?) -> Unit,
    onDeleteArtist: (String) -> Unit,
    onSyncArtist: (String) -> Unit,
    
    onRefresh: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdminDatabaseTab.Tracks) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Header Section ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(top = Dimens.paddingLarge, start = Dimens.paddingLarge, end = Dimens.paddingLarge)) {
                Text(
                    text = "Master Repository",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = Primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = Primary
                        )
                    },
                    divider = {}
                ) {
                    AdminDatabaseTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { 
                                selectedTab = tab 
                                searchQuery = "" // Reset search when switching
                            },
                            text = {
                                Text(
                                    tab.name,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        // --- Tab Content ---
        when (selectedTab) {
            AdminDatabaseTab.Tracks -> {
                TracksTabContent(
                    tracks = tracks,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { 
                        searchQuery = it
                        onSearchTracks(it)
                    },
                    isLoading = isTracksLoading,
                    onLoadMore = onLoadMoreTracks,
                    onEditTrack = onEditTrack,
                    onDeleteTrack = onDeleteTrack
                )
            }
            AdminDatabaseTab.Artists -> {
                // Reuse AdminArtistScreen logic or embed it
                AdminArtistScreen(
                    artists = artists,
                    totalCount = totalArtistCount,
                    isLoading = isArtistsLoading,
                    onLoadMore = onLoadMoreArtists,
                    onCreateArtist = onCreateArtist,
                    onUpdateArtist = onUpdateArtist,
                    onDeleteArtist = onDeleteArtist,
                    onSyncArtist = onSyncArtist,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@Composable
fun TracksTabContent(
    tracks: List<Track>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    onEditTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar for tracks
        Box(modifier = Modifier.padding(Dimens.paddingLarge)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by title or artist...", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.4f))
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
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
                contentPadding = PaddingValues(horizontal = Dimens.paddingLarge, vertical = Dimens.paddingSmall)
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
                item { Spacer(modifier = Modifier.height(120.dp)) }
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
                    text = track.id,
                    color = Primary.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
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
