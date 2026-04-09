package com.example.music_base.ui.screens.album

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.*

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Album
import com.example.music_base.data.model.AlbumDetail
import com.example.music_base.data.model.Track
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    album: Album,
    albumDetail: AlbumDetail?,
    currentPlayingTrack: Track?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayAllClick: (List<Track>) -> Unit,
    onShuffleClick: (List<Track>) -> Unit,
    onToggleShuffle: () -> Unit = {},
    isShuffleEnabled: Boolean = false,
    onFavoriteClick: (Album) -> Unit,
    onRefresh: () -> Unit = {},
    onShare: (Track) -> Unit = {}
) {
    var trackForMenu by remember { mutableStateOf<Track?>(null) }


    val tracks = albumDetail?.tracks ?: emptyList()
    val totalDuration = tracks.sumOf { it.durationMs }
    val totalMins = TimeUnit.MILLISECONDS.toMinutes(totalDuration)
    val hours = totalMins / 60
    val mins = totalMins % 60
    val durationLabel = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    val trackCountLabel = "${tracks.size} songs"

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ── Hero Section ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                ) {
                    // Blurred background
                    AsyncImage(
                        model = album.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(40.dp),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color(0x550E0E0E),
                                    0.4f to Color(0x880E0E0E),
                                    1f to MaterialTheme.colorScheme.background
                                )
                            )
                    )
                    // Content
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Text(
                            text = "STUDIO ALBUM",
                            color = Color(0xFF72FE8F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        // Album Art + Title Row
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            AsyncImage(
                                model = album.coverUrl,
                                contentDescription = album.title,
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column {
                                Text(
                                    text = album.title,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 34.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                // Artist row with avatar
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AsyncImage(
                                        model = album.artist?.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = album.artist?.name ?: "Unknown Artist",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (tracks.isNotEmpty()) {
                                        Text(text = trackCountLabel, color = Color(0xFFADAAAA), fontSize = 12.sp)
                                        Text(text = "·", color = Color(0xFF484847), fontSize = 12.sp)
                                        Text(text = durationLabel, color = Color(0xFFADAAAA), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Action Bar ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF72FE8F), Color(0xFF1CB853))
                                    )
                                )
                                .clickable { if (tracks.isNotEmpty()) onPlayAllClick(tracks) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                tint = Color(0xFF005F26),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) Color(0xFF72FE8F) else Color(0xFFADAAAA),
                            modifier = Modifier.size(28.dp).clickable { onToggleShuffle() }
                        )
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color(0xFFADAAAA),
                            modifier = Modifier.size(28.dp).clickable { onFavoriteClick(album) }
                        )
                    }
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFFADAAAA)
                    )
                }
            }

            // ── Track List Header ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        color = Color(0xFF767575),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = "TITLE",
                        color = Color(0xFF767575),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color(0xFF767575),
                        modifier = Modifier.size(18.dp)
                    )
                }
                HorizontalDivider(color = Color(0xFF484847).copy(alpha = 0.2f))
            }

            // ── Tracks ────────────────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF72FE8F))
                    }
                }
            } else {
                itemsIndexed(tracks) { index, track ->
                    TrackRow(
                        index = index + 1,
                        track = track,
                        isPlaying = track.id == currentPlayingTrack?.id,
                        onClick = { onTrackClick(track, tracks) },
                        onMoreClick = { trackForMenu = track }
                    )
                }
            }


            // ── About Section ─────────────────────────────────────────────
            albumDetail?.artist?.let { artist ->
                item {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "About this album",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF131313))
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.name,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = artist.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = artist.description ?: "",
                                color = Color(0xFFADAAAA),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        } // end PullToRefreshBox

        // ── TopBar (floating) ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF72FE8F))
            }
            Text(
                text = "Album Details",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (trackForMenu != null) {
            com.example.music_base.ui.components.TrackActionSheet(
                track = trackForMenu!!,
                onDismiss = { trackForMenu = null },
                onShare = { onShare(trackForMenu!!) }
            )
        }
    }
}



@Composable
private fun TrackRow(
    index: Int,
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {

    val durationSecs = (track.duration).toLong()
    val mins = durationSecs / 60
    val secs = durationSecs % 60
    val durationStr = "%d:%02d".format(mins, secs)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (isPlaying) Color(0xFF262626) else Color.Transparent
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index / Playing Indicator
        Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
            if (isPlaying) {
                PlayingIndicator()
            } else {
                Text(
                    text = "$index",
                    color = Color(0xFFADAAAA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Track Info
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        ) {
            Text(
                text = track.title,
                color = if (isPlaying) Color(0xFF72FE8F) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.artistName ?: ""} • ${track.formattedViews}",
                color = Color(0xFFADAAAA),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // More button
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "More",
                tint = Color(0xFFADAAAA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}



@Composable
private fun PlayingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "playing")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 16f, label = "b1",
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse)
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 4f, label = "b2",
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse)
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 20f, label = "b3",
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(20.dp)
    ) {
        listOf(bar1, bar2, bar3).forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(Color(0xFF72FE8F), RoundedCornerShape(2.dp))
            )
        }
    }
}
