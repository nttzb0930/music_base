package com.example.music_base.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.ui.components.SortLayoutHeader
import com.example.music_base.data.model.Playlist
import com.example.music_base.data.model.Artist

import com.example.music_base.ui.components.CreatePlaylistDialog
import com.example.music_base.ui.viewmodel.MusicViewModel
import com.example.music_base.ui.theme.Dimens




data class LibraryItem(
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val icon: ImageVector? = null,
    val originalData: Any? = null
)

enum class LibraryFilter { All, Playlists, Artists, Albums }
enum class LibrarySort { Recents, Alphabetical, Creator }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onPlaylistClick: (Playlist) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onLikedSongsClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(LibraryFilter.All) }
    var selectedSort by remember { mutableStateOf(LibrarySort.Recents) }
    var isGridView by remember { mutableStateOf(false) }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToEdit by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var playlistForMenu by remember { mutableStateOf<Playlist?>(null) }


    val userPlaylists by viewModel.userPlaylists.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, isPub ->
                viewModel.createPlaylist(name, desc, isPub)
                showCreateDialog = false
            }
        )
    }

    if (playlistToEdit != null) {
        CreatePlaylistDialog(
            title = "Edit Playlist",
            buttonText = "Save Changes",
            initialName = playlistToEdit!!.name,
            initialDescription = playlistToEdit!!.description ?: "",
            initialIsPublic = playlistToEdit!!.isPublic,
            onDismiss = { playlistToEdit = null },
            onCreate = { name, desc, isPub ->
                viewModel.updatePlaylist(playlistToEdit!!.id, name, desc, isPub)
                playlistToEdit = null
            }
        )
    }

    if (playlistToDelete != null) {
        DeletePlaylistConfirmationDialog(
            playlistName = playlistToDelete!!.name,
            onDismiss = { playlistToDelete = null },
            onConfirm = {
                viewModel.deletePlaylist(playlistToDelete!!.id)
                playlistToDelete = null
            }
        )
    }

    if (playlistForMenu != null) {
        val context = androidx.compose.ui.platform.LocalContext.current
        ModalBottomSheet(
            onDismissRequest = { playlistForMenu = null },
            containerColor = Color(0xFF131313),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp).width(40.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
                // Header in sheet
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AsyncImage(
                        model = playlistForMenu!!.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(playlistForMenu!!.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Playlist • ${playlistForMenu!!.trackCount} tracks", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Menu Options
                TextButton(
                    onClick = { playlistToEdit = playlistForMenu; playlistForMenu = null },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Edit, null, tint = Color.White.copy(alpha = 0.6f))
                        Spacer(Modifier.width(16.dp))
                        Text("Edit Playlist", color = Color.White, fontSize = 16.sp)
                    }
                }
                
                TextButton(
                    onClick = { sharePlaylist(context, playlistForMenu!!); playlistForMenu = null },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Share, null, tint = Color.White.copy(alpha = 0.6f))
                        Spacer(Modifier.width(16.dp))
                        Text("Share Playlist", color = Color.White, fontSize = 16.sp)
                    }
                }
                
                TextButton(
                    onClick = { playlistToDelete = playlistForMenu; playlistForMenu = null },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF7351))
                        Spacer(Modifier.width(16.dp))
                        Text("Delete Playlist", color = Color(0xFFFF7351), fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    val filteredItems = remember(selectedFilter, userPlaylists, followedArtists, selectedSort) {
        val list = mutableListOf<LibraryItem>()
        
        if (selectedFilter == LibraryFilter.All || selectedFilter == LibraryFilter.Playlists) {
            list.addAll(userPlaylists.map {
                LibraryItem(it.name, "Playlist • ${it.trackCount} songs", it.coverUrl, originalData = it)
            })
        }
        if (selectedFilter == LibraryFilter.All || selectedFilter == LibraryFilter.Artists) {
            list.addAll(followedArtists.map {
                LibraryItem(it.name, "Artist", it.imageUrl, originalData = it)
            })
        }
        
        // Apply Sort
        val sortedList = when (selectedSort) {
            LibrarySort.Alphabetical -> list.sortedBy { it.title }
            LibrarySort.Creator -> list.sortedBy { it.subtitle }
            LibrarySort.Recents -> list // Assuming default is recents
        }.toMutableList()
        
        // Add "Liked Songs" as a special entry at the top if appropriate
        if (selectedFilter == LibraryFilter.All || selectedFilter == LibraryFilter.Playlists) {
            sortedList.add(0, LibraryItem("Liked Songs", "Playlist • Liked tracks", "liked_songs_icon", originalData = "LIKED_SONGS"))
        }
        
        sortedList
    }


    Column(modifier = Modifier.fillMaxSize()) {
        LibraryHeroSection(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it },
            onCreateNewClick = { showCreateDialog = true }
        )

        val isRefreshing by viewModel.isRefreshing.collectAsState()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadMyPlaylists() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (isGridView) {
                // Grid View Logic
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(160.dp),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
                ) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        RecentsHeader(
                            currentSort = selectedSort,
                            isGrid = isGridView,
                            onSortClick = { selectedSort = it },
                            onToggleGrid = { isGridView = !isGridView }
                        )
                    }
                    
                    if (selectedFilter == LibraryFilter.All) {
                        val playlists = filteredItems.filter { it.originalData is Playlist || it.originalData == "LIKED_SONGS" }
                        val artists = filteredItems.filter { it.originalData is Artist }
                        
                        if (playlists.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Text("Playlists", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            gridItems(playlists) { libraryItem ->
                                LibraryGridItem(
                                    item = libraryItem,
                                    onClick = {
                                        if (libraryItem.originalData == "LIKED_SONGS") onLikedSongsClick()
                                        else if (libraryItem.originalData is Playlist) onPlaylistClick(libraryItem.originalData)
                                        else if (libraryItem.originalData is Artist) onArtistClick(libraryItem.originalData)
                                    }
                                )
                            }
                        }
                        
                        if (artists.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Text("Artists", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = Dimens.paddingLarge, bottom = 8.dp))
                            }
                            gridItems(artists) { libraryItem ->
                                LibraryGridItem(
                                    item = libraryItem,
                                    onClick = {
                                        if (libraryItem.originalData == "LIKED_SONGS") onLikedSongsClick()
                                        else if (libraryItem.originalData is Playlist) onPlaylistClick(libraryItem.originalData)
                                        else if (libraryItem.originalData is Artist) onArtistClick(libraryItem.originalData)
                                    }
                                )
                            }
                        }
                    } else {
                        gridItems(filteredItems) { libraryItem ->
                            LibraryGridItem(
                                item = libraryItem,
                                onClick = {
                                    if (libraryItem.originalData == "LIKED_SONGS") onLikedSongsClick()
                                    else if (libraryItem.originalData is Playlist) onPlaylistClick(libraryItem.originalData)
                                    else if (libraryItem.originalData is Artist) onArtistClick(libraryItem.originalData)
                                }
                            )
                        }
                    }


                }
            } else {
                // List View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
                ) {
                    item {
                        RecentsHeader(
                            currentSort = selectedSort,
                            isGrid = isGridView,
                            onSortClick = { selectedSort = it },
                            onToggleGrid = { isGridView = !isGridView }
                        )
                    }

                    if (selectedFilter == LibraryFilter.All) {
                        val playlists = filteredItems.filter { it.originalData is Playlist || it.originalData == "LIKED_SONGS" }
                        val artists = filteredItems.filter { it.originalData is Artist }
                        
                        if (playlists.isNotEmpty()) {
                            item {
                                Text("Playlists", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            lazyItems(playlists) { libraryItem ->
                                LibraryRowObsidian(
                                    item = libraryItem,
                                    onClick = {
                                        if (libraryItem.originalData == "LIKED_SONGS") onLikedSongsClick()
                                        else if (libraryItem.originalData is Playlist) onPlaylistClick(libraryItem.originalData)
                                        else if (libraryItem.originalData is Artist) onArtistClick(libraryItem.originalData)
                                    },
                                    onLongClick = {
                                        if (libraryItem.originalData is Playlist) {
                                            playlistForMenu = libraryItem.originalData
                                        }
                                    }
                                )
                            }
                        }
                        
                        if (artists.isNotEmpty()) {
                            item {
                                Text("Artists", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = Dimens.paddingLarge, bottom = 8.dp))
                            }
                            lazyItems(artists) { libraryItem ->
                                LibraryRowObsidian(
                                    item = libraryItem,
                                    onClick = {
                                        if (libraryItem.originalData == "LIKED_SONGS") onLikedSongsClick()
                                        else if (libraryItem.originalData is Playlist) onPlaylistClick(libraryItem.originalData)
                                        else if (libraryItem.originalData is Artist) onArtistClick(libraryItem.originalData)
                                    },
                                    onLongClick = {
                                        if (libraryItem.originalData is Playlist) {
                                            playlistForMenu = libraryItem.originalData
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        lazyItems(filteredItems) { libraryItem ->
                            LibraryRowObsidian(
                                item = libraryItem,
                                onClick = {
                                    if (libraryItem.originalData == "LIKED_SONGS") onLikedSongsClick()
                                    else if (libraryItem.originalData is Playlist) onPlaylistClick(libraryItem.originalData)
                                    else if (libraryItem.originalData is Artist) onArtistClick(libraryItem.originalData)
                                },
                                onLongClick = {
                                    if (libraryItem.originalData is Playlist) {
                                        playlistForMenu = libraryItem.originalData
                                    }
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}


@Composable
fun LibraryHeroSection(
    selectedFilter: LibraryFilter,
    onFilterSelected: (LibraryFilter) -> Unit,
    onCreateNewClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "Library",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Button(
                onClick = onCreateNewClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF72FE8F)),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Create New", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF72FE8F) else Color.White.copy(alpha = 0.05f))
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        filter.name,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecentsHeader(
    currentSort: LibrarySort,
    isGrid: Boolean,
    onSortClick: (LibrarySort) -> Unit,
    onToggleGrid: () -> Unit
) {
    SortLayoutHeader(
        currentSortName = when(currentSort) {
            LibrarySort.Recents -> "Recents"
            LibrarySort.Alphabetical -> "Alphabetical"
            LibrarySort.Creator -> "Creator"
        },
        sortOptions = listOf("Recents", "Alphabetical", "Creator"),
        onSortSelected = { name ->
            val sort = when(name) {
                "Alphabetical" -> LibrarySort.Alphabetical
                "Creator" -> LibrarySort.Creator
                else -> LibrarySort.Recents
            }
            onSortClick(sort)
        },
        isGrid = isGrid,
        onToggleGrid = onToggleGrid
    )
}


@Composable
fun LibraryGridItem(
    item: LibraryItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(if (item.subtitle == "Artist") CircleShape else RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (item.originalData == "LIKED_SONGS") {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(Color(0xFF43e97b), Color(0xFF38f9d7)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Favorite, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            } else {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            item.title,
            color = if (item.title == "Liked Songs") Color(0xFF72FE8F) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            item.subtitle,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryRowObsidian(
    item: LibraryItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .hoverable(interactionSource)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(if (item.subtitle == "Artist") CircleShape else RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (item.originalData == "LIKED_SONGS") {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(Color(0xFF43e97b), Color(0xFF38f9d7)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Favorite, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            } else {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.title == "Liked Songs") Color(0xFF72FE8F) else Color.White,
                maxLines = 1
            )
            Text(
                item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Icon(
            Icons.Rounded.ChevronRight,
            null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun sharePlaylist(context: android.content.Context, playlist: Playlist) {
    val shareIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this playlist: ${playlist.name}\nListen at: https://musicbase.com/playlist/${playlist.id}")
    }
    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Playlist via"))
}

@Composable
fun DeletePlaylistConfirmationDialog(
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = { Text("Delete Playlist", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to delete '$playlistName'? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
