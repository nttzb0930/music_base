package com.example.music_base.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music_base.ui.theme.Dimens

enum class AdminBottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    Command("command", Icons.Default.Dashboard, "Command"),
    Ingest("ingest", Icons.Default.AddModerator, "Ingest"),
    Database("database", Icons.Default.Storage, "Database")
}

@Composable
fun AdminBottomNavigation(
    selectedItem: AdminBottomNavItem,
    onItemSelected: (AdminBottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            .background(Color.Transparent)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(Dimens.radiusPill),
            color = Color.Black.copy(alpha = 0.25f),
            border = BorderStroke(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.15f)
            ),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminBottomNavItem.values().forEach { item ->
                    AdminNavTab(
                        item = item,
                        selected = selectedItem == item,
                        onClick = { onItemSelected(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminNavTab(
    item: AdminBottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.45f),
        animationSpec = tween(500),
        label = "adminTabContent"
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .clickable(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 100f
                )
            )
            .then(
                if (selected) Modifier.background(Color.White.copy(alpha = 0.05f))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}
