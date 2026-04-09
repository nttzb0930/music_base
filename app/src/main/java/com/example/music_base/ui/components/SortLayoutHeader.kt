package com.example.music_base.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SortLayoutHeader(
    modifier: Modifier = Modifier,
    currentSortName: String,
    sortOptions: List<String>,
    onSortSelected: (String) -> Unit,
    isGrid: Boolean,
    onToggleGrid: () -> Unit,
    showLayoutToggle: Boolean = true
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Sort & Label
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showSortMenu = true }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sort,
                    contentDescription = "Sort",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = currentSortName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                containerColor = Color(0xFF282828),
                shape = RoundedCornerShape(12.dp)
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                option, 
                                color = if (option == currentSortName) Color(0xFF72FE8F) else Color.White,
                                fontWeight = if (option == currentSortName) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        onClick = {
                            onSortSelected(option)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // Right side: Layout Toggle
        if (showLayoutToggle) {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp),
                onClick = onToggleGrid
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isGrid) Icons.Rounded.List else Icons.Rounded.Dashboard,
                        contentDescription = "Change Layout",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
