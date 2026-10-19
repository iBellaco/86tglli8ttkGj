package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.EnergyProfile
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvEnergySaverGreen
import com.example.ui.theme.TvFocusBorder
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TvTopBar(
    energyProfile: EnergyProfile,
    isNightMode: Boolean,
    isHiddenUnlocked: Boolean,
    onVoiceSearchClick: () -> Unit,
    onToggleNightMode: () -> Unit,
    onToggleHiddenClick: () -> Unit,
    onCloudSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEnergyProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Brand Logo + Clock & Date
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "TV",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column {
                Text(
                    text = currentTimeString.ifEmpty { "20:00" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = currentDateString.ifEmpty { "Lunes, 19 Oct" },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Weather Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Clima",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "23°C Despejado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Right side: Energy Badge + Remote Friendly Quick Action Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Energy Profile Chip
            TvHeaderActionButton(
                icon = Icons.Default.PowerSettingsNew,
                label = energyProfile.title,
                tint = if (energyProfile == EnergyProfile.ULTRA_SAVER) TvEnergySaverGreen else TvCyanAccent,
                onClick = onEnergyProfileClick,
                testTag = "energy_profile_button"
            )

            // Voice Search Button (DPad Focusable)
            TvHeaderIconButton(
                icon = Icons.Default.Mic,
                contentDesc = "Búsqueda por Voz",
                onClick = onVoiceSearchClick,
                highlight = true,
                testTag = "voice_search_top_button"
            )

            // Night Mode Toggle Button
            TvHeaderIconButton(
                icon = if (isNightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDesc = "Modo Oscuro Automático",
                onClick = onToggleNightMode,
                testTag = "night_mode_top_button"
            )

            // Hidden Apps Button
            TvHeaderIconButton(
                icon = if (isHiddenUnlocked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDesc = "Apps Ocultas",
                onClick = onToggleHiddenClick,
                testTag = "hidden_apps_top_button"
            )

            // Cloud Sync Button
            TvHeaderIconButton(
                icon = Icons.Default.CloudSync,
                contentDesc = "Sincronización en la Nube",
                onClick = onCloudSyncClick,
                testTag = "cloud_sync_top_button"
            )

            // Settings Button
            TvHeaderIconButton(
                icon = Icons.Default.Settings,
                contentDesc = "Ajustes de Nova TV",
                onClick = onSettingsClick,
                testTag = "settings_top_button"
            )
        }
    }
}

@Composable
fun TvHeaderIconButton(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit,
    highlight: Boolean = false,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> TvCyanAccent
            highlight -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        label = "btnBg"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.Black
            highlight -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .scale(if (isFocused) 1.15f else 1f)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else Color.Transparent,
                shape = CircleShape
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun TvHeaderActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isFocused) TvCyanAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else tint.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isFocused) Color.Black else tint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurface
        )
    }
}
