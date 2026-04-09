package com.example.music_base.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Playlist
import com.example.music_base.data.model.PlaylistDetail
import com.example.music_base.data.model.Track
import com.example.music_base.ui.components.CreatePlaylistDialog
import com.example.music_base.ui.components.TrackRowObsidian
import com.example.music_base.ui.components.TrackActionSheet
import com.example.music_base.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: MusicViewModel,
    playlist: Playlist,
    playlistDetail: PlaylistDetail?,
    currentPlayingTrack: Track?,
    onBackClick: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayAllClick: (List<Track>) -> Unit,
    onShuffleClick: (List<Track>) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRefresh: () -> Unit,
    onEditPlaylist: (String, String, Boolean) -> Unit,
    onAddToPlaylist: (Track) -> Unit = {},
    onShare: (Track) -> Unit = {},
    isShuffleEnabled: Boolean = false,
    onToggleShuffle: () -> Unit = {}

) {
    var showEditDialog by remember { mutableStateOf(false) }
    var trackForMenu by remember { mutableStateOf<Track?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 1)
    
    // Snapping logic for Search Bar
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstItemIndex = listState.firstVisibleItemIndex
            val firstItemOffset = listState.firstVisibleItemScrollOffset
            
            if (firstItemIndex == 0) {
                val firstItemSize = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                if (firstItemSize > 0) {
                    if (firstItemOffset > firstItemSize / 2) {
                        // More than halfway through hiding, snap to index 1
                        listState.animateScrollToItem(1)
                    } else if (firstItemOffset > 0) {
                        // Less than halfway hiding, snap back to index 0 (show)
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val detailError by viewModel.playlistDetailError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack, 
                            null, 
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Rounded.MoreVert, 
                                null, 
                                tint = Color(0xFF1DB954),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.95f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(220.dp).padding(4.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit playlist", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { 
                                    Box(Modifier.size(36.dp).background(Color(0xFF262626), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Edit, null, tint = Color(0xFF1DB954), modifier = Modifier.size(18.dp))
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { 
                                    Box(Modifier.size(36.dp).background(Color(0xFF262626), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Share, null, tint = Color(0xFF1DB954), modifier = Modifier.size(18.dp))
                                    }
                                },
                                onClick = { showMenu = false }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.05f))
                            DropdownMenuItem(
                                text = { Text("Delete playlist", color = Color(0xFFFF7351), fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { 
                                    Box(Modifier.size(36.dp).background(Color(0xFFFF7351).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF7351), modifier = Modifier.size(18.dp))
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDeletePlaylist()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            val tracks = playlistDetail?.tracks?.data?.map { it.track } ?: emptyList()
            val filteredTracks = tracks.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.artistName?.contains(searchQuery, ignoreCase = true) == true
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Search Bar at the very top (revealed on pull-up)
                item {
                    val searchFocused = remember { mutableStateOf(false) }
                    
                    // Dynamic calculation for "Smooth Reveal"
                    val pullProgress by remember {
                        derivedStateOf {
                            if (listState.firstVisibleItemIndex == 0) {
                                val size = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1
                                1f - (listState.firstVisibleItemScrollOffset.toFloat() / size.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .graphicsLayer {
                                alpha = pullProgress
                                scaleX = 0.95f + (0.05f * pullProgress)
                                scaleY = 0.95f + (0.05f * pullProgress)
                                translationY = -20f * (1f - pullProgress)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.07f))
                                .then(
                                    if (searchFocused.value)
                                        Modifier.background(Color(0xFF1DB954).copy(alpha = 0.04f))
                                    else Modifier
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(20.dp))
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = Color(0xFF1DB954),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1DB954)),
                                decorationBox = { innerField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Find in playlist",
                                                color = Color.White.copy(alpha = 0.35f),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        innerField()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { searchFocused.value = it.isFocused }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Rounded.Close, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Spacer(Modifier.width(20.dp))
                            }
                        }
                    }
                }

                // HERO SECTION
                // IMMERSIVE HERO SECTION
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp)
                    ) {
                        // Background Hero Image
                        AsyncImage(
                            model = playlist.coverUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.4f
                        )
                        
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startY = 0f,
                                        endY = 1200f
                                    )
                                )
                        )

                        // Centered Content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Centered Album Cover
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .shadow(32.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                AsyncImage(
                                    model = playlist.coverUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            // Playlist Title
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-1.5).sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            // Tracks Count Label
                            Text(
                                text = "Playlist • ${if (searchQuery.isEmpty()) tracks.size else filteredTracks.size} songs",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ACTION BAR — 3 Circles: Shuffle, Play, Share
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        if (isShuffleEnabled) Color(0xFF1DB954).copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.1f), 
                                        CircleShape
                                    )
                                    .clickable { onToggleShuffle() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Shuffle, 
                                    null, 
                                    tint = if (isShuffleEnabled) Color(0xFF1DB954) else Color.White, 
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Play Button (Large Green Circle)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFF1DB954), CircleShape)
                                    .shadow(12.dp, CircleShape)
                                    .clickable { if (tracks.isNotEmpty()) onPlayAllClick(tracks) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.PlayArrow, null, tint = Color(0xFF003817), modifier = Modifier.size(40.dp))
                            }

                            // Share Button
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                    .clickable { 
                                        // Share playlist itself logic or currently playing?
                                        // For now, let's keep the existing logic or add a toast
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Share, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }

                        }
                    }
                }

                // TRACK LIST
                if (detailError != null) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(64.dp), tint = Color(0xFFFF7351).copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (detailError?.contains("403") == true || detailError?.contains("private") == true) 
                                    "This playlist is private" else "Failed to load tracks",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = detailError ?: "",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Text("Try Refresh")
                            }
                        }
                    }
                } else if (filteredTracks.isEmpty() && !isRefreshing) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.MusicNote, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.1f))
                            Text(
                                text = if (searchQuery.isEmpty()) "No tracks in this playlist yet" else "No matching tracks found",
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    items(filteredTracks) { track ->
                        TrackRowObsidian(
                            track = track,
                            isCurrentlyPlaying = track.id == currentPlayingTrack?.id,
                            onClick = { onTrackClick(track, tracks) },
                            onMoreClick = { trackForMenu = track }
                        )
                    }
                }
                
                // Bottom spacer to ensure scrolling is possible even with few tracks
                item {
                    Spacer(Modifier.height(600.dp))
                }
            }
        }

        if (trackForMenu != null) {
            TrackActionSheet(
                track = trackForMenu!!,
                onDismiss = { trackForMenu = null },
                onRemoveFromPlaylist = {
                    viewModel.removeTrackFromPlaylist(playlist.id, trackForMenu!!.id)
                },
                onToggleLike = {
                    viewModel.toggleTrackLike(trackForMenu!!.id)
                },
                isLiked = viewModel.likedTrackIds.value.contains(trackForMenu!!.id),
                onAddToPlaylist = {
                    onAddToPlaylist(trackForMenu!!)
                },
                onShare = {
                    onShare(trackForMenu!!)
                }
            )

        }

        if (showEditDialog) {
            val name = playlistDetail?.name ?: playlist.name
            val description = playlistDetail?.description ?: playlist.description ?: ""
            val isPublic = playlistDetail?.isPublic ?: playlist.isPublic
            
            CreatePlaylistDialog(
                initialName = name,
                initialDescription = description,
                initialIsPublic = isPublic,
                title = "Update Playlist",
                buttonText = "Update",
                onDismiss = { showEditDialog = false },
                onCreate = { n, d, p ->
                    onEditPlaylist(n, d, p)
                    showEditDialog = false
                }
            )
        }
    }
}



