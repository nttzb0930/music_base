package com.example.music_base.ui.screens.player
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.shadow
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track
import com.example.music_base.data.model.Playlist
import com.example.music_base.data.model.Artist
import com.example.music_base.ui.components.CreatePlaylistDialog
import com.example.music_base.ui.components.AddToPlaylistSheet
import com.example.music_base.ui.components.ShareSheet


@Composable
fun PlayerScreen(
    track: Track,
    isPlaying: Boolean,
    userPlaylists: List<Playlist> = emptyList(),
    playingFrom: String = "Sonic Gallery",
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onAddToPlaylistClick: (Playlist) -> Unit = {},
    onCreatePlaylist: (String, String?, Boolean) -> Unit = { _, _, _ -> },
    onSeek: (Long) -> Unit = {},
    isLiked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    isShuffleEnabled: Boolean = false,
    onShuffleClick: () -> Unit = {},
    repeatMode: com.example.music_base.data.player.RepeatMode = com.example.music_base.data.player.RepeatMode.OFF,
    onRepeatClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current



    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, description, isPub ->
                onCreatePlaylist(name, description, isPub)
                showCreatePlaylistDialog = false
                showPlaylistDialog = false
            }
        )
    }

    if (showPlaylistDialog) {
        AddToPlaylistSheet(
            playlists = userPlaylists,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistClick = { playlist ->
                onAddToPlaylistClick(playlist)
                showPlaylistDialog = false
            },
            onCreateNewClick = { 
                showCreatePlaylistDialog = true 
            }
        )
    }

    if (showShareSheet) {
        ShareSheet(
            track = track,
            onDismiss = { showShareSheet = false }
        )
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundAmbientGlows()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            val screenHeight = maxHeight
            val albumArtSize = screenHeight * 0.40f
            val spacerTop = screenHeight * 0.02f
            val spacerMid = screenHeight * 0.03f
            val spacerBot = screenHeight * 0.025f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(spacerTop))
                PlayerHeader(playingFrom = playingFrom, onBackClick = onBackClick)
                
                // Content section that can grow/center
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(albumArtSize)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        PlayerAlbumArt(track.coverUrl)
                    }
                    Spacer(Modifier.height(spacerMid))
                    TrackInfoSection(
                        track = track,
                        isLiked = isLiked,
                        onLikeClick = onLikeClick,
                        onArtistClick = { track.artist?.let { onArtistClick(it) } }
                    )
                    Spacer(Modifier.height(12.dp))
                    PlaybackControls(
                        track,
                        isPlaying,
                        onPlayPauseClick,
                        onPreviousClick,
                        onNextClick,
                        onSeek,
                        isShuffleEnabled = isShuffleEnabled,
                        onShuffleClick = onShuffleClick,
                        repeatMode = repeatMode,
                        onRepeatClick = onRepeatClick
                    )
                }

                // Bottom anchored controls
                Spacer(Modifier.height(spacerBot))
                UtilityControls(
                    onAddToPlaylistClick = { showPlaylistDialog = true },
                    onShareClick = { showShareSheet = true },
                    onConnectClick = onConnectClick
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BackgroundAmbientGlows() {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
                .offset((-40).dp, (-80).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomEnd)
                .offset(40.dp, 80.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary.copy(0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun PlayerHeader(playingFrom: String, onBackClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.ExpandMore,
            null,
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .clickable { onBackClick() }
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "PLAYING FROM",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = Color.White.copy(.5f)
            )
            Text(
                playingFrom,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Rounded.MoreHoriz,
            null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun PlayerAlbumArt(url: String) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize(.95f)
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(.1f),
                    RoundedCornerShape(12.dp)
                )
        )
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun TrackInfoSection(track: Track, isLiked: Boolean, onLikeClick: () -> Unit, onArtistClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artistName ?: "",
                color = MaterialTheme.colorScheme.primary.copy(.9f),
                modifier = Modifier
                    .clickable { onArtistClick() }
                    .padding(vertical = 4.dp)
            )
        }
        Icon(
            if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            null,
            tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White.copy(.4f),
            modifier = Modifier
                .size(26.dp)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { onLikeClick() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackControls(
    track: Track,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    isShuffleEnabled: Boolean = false,
    onShuffleClick: () -> Unit = {},
    repeatMode: com.example.music_base.data.player.RepeatMode = com.example.music_base.data.player.RepeatMode.OFF,
    onRepeatClick: () -> Unit = {}
) {
    val progress = if (track.durationMs > 0) track.currentPosition.toFloat() / track.durationMs else 0f
    val animatedProgress by animateFloatAsState(progress, label = "")

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = animatedProgress,
            onValueChange = { onSeek((it * track.durationMs).toLong()) },
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                // Hidden thumb for a sleek look as per reference image
                Box(modifier = Modifier.size(0.dp))
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(3.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF1DB954),
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    thumbTrackGapSize = 0.dp
                )
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .offset(y = (-4).dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimeText(track.currentPosition)
            TimeText(track.durationMs)
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Icon(
                    Icons.Rounded.SkipPrevious,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp).clickable { onPreviousClick() }
                )
                Surface(
                    onClick = onPlayPauseClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(68.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Icon(
                    Icons.Rounded.SkipNext,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp).clickable { onNextClick() }
                )
            }
            Icon(
                Icons.Rounded.Shuffle,
                null,
                tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(.4f),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onShuffleClick() }
            )
            Icon(
                if (repeatMode == com.example.music_base.data.player.RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                null,
                tint = if (repeatMode != com.example.music_base.data.player.RepeatMode.OFF) MaterialTheme.colorScheme.primary else Color.White.copy(.4f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onRepeatClick() }
            )
        }
    }
}

@Composable
private fun TimeText(time: Long) {
    Text(
        formatTime(time),
        fontSize = 11.sp,
        color = Color.White.copy(.4f),
        fontWeight = FontWeight.Bold
    )
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun UtilityControls(
    onAddToPlaylistClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onConnectClick: () -> Unit = {}
) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
        UtilityItem(Icons.AutoMirrored.Rounded.PlaylistAdd, "Add", onClick = onAddToPlaylistClick)
        UtilityItem(Icons.Rounded.Lyrics, "Lyrics")
        UtilityItem(Icons.Rounded.Devices, "Connect", onClick = onConnectClick)
        UtilityItem(Icons.Outlined.Share, "Share", onClick = onShareClick)
    }
}



@Composable
fun UtilityItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, null, tint = Color.White.copy(.7f), modifier = Modifier.size(28.dp))
        Text(
            label,
            fontSize = 10.sp,
            color = Color.White.copy(.6f),
            fontWeight = FontWeight.Medium
        )
    }
}