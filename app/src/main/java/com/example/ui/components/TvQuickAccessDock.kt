package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.TvCyanAccent

@Composable
fun TvQuickAccessDock(
    dockApps: List<AppItem>,
    onLaunchApp: (AppItem) -> Unit,
    onOpenMenu: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // By user requirement: if no apps are pinned to quick access, do not show the dock panel
    if (dockApps.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Accesos Directos Anclados",
                tint = TvCyanAccent,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Accesos Directos Anclados",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "• ${dockApps.size} fijados",
                fontSize = 11.sp,
                color = TvCyanAccent
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dockApps, key = { "dock_${it.packageName}" }) { app ->
                TvAppCard(
                    app = app,
                    onLaunch = onLaunchApp,
                    onOpenMenu = onOpenMenu,
                    isDockItem = true
                )
            }
        }
    }
}
