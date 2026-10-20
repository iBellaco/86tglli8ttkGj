package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryTab
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder

@Composable
fun TvCategoryTabs(
    categories: List<CategoryTab>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    onAddNewTabClick: () -> Unit,
    onOpenTabManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Todas" Tab
        item {
            TvTabItem(
                id = "all",
                title = "Todas",
                icon = Icons.Default.Apps,
                isSelected = selectedTabId == "all",
                onClick = { onTabSelected("all") },
                testTag = "tab_all"
            )
        }

        // Dynamically fetched Categories
        items(categories, key = { it.id }) { cat ->
            val icon = resolveTabIcon(cat.iconName)
            TvTabItem(
                id = cat.id,
                title = cat.title,
                icon = icon,
                isSelected = selectedTabId == cat.id,
                onClick = { onTabSelected(cat.id) },
                testTag = "tab_${cat.id}"
            )
        }

        // "Ocultas" Tab
        item {
            TvTabItem(
                id = "hidden",
                title = "Ocultas",
                icon = Icons.Default.Lock,
                isSelected = selectedTabId == "hidden",
                onClick = { onTabSelected("hidden") },
                testTag = "tab_hidden"
            )
        }

        // Add Tab Button
        item {
            TvAddTabButton(onClick = onAddNewTabClick)
        }

        // Manage Tabs Button
        item {
            TvManageTabsButton(onClick = onOpenTabManager)
        }
    }
}

@Composable
fun TvManageTabsButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) TvCyanAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .border(
                width = 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("manage_tabs_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = "Gestionar Pestañas",
            tint = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Gestionar Pestañas",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TvTabItem(
    id: String,
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateDpAsState(
        targetValue = if (isFocused) 4.dp else 0.dp,
        label = "tabElev"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> TvCyanAccent
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        label = "tabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.Black
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "tabContent"
    )

    Row(
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun TvAddTabButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) TvCyanAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .border(
                width = 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("add_tab_button"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Nueva Pestaña",
            tint = if (isFocused) Color.Black else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Añadir Pestaña",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFocused) Color.Black else MaterialTheme.colorScheme.primary
        )
    }
}
