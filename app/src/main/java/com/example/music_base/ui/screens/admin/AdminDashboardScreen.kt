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
import com.example.music_base.data.model.Track
import com.example.music_base.ui.theme.Dimens
import com.example.music_base.ui.theme.Primary
import com.example.music_base.ui.theme.Secondary

@Composable
fun AdminDashboardScreen(
    recentTracks: List<Track> = emptyList(),
    totalTrackCount: Int = 0,
    onEditTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.paddingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
        contentPadding = PaddingValues(top = Dimens.paddingLarge, bottom = 120.dp)
    ) {
        item {
            HeaderSection()
        }

        // --- Statistics Panorama ---
        item {
            StatisticsPanorama(totalTracks = totalTrackCount)
        }

        // --- System Health Summary ---
        item {
            ContentSection(title = "Service Integrity") {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)) {
                    ActivityItem("Railway API Engine", "Operational", Icons.Default.CloudDone)
                    ActivityItem("Cloudinary Storage", "Secure Connection", Icons.Default.Dns)
                }
            }
        }

        // --- Track Commander ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${recentTracks.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
        
        if (recentTracks.isEmpty()) {
            item {
                EmptyTracksPlaceholder()
            }
        } else {
            items(recentTracks.take(15)) { track -> 
                TrackManagementItem(
                    track = track,
                    onEdit = { onEditTrack(track) },
                    onDelete = { onDeleteTrack(track) }
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Text(
            text = "Console Central",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Professional Management Engine",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StatisticsPanorama(totalTracks: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Active Users",
            value = "1.2K",
            icon = Icons.Default.Groups,
            gradient = listOf(Primary.copy(alpha = 0.2f), Color.Transparent)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Total Tracks",
            value = totalTracks.toString(),
            icon = Icons.Default.GraphicEq,
            gradient = listOf(Secondary.copy(alpha = 0.2f), Color.Transparent)
        )
    }
}

@Composable
fun TrackManagementItem(
    track: Track,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMedium))
            .background(Color.White.copy(alpha = 0.03f))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(Dimens.radiusMedium))
            .padding(Dimens.paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(Dimens.paddingNormal))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(track.artistName ?: "Unknown", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Row {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
        }
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
    icon: ImageVector,
    gradient: List<Color>
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusLarge))
            .background(Brush.verticalGradient(gradient))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(Dimens.radiusLarge))
            .padding(Dimens.paddingNormal)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(Dimens.paddingSmall))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
        }
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
