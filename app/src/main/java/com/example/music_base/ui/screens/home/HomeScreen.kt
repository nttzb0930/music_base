package com.example.music_base.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track
import com.example.music_base.data.model.Artist
import com.example.music_base.data.model.Album
import com.example.music_base.data.model.Playlist
import com.example.music_base.data.model.PlaybackHistoryItem
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.music_base.ui.components.CreatePlaylistDialog
import com.example.music_base.ui.components.TrackActionSheet
import com.example.music_base.ui.components.AlbumActionSheet
import com.example.music_base.ui.components.ArtistActionSheet
import com.example.music_base.ui.components.AlbumCard
import com.example.music_base.ui.components.ArtistCard
import com.example.music_base.ui.viewmodel.MusicViewModel
import com.example.music_base.ui.viewmodel.MusicState
import com.example.music_base.ui.theme.Dimens
import java.util.Calendar
import com.example.music_base.ui.screens.home.ShowAllType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    accessToken: String? = null,
    modifier: Modifier = Modifier,
    onTrackClick: (Track, List<Track>) -> Unit = { _, _ -> },
    onContinueListeningClick: (index: Int, tracks: List<Track>, listenedSeconds: Int) -> Unit = { _, _, _ -> },
    onArtistClick: (Artist) -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onShowAllClick: (ShowAllType) -> Unit = {},
    onAddToPlaylist: (Track) -> Unit = {},
    onShare: (Track) -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()
    val userPlaylists by viewModel.userPlaylists.collectAsState()
    val suggestedTracks by viewModel.suggestedTracks.collectAsState()
    val rankingTracks by viewModel.rankingTracks.collectAsState()
    val followedArtistIds by viewModel.followedArtistIds.collectAsState()
    val likedTrackIds by viewModel.likedTrackIds.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedTracks.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var trackForMenu by remember { mutableStateOf<Track?>(null) }
    var albumForMenu by remember { mutableStateOf<Album?>(null) }
    var artistForMenu by remember { mutableStateOf<Artist?>(null) }
    
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadData() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                top = Dimens.paddingLarge,
                bottom = Dimens.paddingXLarge * 2 // Extra space for mini player
            )
        ) {
            // Hero Greeting Section
            item {
                HeroSection(greeting = greeting)
            }

            item { Spacer(modifier = Modifier.height(Dimens.paddingXLarge)) }

            // Continue Listening Section - Display independently of uiState
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    Text(
                        text = "Continue Listening",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Dimens.paddingLarge)
                    )
                }
                item { Spacer(modifier = Modifier.height(Dimens.paddingMedium)) }
                item {
                    val validRecentlyPlayed = recentlyPlayed.filter { it.track != null }
                    val recentlyPlayedTracks = validRecentlyPlayed.map { it.track!! }
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Dimens.paddingLarge),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                    ) {
                        itemsIndexed(validRecentlyPlayed) { index, historyItem ->
                            ContinueListeningCard(
                                item = historyItem,
                                onClick = {
                                    onContinueListeningClick(
                                        index,
                                        recentlyPlayedTracks,
                                        historyItem.listenedSeconds
                                    )
                                }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(Dimens.paddingXLarge)) }
            }

            when (val state = uiState) {
                is MusicState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                is MusicState.Error -> {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(Dimens.paddingLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Button(onClick = { viewModel.loadData() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                is MusicState.Success -> {
                    // Top Ranking Section
                    item {
                        SectionHeader(
                            title = "Top Ranking",
                            onShowAllClick = { onShowAllClick(ShowAllType.TOP_RANKING) }
                        )
                    }
                    
                    item {
                        if (rankingTracks.isEmpty()) {
                            // Placeholder
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Dimens.paddingLarge),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
                            ) {
                                items(3) {
                                    Box(
                                        modifier = Modifier
                                            .width(Dimens.albumCardWidth)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Dimens.paddingLarge),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
                            ) {
                                items(rankingTracks) { track ->
                                    TrackCard(
                                        track = track,
                                        onClick = { onTrackClick(track, rankingTracks) },
                                        onMoreClick = { trackForMenu = track },
                                        onQuickLike = { viewModel.toggleTrackLike(track.id) },
                                        isLiked = likedTrackIds.contains(track.id)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.paddingXLarge)) }

                    // Suggested for you Section
                    item {
                        SectionHeader(
                            title = "Suggested for you",
                            onShowAllClick = { onShowAllClick(ShowAllType.SUGGESTED) }
                        )
                    }

                    item {
                        if (suggestedTracks.isEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Dimens.paddingLarge),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
                            ) {
                                items(3) {
                                    Box(
                                        modifier = Modifier
                                            .width(Dimens.albumCardWidth)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(Dimens.radiusMedium))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Dimens.paddingLarge),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
                            ) {
                                items(suggestedTracks) { track ->
                                    TrackCard(
                                        track = track,
                                        onClick = { onTrackClick(track, suggestedTracks) },
                                        onMoreClick = { trackForMenu = track },
                                        onQuickLike = { viewModel.toggleTrackLike(track.id) },
                                        isLiked = likedTrackIds.contains(track.id)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.paddingXLarge)) }

                    // Top Albums Section
                    item {
                        SectionHeader(
                            title = "Top Albums",
                            onShowAllClick = { onShowAllClick(ShowAllType.TOP_ALBUMS) }
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Dimens.paddingLarge),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
                        ) {
                            items(state.albums) { album ->
                                AlbumCard(
                                    album = album,
                                    onClick = { onAlbumClick(album) },
                                    onMoreClick = { albumForMenu = album }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.paddingXLarge)) }

                    // Recommended Artists Section
                    item {
                        SectionHeader(
                            title = "Recommended Artists",
                            onShowAllClick = { onShowAllClick(ShowAllType.TOP_ARTISTS) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.paddingLarge)) }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Dimens.paddingLarge),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingXLarge)
                        ) {
                            items(state.artists) { artist ->
                                ArtistCard(
                                    artist = artist,
                                    onClick = { onArtistClick(artist) },
                                    onMoreClick = { artistForMenu = artist },
                                    onQuickFollow = {
                                        if (followedArtistIds.contains(artist.id)) {
                                            viewModel.unfollowArtist(artist.id)
                                        } else {
                                            viewModel.followArtist(artist.id)
                                        }
                                    },
                                    isFollowed = followedArtistIds.contains(artist.id)
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.paddingXLarge)) }

                    // Your Playlists Section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.paddingLarge),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Playlists",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (accessToken != null) {
                                IconButton(onClick = { showCreateDialog = true }) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Create Playlist",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.paddingLarge)) }

                    items(userPlaylists) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.paddingLarge)
                                .padding(bottom = Dimens.paddingMedium)
                        )
                    }
                }
            }
        }
    }


    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description, isPub ->
                if (name.isNotBlank()) {
                    viewModel.createPlaylist(name, description, isPub)
                    showCreateDialog = false
                }
            }
        )
    }

    if (trackForMenu != null) {
        TrackActionSheet(
            track = trackForMenu!!,
            onDismiss = { trackForMenu = null },
            onToggleLike = { viewModel.toggleTrackLike(trackForMenu!!.id) },
            isLiked = likedTrackIds.contains(trackForMenu!!.id),
            onAddToPlaylist = { onAddToPlaylist(trackForMenu!!) },
            onShare = { onShare(trackForMenu!!) }
        )

    }

    if (albumForMenu != null) {
        AlbumActionSheet(
            album = albumForMenu!!,
            onDismiss = { albumForMenu = null },
            onViewArtist = { 
                albumForMenu!!.artist?.let { onArtistClick(it) }
            }
        )
    }

    if (artistForMenu != null) {
        ArtistActionSheet(
            artist = artistForMenu!!,
            onDismiss = { artistForMenu = null },
            isFollowed = followedArtistIds.contains(artistForMenu!!.id),
            onToggleFollow = {
                if (followedArtistIds.contains(artistForMenu!!.id)) {
                    viewModel.unfollowArtist(artistForMenu!!.id)
                } else {
                    viewModel.followArtist(artistForMenu!!.id)
                }
            }
        )
    }
}

