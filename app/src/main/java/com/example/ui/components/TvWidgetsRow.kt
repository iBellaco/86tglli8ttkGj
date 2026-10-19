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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SystemStats
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvEnergySaverGreen
import com.example.ui.theme.TvFocusBorder

@Composable
fun TvWidgetsRow(
    systemStats: SystemStats?,
    onOptimizeRamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Widget 1: System Hardware Performance & Energy Efficiency Monitor (Flex 1)
        HardwareEfficiencyWidget(
            systemStats = systemStats,
            onOptimizeClick = onOptimizeRamClick,
            modifier = Modifier.weight(1.3f)
        )

        // Widget 2: Ambient Media / Quick Inputs Widget (Flex 1)
        QuickShortcutsWidget(
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HardwareEfficiencyWidget(
    systemStats: SystemStats?,
    onOptimizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val usedRam = systemStats?.usedRamMb ?: 1850L
    val totalRam = systemStats?.totalRamMb ?: 4096L
    val ramFraction = (usedRam.toFloat() / totalRam.toFloat()).coerceIn(0f, 1f)
    val freeStorage = systemStats?.freeStorageGb ?: 22L
    val totalStorage = systemStats?.totalStorageGb ?: 32L

    Surface(
        modifier = modifier
            .height(130.dp)
            .scale(if (isFocused) 1.02f else 1f)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp)
            )
            .focusable(interactionSource = interactionSource),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Monitor de Rendimiento",
                        tint = TvCyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Rendimiento y Eficiencia Energética",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Compact RAM Boost Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TvCyanAccent.copy(alpha = 0.2f))
                        .clickable(onClick = onOptimizeClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("boost_ram_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Liberar RAM",
                        tint = TvCyanAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Optimizar RAM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TvCyanAccent
                    )
                }
            }

            // Gauges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // RAM Gauge
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RAM: ${usedRam}MB / ${totalRam}MB",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(ramFraction * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ramFraction > 0.8f) Color(0xFFEF4444) else TvEnergySaverGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ramFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (ramFraction > 0.8f) Color(0xFFEF4444) else TvCyanAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Storage and Network Pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Almacenamiento",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${freeStorage}GB libres",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Red / Wi-Fi",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = systemStats?.networkType ?: "Wi-Fi (5GHz)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TvEnergySaverGreen
                        )
                    }
                }
            }

            // Bottom Thermal / Battery Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Temperatura CPU",
                        tint = TvEnergySaverGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Temperatura SoC: ${systemStats?.cpuTemperatureEstimate ?: "41°C Normal"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Arquitectura optimizada para bajo consumo",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun QuickShortcutsWidget(
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        modifier = modifier
            .height(130.dp)
            .scale(if (isFocused) 1.02f else 1f)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp)
            )
            .focusable(interactionSource = interactionSource),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Entradas y Controles",
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Acciones y Entradas TV",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShortcutTile(
                    label = "HDMI 1 (Consola)",
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
                ShortcutTile(
                    label = "HDMI 2 (Deco)",
                    color = Color(0xFF0EA5E9),
                    modifier = Modifier.weight(1f)
                )
                ShortcutTile(
                    label = "Mando / BT",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Presiona 'Menú' en el mando sobre cualquier app para opciones",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ShortcutTile(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) TvCyanAccent else color.copy(alpha = 0.2f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = {})
            .focusable(interactionSource = interactionSource)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
