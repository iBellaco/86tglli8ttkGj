package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppItem
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder

@Composable
fun NewTabDialog(
    allApps: List<AppItem> = emptyList(),
    onAddTab: (title: String, iconName: String, colorHex: String, assignedPackages: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var tabName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("apps") }
    var selectedColor by remember { mutableStateOf("#38BDF8") }
    val selectedPackages = remember { mutableStateListOf<String>() }

    val iconOptions = listOf(
        "movie" to Icons.Default.Movie,
        "sports_esports" to Icons.Default.SportsEsports,
        "music_note" to Icons.Default.MusicNote,
        "build" to Icons.Default.Build,
        "star" to Icons.Default.Star,
        "tv" to Icons.Default.Tv,
        "apps" to Icons.Default.Apps,
        "folder" to Icons.Default.Folder
    )

    val colorOptions = listOf(
        "#38BDF8", // Cyan
        "#F43F5E", // Coral
        "#A855F7", // Purple
        "#10B981", // Emerald
        "#FBBF24", // Amber
        "#6366F1"  // Indigo
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(540.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crear Nueva Pestaña / Categoría",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TvHeaderIconButton(
                        icon = Icons.Default.Close,
                        contentDesc = "Cerrar",
                        onClick = onDismiss
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                OutlinedTextField(
                    value = tabName,
                    onValueChange = { tabName = it },
                    label = { Text("Nombre de la Pestaña (ej: Niños, Deportes, Anime)") },
                    modifier = Modifier.fillMaxWidth().testTag("tab_name_input"),
                    singleLine = true
                )

                // Quick suggestions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VoicePromptChip(
                        text = "Deportes",
                        onClick = { tabName = "Deportes"; selectedIcon = "sports_esports"; selectedColor = "#10B981" },
                        modifier = Modifier.weight(1f)
                    )
                    VoicePromptChip(
                        text = "Niños / Familia",
                        onClick = { tabName = "Niños"; selectedIcon = "movie"; selectedColor = "#FBBF24" },
                        modifier = Modifier.weight(1f)
                    )
                    VoicePromptChip(
                        text = "Retro Games",
                        onClick = { tabName = "Retro Games"; selectedIcon = "sports_esports"; selectedColor = "#A855F7" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Icon Picker
                Text(
                    text = "Icono:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { (name, iconVector) ->
                        val isSelected = selectedIcon == name
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TvCyanAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { selectedIcon = name },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = name,
                                tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Color Picker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Color:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Optional: Checkbox list of apps to immediately group into this tab
                if (allApps.isNotEmpty()) {
                    Text(
                        text = "Agrupar aplicaciones inmediatamente (${selectedPackages.size} seleccionadas):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(
                        modifier = Modifier.height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allApps, key = { it.packageName }) { app ->
                            val isChecked = selectedPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                    .clickable {
                                        if (isChecked) selectedPackages.remove(app.packageName) else selectedPackages.add(app.packageName)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = app.label,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedPackages.add(app.packageName) else selectedPackages.remove(app.packageName)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = TvCyanAccent,
                                        checkmarkColor = Color.Black
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (tabName.isNotBlank()) {
                                onAddTab(tabName.trim(), selectedIcon, selectedColor, selectedPackages.toList())
                            }
                        },
                        enabled = tabName.isNotBlank(),
                        modifier = Modifier.testTag("create_tab_confirm")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear Pestaña")
                    }
                }
            }
        }
    }
}
