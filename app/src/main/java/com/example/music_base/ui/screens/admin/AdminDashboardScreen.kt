package com.example.music_base.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.music_base.data.model.*
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary
import com.example.music_base.ui.theme.Secondary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import okhttp3.MultipartBody

@Composable
fun AdminDashboardScreen(
    overview: StashOverview?,
    recent: StashRecent?,
    topTracks: StashTopTracks?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onTrackClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    var selectedRecentTab by remember { mutableStateOf(0) }

    val tabs = listOf("Tracks", "Artists", "Albums", "Users", "Playlists")

    LaunchedEffect(Unit) {
        if (overview == null) {
            onRefresh()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = Dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
            contentPadding = PaddingValues(top = Dimens.paddingLarge, bottom = 120.dp)
        ) {
            item {
                HeaderSection(isLoading, onRefresh)
            }

            // --- Statistics Panorama ---
            item {
                if (overview != null) {
                    StatisticsPanorama(overview)
                } else if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }

            // --- Top Tracks Leaderboard ---
            item {
                ContentSection(title = "Top Performing Tracks") {
                    if (topTracks != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)) {
                            topTracks.data.take(5).forEachIndexed { index, track ->
                                TopTrackItem(index + 1, track, onTrackClick)
                            }
                        }
                    } else if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
                    }
                }
            }

            // --- Real-time Activity Feed ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Real-time Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingNormal))
                    
                    ScrollableTabRow(
                        selectedTabIndex = selectedRecentTab,
                        containerColor = Color.Transparent,
                        contentColor = Primary,
                        divider = {},
                        edgePadding = 0.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedRecentTab]),
                                color = Primary
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedRecentTab == index,
                                onClick = { selectedRecentTab = index },
                                text = { Text(title, fontSize = 14.sp) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.paddingLarge))
                    
                    RecentContentList(
                        tabIndex = selectedRecentTab,
                        recent = recent,
                        onTrackClick = onTrackClick,
                        onArtistClick = onArtistClick,
                        onUserClick = onUserClick
                    )
                }
            }

            // --- Network Infrastructure ---
            item {
                ContentSection(title = "Network Infrastructure") {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)) {
                        ActivityItem("Primary API Gateway", "Stable - 42ms", Icons.Default.CloudDone)
                        ActivityItem("Edge CDN Node", "Optimized", Icons.Default.Router)
                        ActivityItem("Auth Engine", "Active Session Layer", Icons.Default.Security)
                    }
                }
            }
        }

        if (isLoading && overview == null) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

@Composable
private fun HeaderSection(isLoading: Boolean, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Console Central",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Intelligence & Analytics Hub",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.radiusSmall))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
private fun StatisticsPanorama(overview: StashOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Users",
                value = overview.totalUsers.toString(),
                subValue = "+${overview.newUsersLast7Days} this week",
                icon = Icons.Default.Groups,
                gradient = listOf(Primary.copy(alpha = 0.15f), Color.Transparent)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Live Tracks",
                value = overview.totalTracks.toString(),
                subValue = "${overview.totalTracksWithAudio} with audio",
                icon = Icons.Default.GraphicEq,
                gradient = listOf(Secondary.copy(alpha = 0.15f), Color.Transparent)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Artists",
                value = overview.totalArtists.toString(),
                subValue = "+${overview.newArtistsLast7Days} new",
                icon = Icons.Default.Person,
                gradient = listOf(Color.Cyan.copy(alpha = 0.1f), Color.Transparent)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Engagement",
                value = "${(overview.totalPlaybackHistory / 1000f).coerceAtLeast(0.1f)}K",
                subValue = "Play events",
                icon = Icons.Default.Insights,
                gradient = listOf(Color.Magenta.copy(alpha = 0.1f), Color.Transparent)
            )
        }

        // Mini detail row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
        ) {
            MiniStatCard(Modifier.weight(1f), "Albums", overview.totalAlbums.toString(), Icons.Default.Album)
            MiniStatCard(Modifier.weight(1f), "Playlists", overview.totalPlaylists.toString(), Icons.Default.QueueMusic)
            MiniStatCard(Modifier.weight(1f), "Likes", overview.totalTrackLikes.toString(), Icons.Default.Favorite)
        }
    }
}

@Composable
fun DashboardTrackItem(
    track: StashTrackItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(Color.White.copy(alpha = 0.03f))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(Dimens.radiusMedium))
            .clickable { onClick() }
            .padding(Dimens.paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://picsum.photos/200/200?random=${track.id}",
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(Dimens.paddingNormal))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(track.artistName, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyTracksPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(Dimens.radiusLarge)),
        contentAlignment = Alignment.Center
    ) {
        Text("No tracks available for command", color = Color.White.copy(alpha = 0.2f), fontSize = 14.sp)
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    gradient: List<Color>
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusLarge))
            .background(Brush.verticalGradient(gradient))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(Dimens.radiusLarge))
            .padding(Dimens.paddingLarge)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
            Spacer(Modifier.height(Dimens.paddingSmall))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(text = subValue, fontSize = 10.sp, color = Primary.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MiniStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(Color.White.copy(alpha = 0.04f))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(Dimens.radiusMedium))
            .padding(Dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(title, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        }
    }
}

@Composable
fun TopTrackItem(rank: Int, track: StashTrackItem, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(track.id) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.titleSmall,
            color = if (rank <= 3) Primary else Color.White.copy(alpha = 0.3f),
            fontWeight = FontWeight.Bold
        )
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artistName, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${track.viewCount ?: 0}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("VIEWS", color = Primary.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun RecentContentList(
    tabIndex: Int,
    recent: StashRecent?,
    onTrackClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    if (recent == null) {
        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Initializing feeds...", color = Color.White.copy(alpha = 0.3f))
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (tabIndex) {
            0 -> recent.tracks.forEach { track ->
                DashboardTrackItem(
                    track = track,
                    onClick = { onTrackClick(track.id) }
                )
            }
            1 -> recent.artists.forEach { artist ->
                RecentItemRow(artist.name, "@" + artist.youtubeChannelId, artist.createdAt, Icons.Default.Person) { onArtistClick(artist.id) }
            }
            2 -> recent.albums.forEach { album ->
                RecentItemRow(album.title, album.artistName, album.createdAt, Icons.Default.Album) { }
            }
            3 -> recent.users.forEach { user ->
                RecentItemRow(user.username, user.email, user.createdAt, Icons.Default.AccountCircle) { onUserClick(user.id) }
            }
            4 -> recent.playlists.forEach { playlist ->
                RecentItemRow(playlist.name, if (playlist.isPublic) "Public" else "Private", playlist.createdAt, Icons.Default.PlaylistPlay) { }
            }
        }
    }
}

@Composable
fun RecentItemRow(title: String, subtitle: String, time: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(Dimens.paddingNormal))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
            Text(subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, maxLines = 1)
        }
        Text(
            text = time.substringBefore("T").replace("-", "/"), // Simple format
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ContentSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = Dimens.paddingSmall)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.radiusLarge))
                .background(Color.White.copy(alpha = 0.03f))
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(Dimens.radiusLarge))
                .padding(Dimens.paddingNormal)
        ) {
            content()
        }
    }
}

@Composable
fun ActivityItem(text: String, time: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(Dimens.radiusSmall)).background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(Dimens.paddingNormal))
        Column(modifier = Modifier.weight(1f)) {
            Text(text, fontSize = 14.sp, color = Color.White, maxLines = 1)
            Text(time, fontSize = 11.sp, color = Color.White.copy(alpha = 0.3f))
        }
    }
}
