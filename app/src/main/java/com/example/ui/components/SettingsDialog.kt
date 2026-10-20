package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import com.example.data.model.WeatherState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EnergyProfile
import com.example.data.model.SystemStats
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvEnergySaverGreen
import com.example.ui.theme.TvFocusBorder

data class SettingsTabItemData(
    val title: String,
    val icon: ImageVector
)

@Composable
fun SettingsDialog(
    energyProfile: EnergyProfile,
    isAutoNightMode: Boolean,
    isNightModeActive: Boolean,
    cloudSyncMessage: String?,
    systemStats: SystemStats? = null,
    weather: WeatherState = WeatherState(),
    onCycleWeather: () -> Unit = {},
    onSetEnergyProfile: (EnergyProfile) -> Unit,
    onToggleAutoNightMode: () -> Unit,
    onToggleNightModeManual: () -> Unit,
    onOptimizeRam: () -> Unit,
    onSetDefaultLauncher: () -> Unit = {},
    onSimulateMedia: (String) -> Unit = {},
    onClearMedia: () -> Unit = {},
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedSection by remember { mutableStateOf(0) }
    var jsonInput by remember { mutableStateOf("") }
    var exportedJson by remember { mutableStateOf("") }

    val usedRam = systemStats?.usedRamMb ?: 1820L
    val totalRam = systemStats?.totalRamMb ?: 4096L
    val ramFraction = (usedRam.toFloat() / totalRam.toFloat()).coerceIn(0f, 1f)

    val tabs = remember {
        listOf(
            SettingsTabItemData("Rendimiento", Icons.Default.Speed),
            SettingsTabItemData("Launcher TV", Icons.Default.Home),
            SettingsTabItemData("Modo Noche", Icons.Default.DarkMode),
            SettingsTabItemData("Sincronización", Icons.Default.CloudSync),
            SettingsTabItemData("Estabilidad", Icons.Default.SystemUpdate)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tabs[selectedSection].icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Configuración de Nova TV Launcher",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tabs[selectedSection].title,
                                fontSize = 12.sp,
                                color = TvCyanAccent
                            )
                        }
                    }

                    TvHeaderIconButton(
                        icon = Icons.Default.Close,
                        contentDesc = "Cerrar",
                        onClick = onDismiss,
                        testTag = "settings_close_btn"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Tabs (Spacious, scrollable horizontal row)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(tabs) { index, tab ->
                        SettingsSectionTab(
                            title = tab.title,
                            icon = tab.icon,
                            isSelected = selectedSection == index,
                            onClick = { selectedSection = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Fully Scrollable Content Pane
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedSection) {
                        0 -> {
                            // Section 0: Rendimiento y Eficiencia Energética
                            Text(
                                text = "Panel de Rendimiento y Configuración Rápida",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvCyanAccent
                            )

                            // Quick Action: Establecer como Launcher Principal de Android TV
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TvCyanAccent.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, TvCyanAccent.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(TvCyanAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Establecer como Launcher Principal",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Abre Nova TV automáticamente al encender el televisor o pulsar Home",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = onSetDefaultLauncher,
                                        colors = ButtonDefaults.buttonColors(containerColor = TvCyanAccent),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_set_default_quick")
                                    ) {
                                        Text(
                                            text = "Configurar",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Weather & Alerts Test Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (weather.isRainAlertActive) Icons.Default.WaterDrop else Icons.Default.WbSunny,
                                                contentDescription = null,
                                                tint = if (weather.isRainAlertActive) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Pronóstico y Alertas de Clima",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${weather.currentTemp}°C • ${weather.condition}",
                                                    fontSize = 11.5.sp,
                                                    color = TvCyanAccent
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = onCycleWeather,
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("settings_cycle_weather_btn")
                                        ) {
                                            Text(
                                                text = "Cambiar Clima",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Hoy: Mín ${weather.todayMin}° / Máx ${weather.todayMax}° • Mañana: Mín ${weather.tomorrowMin}° / Máx ${weather.tomorrowMax}°",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (weather.isRainAlertActive) {
                                        Text(
                                            text = "🌧️ Alerta activa: Probabilidad de lluvia ${weather.rainProbabilityPercent}% (>49%) de ${weather.rainStartHour} a ${weather.rainEndHour}",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (weather.isHeatAlertActive) {
                                        Text(
                                            text = "🔥 Alerta activa: Calor extremo superior a 35°C (Actual ${weather.currentTemp}°C / Máx ${weather.todayMax}°C)",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFF87171),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // RAM & Hardware Vitals Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Memoria RAM",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$usedRam MB / $totalRam MB (${(ramFraction * 100).toInt()}%)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ramFraction > 0.8f) Color(0xFFEF4444) else TvEnergySaverGreen
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { ramFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (ramFraction > 0.8f) Color(0xFFEF4444) else TvCyanAccent,
                                        trackColor = MaterialTheme.colorScheme.surface
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Thermostat,
                                                contentDescription = null,
                                                tint = TvEnergySaverGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Temperatura SoC",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = systemStats?.cpuTemperatureEstimate ?: "41°C Normal",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TvEnergySaverGreen
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = TvCyanAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Almacenamiento",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${systemStats?.freeStorageGb ?: 22} GB libres",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = onOptimizeRam,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_optimize_ram")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Liberar Memoria RAM y Caché",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "Perfiles de Ahorro y Potencia:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            EnergyProfileItem(
                                profile = EnergyProfile.ULTRA_SAVER,
                                current = energyProfile,
                                onSelect = { onSetEnergyProfile(EnergyProfile.ULTRA_SAVER) }
                            )
                            EnergyProfileItem(
                                profile = EnergyProfile.BALANCED,
                                current = energyProfile,
                                onSelect = { onSetEnergyProfile(EnergyProfile.BALANCED) }
                            )
                            EnergyProfileItem(
                                profile = EnergyProfile.PERFORMANCE,
                                current = energyProfile,
                                onSelect = { onSetEnergyProfile(EnergyProfile.PERFORMANCE) }
                            )
                        }

                        1 -> {
                            // Section 1: Default Launcher
                            Text(
                                text = "Configurar Launcher Predeterminado",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvCyanAccent
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = TvCyanAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "Nova TV Launcher como Inicio del Sistema",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Abre automáticamente este launcher al encender la TV o pulsar el botón Home del mando.",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                softWrap = true
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = onSetDefaultLauncher,
                                        colors = ButtonDefaults.buttonColors(containerColor = TvCyanAccent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_set_default_launcher")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Seleccionar como Launcher por Defecto",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Text(
                                        text = "Nota: Al pulsar el botón, Android TV te mostrará el diálogo del sistema para elegir la app de inicio. Selecciona 'Nova TV Launcher' y luego elige 'Siempre'.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        softWrap = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Media & Playback History Box (Continuar Viendo)
                            Text(
                                text = "Historial Multimedia (Continuar Viendo)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvCyanAccent
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Carrusel de Último Video Reproducido",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Muestra el último contenido visto en YouTube, YouTube Kids, Netflix o Prime Video en la pantalla principal. Si no se ha reproducido ningún video, la sección no aparecerá en el inicio.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = true
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onSimulateMedia("YouTube")
                                                Toast.makeText(context, "Video de YouTube añadido a Continuar Viendo", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+ YouTube", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                onSimulateMedia("Netflix")
                                                Toast.makeText(context, "Episodio de Netflix añadido", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+ Netflix", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                onClearMedia()
                                                Toast.makeText(context, "Historial vaciado: la sección desaparece de la pantalla principal", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Vaciar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Section 2: Modo Noche
                            Text(
                                text = "Modo Oscuro Automático y Confort Visual",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvCyanAccent
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Activar Automáticamente según la Hora",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Atenúa la pantalla y activa el modo noche de 19:00 a 07:00 automáticamente.",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = true
                                    )
                                }
                                Switch(
                                    checked = isAutoNightMode,
                                    onCheckedChange = { onToggleAutoNightMode() }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Estado del Tema Visual",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isNightModeActive) "Modo Noche activo (Fondo Cine #090D16)" else "Modo Claro activo",
                                        fontSize = 11.5.sp,
                                        color = if (isNightModeActive) TvCyanAccent else MaterialTheme.colorScheme.outline
                                    )
                                }
                                Button(
                                    onClick = onToggleNightModeManual,
                                    modifier = Modifier.testTag("toggle_night_manual")
                                ) {
                                    Text(
                                        text = if (isNightModeActive) "Pasar a Claro" else "Pasar a Noche",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        3 -> {
                            // Section 3: Sincronización en la Nube
                            Text(
                                text = "Sincronización en la Nube y Respaldo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvCyanAccent
                            )
                            Text(
                                text = "Guarda o restaura tus pestañas personalizadas, orden de apps, accesos rápidos anclados y lista de apps ocultas.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = true
                            )

                            if (cloudSyncMessage != null) {
                                Text(
                                    text = cloudSyncMessage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TvEnergySaverGreen
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onExportBackup { json ->
                                            exportedJson = json
                                            clipboardManager.setText(AnnotatedString(json))
                                            Toast.makeText(context, "Configuración exportada al portapapeles", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_backup_button")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Exportar Respaldo", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        if (jsonInput.isNotBlank()) {
                                            onImportBackup(jsonInput)
                                        } else {
                                            Toast.makeText(context, "Pega el código JSON de respaldo", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("import_backup_button")
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Importar Respaldo", fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = jsonInput,
                                onValueChange = { jsonInput = it },
                                label = { Text("Pegar Respaldo JSON o enlace de nube", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.5.sp)
                            )

                            if (exportedJson.isNotBlank()) {
                                Text(
                                    text = "JSON Exportado recientemente (${exportedJson.length} caracteres)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        4 -> {
                            // Section 4: Despliegue Continuo y Estabilidad
                            Text(
                                text = "Despliegue Continuo y Estabilidad del Sistema",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvCyanAccent
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TvEnergySaverGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Nova TV Launcher v1.5 (Estable)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Canal de despliegue: ESTABLE • Verificación continua de procesos Android",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = true
                                    )
                                    Text(
                                        text = "Sin fugas de memoria detectadas • Consumo de CPU en reposo < 0.8%",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TvEnergySaverGreen,
                                        softWrap = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTab(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .scale(if (isFocused) 1.05f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) TvCyanAccent else if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isFocused) Color.Black else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                color = if (isFocused) Color.Black else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EnergyProfileItem(
    profile: EnergyProfile,
    current: EnergyProfile,
    onSelect: () -> Unit
) {
    val isSelected = profile == current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isFocused) 1.02f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) TvCyanAccent else if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onSelect)
            .focusable(interactionSource = interactionSource)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = profile.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${profile.targetFps} FPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFocused) Color.DarkGray else TvCyanAccent
                )
            }
            Text(
                text = profile.description,
                fontSize = 11.5.sp,
                color = if (isFocused) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant,
                softWrap = true
            )
        }
        if (isSelected) {
            Text(
                text = "✓ Activo",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) Color.Black else TvEnergySaverGreen,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
