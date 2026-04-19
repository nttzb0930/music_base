package com.example.music_base.ui.screens.artist

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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.music_base.data.model.Artist
import com.example.music_base.data.model.Track
import com.example.music_base.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: Artist,
    tracks: List<Track>,
    currentPlayingTrack: Track?,
    isFollowing: Boolean,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onFollowClick: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayAllClick: (List<Track>) -> Unit,
    isShuffleEnabled: Boolean = false,
    onShuffleClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onShare: (Track) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var trackForMenu by remember { mutableStateOf<Track?>(null) }


    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Section
            item {
                ArtistHeader(
                    artist = artist,
                    isFollowing = isFollowing,
                    onFollowClick = onFollowClick
                )
            }

            // Controls Row
            item {
                ArtistControls(
                    tracks = tracks,
                    onPlayAllClick = onPlayAllClick,
                    isShuffleEnabled = isShuffleEnabled,
                    onShuffleClick = onShuffleClick
                )
            }

            // Popular Tracks Label
            item {
                Text(
                    text = "Popular Tracks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Tracks List
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF72FE8F))
                    }
                }
            } else {
                itemsIndexed(tracks) { index, track ->
                    ArtistTrackRow(
                        index = index + 1,
                        track = track,
                        isPlaying = track.id == currentPlayingTrack?.id,
                        onClick = { onTrackClick(track, tracks) },
                        onMoreClick = { trackForMenu = track }
                    )
                }
            }

            
            // About Section
            artist.description?.let {
                item {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "About",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF131313))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = it,
                            color = Color(0xFFADAAAA),
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        } // end LazyColumn
        } // end PullToRefreshBox

        // Top AppBar (Transparent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
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
fun ArtistHeader(
    artist: Artist,
    isFollowing: Boolean,
    onFollowClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // Background Image with Blur
        AsyncImage(
            model = artist.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp),
            contentScale = ContentScale.Crop
        )

        // Gradient & Darkening
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = artist.name,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = artist.name,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.Transparent else Color(0xFF72FE8F),
                    contentColor = if (isFollowing) Color.White else Color.Black
                ),
                border = if (isFollowing) ButtonDefaults.outlinedButtonBorder else null,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isFollowing) "FOLLOWING" else "FOLLOW")
            }
        }
    }
}

@Composable
fun ArtistControls(
    tracks: List<Track>,
    onPlayAllClick: (List<Track>) -> Unit,
    isShuffleEnabled: Boolean = false,
    onShuffleClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onPlayAllClick(tracks) },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF72FE8F)),
            modifier = Modifier.size(56.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(Modifier.width(24.dp))
        
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffleEnabled) Color(0xFF72FE8F) else Color(0xFFADAAAA),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ArtistTrackRow(
    index: Int,
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isPlaying) Color.White.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPlaying) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF72FE8F),
                modifier = Modifier.width(32.dp).size(16.dp)
            )
        } else {
            Text(
                text = index.toString(),
                color = Color(0xFFADAAAA),
                modifier = Modifier.width(32.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.formattedViews,
                color = Color(0xFFADAAAA),
                style = MaterialTheme.typography.bodySmall
            )
        }
        
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