@Composable
fun HeroSection(greeting: String) {
    val glowColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge)
    ) {
        // Optimized glow: Use radial gradient instead of real-time blur(120.dp)
        // This is much faster for scrolling on Android
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        )

        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            Text(
                text = "Curating your evening soundtrack.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onShowAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        TextButton(onClick = onShowAllClick) {
            Text(
                text = "SHOW ALL",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    Spacer(modifier = Modifier.height(Dimens.paddingLarge))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackCard(
    track: Track,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    onQuickLike: () -> Unit = {},
    isLiked: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(Dimens.albumCardWidth)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMoreClick
            )
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(Dimens.radiusMedium))
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Play button overlay

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.paddingMedium)
                    .size(Dimens.iconXLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(Dimens.iconMedium)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        Text(
            text = track.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${track.artistName ?: ""} • ${track.formattedViews}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}



// ─── Continue Listening Card ────────────────────────────────────────────────

@Composable
fun ContinueListeningCard(
    item: PlaybackHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rawProgress = if (item.trackDuration > 0)
        item.listenedSeconds.toFloat() / item.trackDuration else 0f
    val progress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(Dimens.radiusMedium),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            // Album art Square
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = Dimens.radiusMedium, topEnd = Dimens.radiusMedium))
            ) {
                AsyncImage(
                    model = item.trackThumb ?: item.track?.coverUrl,
                    contentDescription = item.trackName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Play overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp).padding(4.dp)
                        )
                    }
                }
            }

            // Info Section
            Column(
                modifier = Modifier
                    .padding(Dimens.paddingSmall + 4.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.trackName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.track?.artistName ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(Dimens.paddingSmall))
                
                // Slim Progress
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = primaryColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)


@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(Dimens.radiusMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = playlist.coverUrl,
                contentDescription = playlist.name,
                modifier = Modifier
                    .size(Dimens.playlistThumbnailSize)
                    .clip(RoundedCornerShape(topStart = Dimens.radiusMedium, bottomStart = Dimens.radiusMedium)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.paddingNormal)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${playlist.trackCount} tracks",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
