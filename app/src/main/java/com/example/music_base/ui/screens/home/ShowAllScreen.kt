package com.example.music_base.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.music_base.data.model.Album
import com.example.music_base.data.model.Artist

import com.example.music_base.data.model.Track
import com.example.music_base.ui.components.TrackRowObsidian
import com.example.music_base.ui.components.TrackGridItem
import com.example.music_base.ui.components.AlbumCard
import com.example.music_base.ui.components.ArtistCard
import com.example.music_base.ui.components.SortLayoutHeader
import com.example.music_base.ui.theme.Dimens


enum class ShowAllType(val title: String) {
    TOP_RANKING("Top Ranking"),
    SUGGESTED("Suggested for you"),
    TOP_ALBUMS("Top Albums"),
    TOP_ARTISTS("Recommend Artists")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAllScreen(
    type: ShowAllType,
    tracks: List<Track> = emptyList(),
    albums: List<Album> = emptyList(),
    artists: List<com.example.music_base.data.model.Artist> = emptyList(),
    currentPlayingTrack: Track?,
    onBackClick: () -> Unit,
    onTrackClick: (Int, List<Track>) -> Unit,
    onTrackMoreClick: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onAlbumMoreClick: (Album) -> Unit,
    onArtistClick: (com.example.music_base.data.model.Artist) -> Unit = {},
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSort by remember { mutableStateOf("Default") }
    var isGridView by remember { mutableStateOf(type == ShowAllType.TOP_ALBUMS || type == ShowAllType.TOP_ARTISTS) }

    val sortedTracks = remember(tracks, selectedSort) {
        when (selectedSort) {
            "Title" -> tracks.sortedBy { it.title }
            "Artist" -> tracks.sortedBy { it.artistName }
            else -> tracks
        }
    }

    val sortedAlbums = remember(albums, selectedSort) {
        when (selectedSort) {
            "Title" -> albums.sortedBy { it.title }
            "Artist" -> albums.sortedBy { it.artist?.name }
            else -> albums
        }
    }

    val sortedArtists = remember(artists, selectedSort) {
        when (selectedSort) {
            "Name" -> artists.sortedBy { it.name }
            else -> artists
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.paddingNormal, vertical = Dimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Text(
                    text = type.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Sub-header for Sort/Layout
            SortLayoutHeader(
                modifier = Modifier.padding(horizontal = Dimens.paddingNormal),
                currentSortName = selectedSort,
                sortOptions = when(type) {
                    ShowAllType.TOP_ARTISTS -> listOf("Default", "Name")
                    ShowAllType.TOP_ALBUMS -> listOf("Default", "Title", "Artist")
                    else -> listOf("Default", "Title", "Artist")
                },
                onSortSelected = { selectedSort = it },
                isGrid = isGridView,
                onToggleGrid = { isGridView = !isGridView }
            )


            // Content
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 120.dp, start = Dimens.paddingLarge, end = Dimens.paddingLarge),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (type == ShowAllType.TOP_ALBUMS) {
                        gridItems(sortedAlbums) { albumItem ->
                            AlbumCard(
                                album = albumItem,
                                onClick = { onAlbumClick(albumItem) },
                                onMoreClick = { onAlbumMoreClick(albumItem) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else if (type == ShowAllType.TOP_ARTISTS) {
                        gridItems(sortedArtists) { artistItem ->
                            ArtistCard(
                                artist = artistItem,
                                onClick = { onArtistClick(artistItem) },
                                onMoreClick = { /* Dummy empty if not used */ },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        gridItems(sortedTracks) { trackItem ->
                            TrackGridItem(
                                track = trackItem,
                                onClick = { onTrackClick(sortedTracks.indexOf(trackItem), sortedTracks) }
                            )
                        }
                    }



                    item(span = { GridItemSpan(2) }) {
                        LoadMoreButton(
                            isLoading = isLoadingMore,
                            onClick = onLoadMore,
                            modifier = Modifier.padding(vertical = Dimens.paddingLarge)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    if (type == ShowAllType.TOP_ALBUMS) {
                        lazyItems(sortedAlbums) { albumItem ->
                            AlbumListRow(
                                album = albumItem, 
                                onClick = { onAlbumClick(albumItem) }, 
                                onMoreClick = { onAlbumMoreClick(albumItem) }
                            )
                        }
                    } else if (type == ShowAllType.TOP_ARTISTS) {
                        lazyItems(sortedArtists) { artistItem ->
                            ArtistListRow(
                                artist = artistItem, 
                                onClick = { onArtistClick(artistItem) }
                            )
                        }
                    } else {



                        itemsIndexed(sortedTracks) { index, track ->
                            TrackRowObsidian(
                                track = track,
                                isCurrentlyPlaying = track.id == currentPlayingTrack?.id,
                                onClick = { onTrackClick(index, sortedTracks) },
                                onMoreClick = { onTrackMoreClick(track) },
                                rank = if (type == ShowAllType.TOP_RANKING) index + 1 else null,
                                showViews = type == ShowAllType.TOP_RANKING
                            )
                        }
                    }

                    item {
                        LoadMoreButton(
                            isLoading = isLoadingMore,
                            onClick = onLoadMore,
                            modifier = Modifier.padding(vertical = Dimens.paddingLarge)
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun LoadMoreButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Load More . . . ", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun AlbumListRow(
    album: Album,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = album.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = album.artist?.name ?: "Various Artists",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                Icons.Rounded.MoreHoriz,
                null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ArtistListRow(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = artist.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Artist",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

