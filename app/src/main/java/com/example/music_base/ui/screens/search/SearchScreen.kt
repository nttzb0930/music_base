package com.example.music_base.ui.screens.search

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.music_base.ui.viewmodel.MusicViewModel

data class Category(val name: String, val color: Color)

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onTrackClick: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit = {},
    onShare: (Track) -> Unit = {},
    modifier: Modifier = Modifier

) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

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
                // Search results
                if (searchResults.isEmpty() && !isSearching) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No results found for \"$searchQuery\"", color = Color.Gray)
                        }
                    }
                } else {
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
