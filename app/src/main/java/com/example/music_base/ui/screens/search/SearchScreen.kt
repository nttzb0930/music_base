package com.example.music_base.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary
import com.example.music_base.ui.viewmodel.MusicViewModel

data class Category(val name: String, val color: Color)

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onTrackClick: (Track) -> Unit,
    onArtistClick: (com.example.music_base.data.model.Artist) -> Unit,
    onAddToPlaylist: (Track) -> Unit = {},
    onShare: (Track) -> Unit = {},
    modifier: Modifier = Modifier

) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val artistResults by viewModel.artistSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isSyncing by viewModel.isSyncingFromUrl.collectAsState()

    val categories = listOf(
        Category("Pop", Color(0xFFE91E63)),
        Category("Rock", Color(0xFF673AB7)),
        Category("Jazz", Color(0xFF3F51B5)),
        Category("Hip Hop", Color(0xFF009688)),
        Category("Electronic", Color(0xFFFFC107)),
        Category("Classical", Color(0xFF795548)),
        Category("Mood", Color(0xFF607D8B)),
        Category("New Releases", Color(0xFF4CAF50))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        var trackForMenu by remember { mutableStateOf<Track?>(null) }
        val likedTrackIds by viewModel.likedTrackIds.collectAsState()

        // Refined logic: Only consider it a "match" if the query is actually found in the results
        val hasActualMatches = remember(searchQuery, searchResults, artistResults) {
            val query = searchQuery.trim()
            if (query.isEmpty()) true 
            else {
                val trackMatch = searchResults.any { it.title.contains(query, ignoreCase = true) }
                val artistMatch = artistResults.any { it.name.contains(query, ignoreCase = true) }
                trackMatch || artistMatch
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            item {
                SearchInput(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    isSearching = isSearching
                )
            }

            if (searchQuery.trim().isEmpty()) {
                // Browse all categories
                item {
                    Text(
                        text = "Browse all",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                }
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(((categories.size / 2) * 116 + 16).dp),
                        userScrollEnabled = false
                    ) {
                        items(categories) { category ->
                            CategoryCard(category)
                        }
                    }
                }
            } else {
                // Search results - Explicit states

                when {
                    isSearching -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                    !hasActualMatches || (searchResults.isEmpty() && artistResults.isEmpty()) -> {
                        item {
                            EmptySearchResult(
                                query = searchQuery,
                                isSyncing = isSyncing,
                                onSyncRequested = { url ->
                                    viewModel.syncTrackFromUrl(url)
                                }
                            )
                        }
                    }
                    else -> {
                    if (artistResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Artists",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        item {
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(artistResults) { artist ->
                                    ArtistSearchItem(artist) { onArtistClick(artist) }
                                }
                            }
                        }
                    }

                    if (searchResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tracks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(searchResults) { track ->
                            SearchTrackItem(
                                track = track,
                                onClick = { onTrackClick(track) },
                                onMoreClick = { trackForMenu = track }
                            )
                        }
                        }
                }
                }
            }
        }

        if (trackForMenu != null) {
            com.example.music_base.ui.components.TrackActionSheet(
                track = trackForMenu!!,
                onDismiss = { trackForMenu = null },
                onToggleLike = { viewModel.toggleTrackLike(trackForMenu!!.id) },
                isLiked = likedTrackIds.contains(trackForMenu!!.id),
                onAddToPlaylist = { onAddToPlaylist(trackForMenu!!) },
                onShare = { onShare(trackForMenu!!) }
            )
        }
    }
}


@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    isSearching: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        placeholder = { Text("What do you want to listen to?", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = RoundedCornerShape(Dimens.radiusMedium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
fun SearchTrackItem(track: Track, onClick: () -> Unit, onMoreClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = track.coverUrl,
            contentDescription = track.title,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(Dimens.radiusSmall)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artistName ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CategoryCard(category: Category) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(
                Brush.linearGradient(
                    colors = listOf(category.color, category.color.copy(alpha = 0.6f))
                )
            )
            .padding(12.dp)
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}



@Composable
private fun EmptySearchResult(
    query: String, 
    isSyncing: Boolean,
    onSyncRequested: (String) -> Unit
) {
    var isReportVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Sending report to admin...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No results found for \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(Modifier.height(Dimens.paddingLarge))

            if (!isReportVisible) {
                OutlinedButton(
                    onClick = { isReportVisible = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(Dimens.radiusPill)
                ) {
                    Icon(Icons.Default.Flag, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Report to Admin & Sync", fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(
                visible = isReportVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                com.example.music_base.ui.components.PulseSyncCard(onSyncRequested = {
                    onSyncRequested(it)
                    isReportVisible = false
                })
            }
        }
    }
}

@Composable
fun ArtistSearchItem(artist: com.example.music_base.data.model.Artist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = artist.imageUrl,
            contentDescription = artist.name,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
