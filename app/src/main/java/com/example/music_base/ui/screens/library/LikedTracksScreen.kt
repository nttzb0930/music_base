package com.example.music_base.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import com.example.music_base.ui.components.SortLayoutHeader
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track
import com.example.music_base.ui.components.TrackRowObsidian
import com.example.music_base.ui.components.TrackActionSheet
import com.example.music_base.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedTracksScreen(
    viewModel: MusicViewModel,
    currentPlayingTrack: Track?,
    onBackClick: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayAllClick: (List<Track>) -> Unit,
    onShuffleClick: (List<Track>) -> Unit,
    isShuffleEnabled: Boolean = false,
    onToggleShuffle: () -> Unit = {},
    onAddToPlaylist: (Track) -> Unit = {},
    onShare: (Track) -> Unit = {}
) {

    val likedTracks by viewModel.likedTracks.collectAsState()
    var selectedSort by remember { mutableStateOf("Recently Added") }
    var isGridView by remember { mutableStateOf(false) }
    
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var trackForMenu by remember { mutableStateOf<Track?>(null) }
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
                        listState.animateScrollToItem(1)
                    } else if (firstItemOffset > 0) {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }

    val sortedTracks = remember(likedTracks, selectedSort) {
        when (selectedSort) {
            "Title" -> likedTracks.sortedBy { it.title }
            "Artist" -> likedTracks.sortedBy { it.artistName }
            else -> likedTracks // Recently Added (Default)
        }
    }

    LaunchedEffect(searchQuery) {
        delay(300)
        viewModel.loadLikedTracks(searchQuery)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked Songs", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) },
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
                                tint = Color(0xFF72FE8F),
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
                                text = { Text("Clear all liked songs", color = Color(0xFFFF7351), fontSize = 15.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { 
                                    Box(Modifier.size(36.dp).background(Color(0xFFFF7351).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.DeleteSweep, null, tint = Color(0xFFFF7351), modifier = Modifier.size(18.dp))
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.clearAllLikedTracks()
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
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadLikedTracks() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Search Bar (revealed on pull up)
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
                            .background(Color.White.copy(alpha = 0.08f))
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
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(Color(0xFF1DB954)),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Find in liked songs",
                                            color = Color.White.copy(alpha = 0.35f),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    innerTextField()
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
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF72FE8F).copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .shadow(24.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF43e97b), Color(0xFF38f9d7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Favorite,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text(
                            text = "Liked Songs",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        
                        Text(
                            text = "${likedTracks.size} songs",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            // ACTION BAR
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToggleShuffle() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isShuffleEnabled) Color(0xFF72FE8F).copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.05f), 
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Rounded.Shuffle, 
                            null, 
                            tint = if (isShuffleEnabled) Color(0xFF72FE8F) else Color.White
                        )
                    }
                    
                    Spacer(Modifier.width(24.dp))
                    
                    FloatingActionButton(
                        onClick = { if (likedTracks.isNotEmpty()) onPlayAllClick(likedTracks) },
                        containerColor = Color(0xFF72FE8F),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // SORT & LAYOUT HEADER
            item {
                SortLayoutHeader(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    currentSortName = selectedSort,
                    sortOptions = listOf("Recently Added", "Title", "Artist"),
                    onSortSelected = { selectedSort = it },
                    isGrid = isGridView,
                    onToggleGrid = { isGridView = !isGridView }
                )
            }

            // TRACK LIST
            if (sortedTracks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.FavoriteBorder,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.White.copy(alpha = 0.05f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Songs you like will appear here",
                            color = Color.White.copy(alpha = 0.3f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                if (isGridView) {
                    item {
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(160.dp),
                            modifier = Modifier.fillMaxWidth().height(1000.dp).padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            gridItems(sortedTracks) { trackItem ->
                                com.example.music_base.ui.components.TrackGridItem(
                                    track = trackItem,
                                    onClick = { onTrackClick(trackItem, sortedTracks) }
                                )
                            }
                        }

                    }
                }
 else {
                    lazyItems(sortedTracks) { trackItem ->
                        TrackRowObsidian(
                            track = trackItem,
                            isCurrentlyPlaying = trackItem.id == currentPlayingTrack?.id,
                            onClick = { onTrackClick(trackItem, sortedTracks) },
                            onMoreClick = { trackForMenu = trackItem }
                        )
                    }
                }

            }

            
            // Add a bottom spacer to ensure the list is long enough to hide the search bar via initialFirstVisibleItemIndex = 1
            // Even if there are very few tracks (like 2 songs).
            item {
                Spacer(modifier = Modifier.height(1000.dp))
            }
        } // end LazyColumn
        } // end PullToRefreshBox

        if (trackForMenu != null) {
            TrackActionSheet(
                track = trackForMenu!!,
                onDismiss = { trackForMenu = null },
                onToggleLike = {
                    viewModel.toggleTrackLike(trackForMenu!!.id)
                },
                isLiked = true, // Since we are in LikedTracksScreen
                onAddToPlaylist = { onAddToPlaylist(trackForMenu!!) },
                onShare = { onShare(trackForMenu!!) }
            )

        }
    }
}
