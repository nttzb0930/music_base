package com.example.music_base.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track
import com.example.music_base.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun MiniPlayer(
    currentTrack: Track?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onDismiss: () -> Unit = {},       // swipe left (right-to-left)
    onNextTrack: () -> Unit = {},     // swipe right (left-to-right)
    modifier: Modifier = Modifier
) {
    if (currentTrack == null) return

    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(Dimens.miniPlayerHeight)
            .graphicsLayer { translationX = offsetX.value }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = size.width * 0.35f
                        when {
                            // Swipe RIGHT-TO-LEFT → next track
                            offsetX.value < -threshold -> {
                                scope.launch {
                                    offsetX.animateTo(-size.width.toFloat(), tween(200))
                                    onNextTrack()
                                    offsetX.snapTo(0f)
                                }
                            }
                            // Swipe LEFT-TO-RIGHT → dismiss
                            offsetX.value > threshold -> {
                                scope.launch {
                                    offsetX.animateTo(size.width.toFloat(), tween(200))
                                    onDismiss()
                                    offsetX.snapTo(0f)
                                }
                            }
                            // Not enough → snap back
                            else -> scope.launch { offsetX.animateTo(0f, tween(300)) }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    }
                )
            }
            .clickable { onPlayerClick() },
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        ),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.paddingMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingNormal)
            ) {
                // Album Art
                AsyncImage(
                    model = currentTrack.coverUrl,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(Dimens.miniPlayerArtSize)
                        .clip(RoundedCornerShape(Dimens.radiusSmall)),
                    contentScale = ContentScale.Crop
                )

                // Track Info + Progress Bar
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentTrack.artistName?.uppercase() ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (currentTrack.durationMs > 0) {
                        currentTrack.currentPosition.toFloat() / currentTrack.durationMs.toFloat()
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDevicesClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Devices",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
