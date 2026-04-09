package com.example.music_base.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track

@Composable
fun TrackRowObsidian(
    track: Track,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    rank: Int? = null,
    showViews: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrentlyPlaying) Color(0xFF72FE8F).copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (rank != null) {
            Text(
                text = if (rank < 10) "0$rank" else "$rank",
                color = if (isCurrentlyPlaying) Color(0xFF72FE8F) else Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Smaller track cover (48dp)
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isCurrentlyPlaying) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.GraphicEq, null, tint = Color(0xFF72FE8F), modifier = Modifier.size(20.dp))
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = if (isCurrentlyPlaying) Color(0xFF72FE8F) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (showViews) "${track.artistName ?: ""} • ${track.formattedViews}" else (track.artistName ?: ""),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackActionSheet(
    track: Track,
    onDismiss: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onRemoveFromHistory: (() -> Unit)? = null,
    onToggleLike: (() -> Unit)? = null,
    isLiked: Boolean = false,
    onShare: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131313),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        track.artistName ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Actions
            TrackActionItem(
                icon = Icons.Rounded.Share,
                label = "Chia sẻ",
                onClick = { onShare(); onDismiss() }
            )
            
            TrackActionItem(
                icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                label = if (isLiked) "Bỏ thích" else "Thêm vào bài hát ưa thích",
                iconColor = if (isLiked) Color(0xFF72FE8F) else Color.White.copy(alpha = 0.6f),
                onClick = { onToggleLike?.invoke(); onDismiss() }
            )

            TrackActionItem(
                icon = Icons.Rounded.PlaylistAdd,
                label = "Thêm vào playlist",
                onClick = { onAddToPlaylist(); onDismiss() }
            )

            if (onRemoveFromPlaylist != null) {
                TrackActionItem(
                    icon = Icons.Rounded.RemoveCircleOutline,
                    label = "Xóa khỏi danh sách phát này",
                    iconColor = Color(0xFFFF7351),
                    textColor = Color(0xFFFF7351),
                    onClick = { onRemoveFromPlaylist(); onDismiss() }
                )
            }

            if (onRemoveFromHistory != null) {
                TrackActionItem(
                    icon = Icons.Rounded.DeleteOutline,
                    label = "Xóa khỏi lịch sử nghe",
                    iconColor = Color(0xFFFF7351),
                    textColor = Color(0xFFFF7351),
                    onClick = { onRemoveFromHistory(); onDismiss() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumActionSheet(
    album: com.example.music_base.data.model.Album,
    onDismiss: () -> Unit,
    onShare: () -> Unit = {},
    onViewArtist: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131313),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = album.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        album.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        album.artist?.name ?: "Various Artists",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            TrackActionItem(
                icon = Icons.Rounded.Share,
                label = "Chia sẻ",
                onClick = { onShare(); onDismiss() }
            )
            
            TrackActionItem(
                icon = Icons.Rounded.Person,
                label = "Xem nghệ sĩ",
                onClick = { onViewArtist(); onDismiss() }
            )
            
            TrackActionItem(
                icon = Icons.Rounded.PlaylistAdd,
                label = "Thêm vào thư viện",
                onClick = { onDismiss() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistActionSheet(
    artist: com.example.music_base.data.model.Artist,
    onDismiss: () -> Unit,
    onShare: () -> Unit = {},
    isFollowed: Boolean = false,
    onToggleFollow: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131313),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        artist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        artist.uploaderId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            TrackActionItem(
                icon = Icons.Rounded.Share,
                label = "Chia sẻ",
                onClick = { onShare(); onDismiss() }
            )
            
            TrackActionItem(
                icon = if (isFollowed) Icons.Rounded.PersonRemove else Icons.Rounded.PersonAdd,
                label = if (isFollowed) "Bỏ theo dõi" else "Theo dõi",
                iconColor = if (isFollowed) Color(0xFF72FE8F) else Color.White.copy(alpha = 0.6f),
                onClick = { onToggleFollow(); onDismiss() }
            )
        }
    }
}

@Composable
private fun TrackActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    iconColor: Color = Color.White.copy(alpha = 0.6f),
    textColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
        Text(label, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TrackGridItem(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            track.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            track.artistName ?: "",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

