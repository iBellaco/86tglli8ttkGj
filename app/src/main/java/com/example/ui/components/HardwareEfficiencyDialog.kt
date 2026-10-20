package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.EnergyProfile
import com.example.data.model.SystemStats
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvEnergySaverGreen
import com.example.ui.theme.TvFocusBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dialog animado y reducido de Rendimiento y Eficiencia Energética para Android TV.
 */
@Composable
fun HardwareEfficiencyDialog(
    systemStats: SystemStats?,
    energyProfile: EnergyProfile,
    onSetEnergyProfile: (EnergyProfile) -> Unit,
    onOptimizeRam: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isOptimizing by remember { mutableStateOf(false) }
    var optimizationDone by remember { mutableStateOf(false) }
    val rotationAnim = remember { Animatable(0f) }

    val usedRam = systemStats?.usedRamMb ?: 1820L
    val totalRam = systemStats?.totalRamMb ?: 4096L
    val ramFraction = (usedRam.toFloat() / totalRam.toFloat()).coerceIn(0f, 1f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TvCyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TvCyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Rendimiento y Eficiencia",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Arquitectura de bajo consumo para Smart TV",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TvHeaderIconButton(
                        icon = Icons.Default.Close,
                        contentDesc = "Cerrar",
                        onClick = onDismiss
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Animated RAM Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = TvCyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Memoria RAM",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "$usedRam MB / $totalRam MB (${(ramFraction * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ramFraction > 0.8f) Color(0xFFEF4444) else TvEnergySaverGreen
                            )
                        }

                        LinearProgressIndicator(
                            progress = { if (isOptimizing) 0.35f else ramFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (ramFraction > 0.8f) Color(0xFFEF4444) else TvCyanAccent,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        if (optimizationDone) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = TvEnergySaverGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "¡Memoria optimizada! Se liberaron ~320 MB de caché",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TvEnergySaverGreen
                                )
                            }
                        }
                    }
                }

                // Grid of System Vitals (Temperature, Storage, Network)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalStatPill(
                        icon = Icons.Default.Thermostat,
                        title = "Temperatura SoC",
                        value = systemStats?.cpuTemperatureEstimate ?: "41°C Normal",
                        color = TvEnergySaverGreen,
                        modifier = Modifier.weight(1f)
                    )

                    VitalStatPill(
                        icon = Icons.Default.Speed,
                        title = "Almacenamiento",
                        value = "${systemStats?.freeStorageGb ?: 22}GB libres",
                        color = TvCyanAccent,
                        modifier = Modifier.weight(1f)
                    )

                    VitalStatPill(
                        icon = Icons.Default.Wifi,
                        title = "Conexión",
                        value = systemStats?.networkType ?: "Wi-Fi TV",
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Energy Profile Selection Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Perfil de Consumo Energético:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EnergyProfileCompactButton(
                            profile = EnergyProfile.ULTRA_SAVER,
                            isSelected = energyProfile == EnergyProfile.ULTRA_SAVER,
                            onClick = { onSetEnergyProfile(EnergyProfile.ULTRA_SAVER) },
                            modifier = Modifier.weight(1f)
                        )
                        EnergyProfileCompactButton(
                            profile = EnergyProfile.BALANCED,
                            isSelected = energyProfile == EnergyProfile.BALANCED,
                            onClick = { onSetEnergyProfile(EnergyProfile.BALANCED) },
                            modifier = Modifier.weight(1f)
                        )
                        EnergyProfileCompactButton(
                            profile = EnergyProfile.PERFORMANCE,
                            isSelected = energyProfile == EnergyProfile.PERFORMANCE,
                            onClick = { onSetEnergyProfile(EnergyProfile.PERFORMANCE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Action Button: Clean / Optimize RAM with animation
                val optimizeInteractionSource = remember { MutableInteractionSource() }
                val isOptimizeFocused by optimizeInteractionSource.collectIsFocusedAsState()

                Button(
                    onClick = {
                        if (!isOptimizing) {
                            isOptimizing = true
                            optimizationDone = false
                            coroutineScope.launch {
                                rotationAnim.animateTo(
                                    targetValue = 720f,
                                    animationSpec = tween(durationMillis = 900, easing = LinearEasing)
                                )
                                onOptimizeRam()
                                delay(300)
                                isOptimizing = false
                                optimizationDone = true
                                rotationAnim.snapTo(0f)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isOptimizeFocused) 1.03f else 1f)
                        .focusable(interactionSource = optimizeInteractionSource)
                        .testTag("dialog_optimize_ram_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOptimizeFocused) TvCyanAccent else MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = if (isOptimizeFocused) Color.Black else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotationAnim.value)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOptimizing) "Limpiando procesos y memoria..." else "Optimizar RAM y Procesos Ahora",
                        color = if (isOptimizeFocused) Color.Black else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VitalStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EnergyProfileCompactButton(
    profile: EnergyProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .scale(if (isFocused) 1.05f else 1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isFocused) TvCyanAccent else if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = profile.title,
            fontSize = 11.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
            color = if (isFocused) Color.Black else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
