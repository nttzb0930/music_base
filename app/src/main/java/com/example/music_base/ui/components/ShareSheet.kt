package com.example.music_base.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.music_base.data.model.Track
import com.example.music_base.ui.theme.*


private val ShareColorOptions = listOf(
    ShareMint,
    ShareGreyGreen,
    com.example.music_base.ui.theme.Primary, // Using the main theme green as Spotify Green alternative
    ShareDeepPurple,
    ShareRoyalBlue,
    ShareOrange,
    ShareBlack
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    track: Track,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val shareUrl = "https://www.youtube.com/watch?v=${track.youtubeVideoId}"
    
    var selectedColor by remember { mutableStateOf(ShareColorOptions[0]) }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Preview Section
            Box(
                modifier = Modifier
                    .padding(24.dp)
                    .width(260.dp)
                    .aspectRatio(0.65f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(selectedColor) 
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2624).copy(alpha = 0.9f)) // Dark inner card
                ) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artistName ?: "Unknown",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        
                        // Logo/Branding
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.MusicNote,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Sonic Gallery",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Color Selectors (7 aesthetic colors)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                ShareColorOptions.forEach { color ->

                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }



            // Bottom Actions Strip
            Divider(color = Color.White.copy(alpha = 0.1f))
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SimpleActionItem(
                        icon = Icons.Rounded.Link,
                        label = "Sao chép\nđường liên...",
                        onClick = {
                            copyToClipboard(context, shareUrl)
                            Toast.makeText(context, "Đã sao chép liên kết", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item {
                    SimpleActionItem(
                        icon = Icons.Rounded.Chat,
                        label = "Messenger",
                        iconBg = Brush.linearGradient(listOf(Color(0xFF00B2FF), Color(0xFF006AFF), Color(0xFF9100FF))),
                        onClick = { /* Launch Messenger */ }
                    )
                }
                item {
                    SimpleActionItem(
                        icon = Icons.Rounded.Facebook,
                        label = "Tin",
                        iconBg = Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF0C63D4))),
                        onClick = { /* Launch FB Stories */ }
                    )
                }
                item {
                    SimpleActionItem(
                        icon = Icons.Rounded.MusicNote,
                        label = "TikTok",
                        iconBg = Brush.linearGradient(listOf(Color.Black, Color(0xFF222222))),
                        onClick = { /* Launch TikTok */ }
                    )
                }
                item {
                    SimpleActionItem(
                        icon = Icons.Rounded.Sms,
                        label = "Tin nhắn",
                        iconBg = Brush.linearGradient(listOf(Color(0xFF34C759), Color(0xFF2DB04D))),
                        onClick = { /* Launch SMS */ }
                    )
                }
                item {
                    SimpleActionItem(
                        icon = Icons.Rounded.MoreHoriz,
                        label = "Thêm",
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Listen to ${track.title} on Sonic Gallery: $shareUrl")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleActionItem(
    icon: ImageVector,
    label: String,
    iconBg: Brush = Brush.linearGradient(listOf(Color(0xFF282828), Color(0xFF282828))),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Music Link", text)
    clipboard.setPrimaryClip(clip)
}
