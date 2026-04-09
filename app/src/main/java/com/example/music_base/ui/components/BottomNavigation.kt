package com.example.music_base.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.music_base.ui.theme.Dimens

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    Home("home", Icons.Outlined.Home, "Home"),
    Library("library", Icons.Outlined.LibraryMusic, "Library"),
    Profile("profile", Icons.Outlined.Person, "Profile"),
    Search("search", Icons.Outlined.Search, "Search")
}

data class TabBounds(val left: Float = 0f, val width: Float = 0f)

@Composable
fun MusicBottomNavigation(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var boxBoundsMap by remember { mutableStateOf(mapOf<BottomNavItem, TabBounds>()) }
    var dragOffset by remember { mutableStateOf<Float?>(null) }
    var rowCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Nav Glass Background
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(Dimens.radiusPill),
                color = Color.Black.copy(alpha = 0.15f),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.12f)
                ),
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(boxBoundsMap, selectedItem) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val currentBox = boxBoundsMap[selectedItem]
                                    if (currentBox != null) {
                                        val localX = offset.x - 12.dp.toPx()
                                        if (localX >= currentBox.left - 40f && localX <= currentBox.left + currentBox.width + 40f) {
                                            val currentLeft = currentBox.left + with(density) { 10.dp.toPx() }
                                            dragOffset = currentLeft
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val currentDragX = dragOffset
                                    if (currentDragX != null) {
                                        val nearest = boxBoundsMap.entries.minByOrNull { Math.abs(it.value.left + it.value.width / 2f - (currentDragX + 50f)) }
                                        if (nearest != null && nearest.key != selectedItem) {
                                            onItemSelected(nearest.key)
                                        }
                                    }
                                    dragOffset = null
                                },
                                onDragCancel = {
                                    dragOffset = null
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val currentDragX = dragOffset
                                    if (currentDragX != null) {
                                        val minDrag = boxBoundsMap.values.minOfOrNull { it.left } ?: 0f
                                        val maxDrag = boxBoundsMap.values.maxOfOrNull { it.left + it.width } ?: 1000f
                                        dragOffset = (currentDragX + dragAmount).coerceIn(minDrag, maxDrag)
                                    }
                                }
                            )
                        }
                ) {
                    val paddingPx = with(density) { 10.dp.toPx() }
                    val selectedBox = boxBoundsMap[selectedItem]
                    
                    val currentBoxTargetWidth = (selectedBox?.width ?: 0f) - paddingPx * 2
                    val targetLeft = dragOffset ?: ((selectedBox?.left ?: 0f) + paddingPx)
                    val targetRight = if (dragOffset != null) {
                        dragOffset!! + currentBoxTargetWidth
                    } else {
                        (selectedBox?.left ?: 0f) + (selectedBox?.width ?: 0f) - paddingPx
                    }

                    val spec = if (dragOffset != null) {
                        spring<Float>(stiffness = 1500f)
                    } else {
                        spring<Float>(dampingRatio = 0.55f, stiffness = 50f) 
                    }

                    val animatedLeft by animateFloatAsState(targetValue = targetLeft, animationSpec = spec, label = "left")
                    val animatedRight by animateFloatAsState(targetValue = targetRight, animationSpec = spec, label = "right")

                    // Visual selection độc lập, không bị vòng lặp
                    val visuallySelectedItem = if (dragOffset != null && boxBoundsMap.isNotEmpty()) {
                        boxBoundsMap.entries.minByOrNull { Math.abs(it.value.left + it.value.width / 2f - (dragOffset!! + currentBoxTargetWidth / 2f)) }?.key ?: selectedItem
                    } else {
                        selectedItem
                    }

                    // Liquid Indicator Layer
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        if (boxBoundsMap.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .height(52.dp)
                                    .offset(x = with(LocalDensity.current) { animatedLeft.toDp() })
                                    .width(with(LocalDensity.current) { (animatedRight - animatedLeft).coerceAtLeast(0f).toDp() })
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(Dimens.radiusPill))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(Dimens.radiusPill))
                            )
                        }
                    }

                    // Tabs Layer
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                            .onGloballyPositioned { rowCoords = it },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mainItems = listOf(BottomNavItem.Home, BottomNavItem.Library, BottomNavItem.Profile)
                        mainItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .onGloballyPositioned { boxCoords ->
                                        rowCoords?.let { rowC ->
                                            if (boxCoords.isAttached && rowC.isAttached) {
                                                val bounds = rowC.localBoundingBoxOf(boxCoords, clipBounds = false)
                                                val currentBounds = boxBoundsMap[item]
                                                if (currentBounds == null || Math.abs(currentBounds.left - bounds.left) > 1f) {
                                                    boxBoundsMap = boxBoundsMap + (item to TabBounds(bounds.left, bounds.width))
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                BottomNavTab(
                                    item = item,
                                    selected = visuallySelectedItem == item,
                                    onClick = { onItemSelected(item) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Separate Search Button
            Surface(
                onClick = { onItemSelected(BottomNavItem.Search) },
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(50),
                color = if (selectedItem == BottomNavItem.Search) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.15f),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.12f)
                ),
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = BottomNavItem.Search.icon,
                        contentDescription = "Search",
                        tint = if (selectedItem == BottomNavItem.Search) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.45f),
        animationSpec = tween(500),
        label = "tabContent"
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .clickable(onClick = onClick)
            .animateContentSize( // Tự động animate kích thước khi chữ hiện ra
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = 50f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = if (selected) 4.dp else 0.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            
            if (selected) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .alpha(1f)
                )
            }
        }
    }
}
