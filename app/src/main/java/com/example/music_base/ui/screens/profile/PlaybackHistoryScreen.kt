package com.example.music_base.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.rounded.Favorite

import coil.compose.AsyncImage
import com.example.music_base.data.model.*
import com.example.music_base.ui.viewmodel.MusicViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackHistoryScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onTrackClick: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    onAddToPlaylist: (Track) -> Unit = {},
    onShare: (Track) -> Unit = {}
) {

    val history by viewModel.filteredHistory.collectAsState()
    val rawHistory by viewModel.playbackHistory.collectAsState()
    val isLoading by viewModel.isHistoryLoading.collectAsState()

    val likedTrackIds by viewModel.likedTrackIds.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(true) { viewModel.loadPlaybackHistory() }

    // Reset refreshing when loading finishes
    LaunchedEffect(isLoading) {
        if (!isLoading) isRefreshing = false
    }

    var showClearDialog by remember { mutableStateOf(false) }
    var trackForMenu by remember { mutableStateOf<Track?>(null) }
    var showDailyStats by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text(
                    "Listening History",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (history.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Rounded.DeleteSweep,
                        "Clear history",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // Search Bar
        val searchQuery by viewModel.historySearchQuery.collectAsState()
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onHistorySearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search in history...", color = Color.White.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { viewModel.onHistorySearchQueryChanged("") }) { Icon(Icons.Default.Close, null, tint = Color.White) } }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                focusedIndicatorColor = Color(0xFF72FE8F),
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF72FE8F)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )


        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadPlaybackHistory()
            },
            modifier = Modifier.fillMaxSize()
        ) {
        if (isLoading && !isRefreshing) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎵", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No listening history yet",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Start playing music to build your history!",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // All content in one LazyColumn to avoid Box overlap
            val totalSeconds = rawHistory.sumOf { group -> group.items.sumOf { it.listenedSeconds } }
            val totalHours = totalSeconds / 3600
            val totalMinutes = (totalSeconds % 3600) / 60
            val totalSecs = totalSeconds % 60
            val timeLabel = when {
                totalHours > 0 -> "${totalHours}h ${totalMinutes}m"
                totalMinutes > 0 -> "${totalMinutes}m ${totalSecs}s"
                else -> "${totalSecs}s"
            }
            val totalTracks = rawHistory.sumOf { it.items.size }
            val completedTracks = rawHistory.sumOf { group -> group.items.count { it.completed } }


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Stats summary cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDailyStats = true },
                            color = Color(0xFF1A1A1A),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎧", fontSize = 22.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    timeLabel,
                                    color = Color(0xFF72FE8F),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "Total listened",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    "Tap For Details",
                                    color = Color(0xFF72FE8F).copy(alpha = 0.5f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF1A1A1A),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("✅", fontSize = 22.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "$completedTracks/$totalTracks",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "Completed",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Track list grouped by date
                history.forEach { group ->
                    item {
                        DateGroupHeader(group)
                    }
                    items(group.items) { item ->
                        PlaybackHistoryRow(
                            item = item,
                            onClick = { onTrackClick(item.trackId, item.trackName, item.trackThumb, item.listenedSeconds) },
                            onMoreClick = {
                                item.track?.let { trackForMenu = it }
                            }
                        )
                    }
                }
            }
        }
        } // end PullToRefreshBox

        if (trackForMenu != null) {
            com.example.music_base.ui.components.TrackActionSheet(
                track = trackForMenu!!,
                onDismiss = { trackForMenu = null },
                onToggleLike = { viewModel.toggleTrackLike(trackForMenu!!.id) },
                onRemoveFromHistory = {
                    viewModel.removeTrackFromHistory(trackForMenu!!.id)
                },
                isLiked = likedTrackIds.contains(trackForMenu!!.id),
                onAddToPlaylist = { onAddToPlaylist(trackForMenu!!) },
                onShare = { onShare(trackForMenu!!) }
            )

        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History", color = Color.White) },
            text = { Text("Remove all listening history?", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) {
                    Text("Clear", color = Color(0xFFFF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    // Daily stats bottom sheet
    if (showDailyStats) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDailyStats = false },
            sheetState = sheetState,
            containerColor = Color(0xFF141414),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }
        ) {
            val insights by viewModel.listeningInsights.collectAsState()
            val topArtists by viewModel.topArtistsFromHistory.collectAsState()
            val rawHistory by viewModel.playbackHistory.collectAsState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Insights row
                item {
                    if (insights != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔥 Streak", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("${insights!!.streak} Days", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)).align(Alignment.CenterVertically))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⏰ Active at", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text(insights!!.preferredTimeOfDay, color = Color(0xFF72FE8F), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Top Artists Insights
                if (topArtists.isNotEmpty()) {
                    item {
                        Text(
                            "🌟 Your Top Artists",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    items(topArtists) { info ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = info.artist.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262626)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                info.artist.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${info.playCount} plays",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Listening Daily List
                item {
                    Text(
                        "📅 Listening by Day",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                items(rawHistory) { group ->
                    val daySecs = group.items.sumOf { it.listenedSeconds }
                    val dayH = daySecs / 3600
                    val dayM = (daySecs % 3600) / 60
                    val dayS = daySecs % 60
                    val dayLabel = when {
                        dayH > 0 -> "${dayH}h ${dayM}m"
                        dayM > 0 -> "${dayM}m ${dayS}s"
                        else -> "${dayS}s"
                    }
                    val dayTracks = group.items.size

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                formatHistoryDate(group.date),
                                color = Color(0xFF72FE8F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$dayTracks track${if (dayTracks != 1) "s" else ""}",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            dayLabel,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                }
            }
        }
    }
}

@Composable
private fun DateGroupHeader(group: PlaybackDateGroup) {
    Text(
        text = formatHistoryDate(group.date),
        color = Color(0xFF72FE8F),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun PlaybackHistoryRow(
    item: PlaybackHistoryItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    val progress = if (item.trackDuration > 0) item.listenedSeconds.toFloat() / item.trackDuration else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Album art
        AsyncImage(
            model = item.trackThumb ?: item.track?.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF262626)),
            contentScale = ContentScale.Crop
        )

        // Track info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.trackName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                item.track?.artistName ?: "",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            // Progress bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (item.completed) Color(0xFF72FE8F) else Color.White.copy(alpha = 0.5f),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }

        // Time listened
        Text(
            "${item.listenedSeconds / 60}:${"%02d".format(item.listenedSeconds % 60)}",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp
        )

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WeeklyActivityChart(stats: List<DailyStat>) {
    val maxSecs = stats.maxOfOrNull { it.seconds }?.coerceAtLeast(1) ?: 1
    
    // Last 7 days, chrono order
    val displayStats = stats.take(7).reversed()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            displayStats.forEach { stat ->
                val heightPercent = (stat.seconds.toFloat() / maxSecs).coerceIn(0.08f, 1f)
                val dayLabel = try {
                    val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    val dateObj = sdfInput.parse(stat.date)
                    java.text.SimpleDateFormat("EEE", java.util.Locale.US).format(dateObj!!).first().toString()
                } catch (_: Exception) { 
                    stat.date.split("-").last()
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Time label on top of bar
                    if (stat.seconds > 0) {
                        Text(
                            text = if (stat.seconds >= 3600) "${stat.seconds / 3600}h" else "${stat.seconds / 60}m",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    
                    // The Bar
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .fillMaxHeight(heightPercent)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF72FE8F),
                                        Color(0xFF72FE8F).copy(alpha = 0.6f),
                                        Color(0xFF72FE8F).copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )
                    
                    Spacer(Modifier.height(10.dp))
                    
                    // Day initial
                    Text(
                        dayLabel,
                        color = if (heightPercent > 0.1f) Color.White else Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private fun formatHistoryDate(date: String): String {
    return try {
        val parts = date.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else date
    } catch (_: Exception) { date }
}
