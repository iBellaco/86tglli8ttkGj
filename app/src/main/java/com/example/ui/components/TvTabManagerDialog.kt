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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.data.model.TabGroupWithApps
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvEnergySaverGreen
import com.example.ui.theme.TvFocusBorder

@Composable
fun TvTabManagerDialog(
    tabGroups: List<TabGroupWithApps>,
    allApps: List<AppItem>,
    onAddTab: () -> Unit,
    onEditTab: (CategoryTab) -> Unit,
    onDeleteTab: (String) -> Unit,
    onMoveTabUp: (String) -> Unit,
    onMoveTabDown: (String) -> Unit,
    onManageAppsForTab: (CategoryTab) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(680.dp)
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
                    Column {
                        Text(
                            text = "Gestor de Pestañas y Categorías",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Organiza, reordena y agrupa aplicaciones por categorías para Android TV",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddTab,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("tab_manager_create_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nueva Pestaña")
                        }

                        TvHeaderIconButton(
                            icon = Icons.Default.Close,
                            contentDesc = "Cerrar",
                            onClick = onDismiss
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Tab List
                LazyColumn(
                    modifier = Modifier.height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tabGroups, key = { it.tab.id }) { group ->
                        TabRowItem(
                            group = group,
                            onEdit = { onEditTab(group.tab) },
                            onDelete = { onDeleteTab(group.tab.id) },
                            onMoveUp = { onMoveTabUp(group.tab.id) },
                            onMoveDown = { onMoveTabDown(group.tab.id) },
                            onManageApps = { onManageAppsForTab(group.tab) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabRowItem(
    group: TabGroupWithApps,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onManageApps: () -> Unit
) {
    val tab = group.tab
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val tabColor = try {
        Color(android.graphics.Color.parseColor(tab.colorHex))
    } catch (e: Exception) {
        TvCyanAccent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isFocused) 1.02f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Tab Icon + Title + Apps count badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tabColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resolveTabIcon(tab.iconName),
                    contentDescription = tab.title,
                    tint = tabColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tab.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (tab.isCustom) {
                        Text(
                            text = "Personalizada",
                            fontSize = 10.sp,
                            color = TvCyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = "${group.appCount} aplicaciones agrupadas",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons Row (D-Pad friendly)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Manage Apps button
            IconButton(
                onClick = onManageApps,
                modifier = Modifier.size(36.dp).testTag("tab_manage_apps_${tab.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = "Agrupar Apps",
                    tint = TvCyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Move Up
            IconButton(
                onClick = onMoveUp,
                modifier = Modifier.size(36.dp).testTag("tab_move_up_${tab.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Mover Arriba",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Move Down
            IconButton(
                onClick = onMoveDown,
                modifier = Modifier.size(36.dp).testTag("tab_move_down_${tab.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Mover Abajo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Edit (only for custom or all)
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp).testTag("tab_edit_${tab.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete (only custom tabs)
            if (tab.isCustom) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("tab_delete_${tab.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Dialog to Group/Assign Applications to a specific Tab
@Composable
fun TabAppAssignmentDialog(
    tab: CategoryTab,
    allApps: List<AppItem>,
    onSaveAssignments: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedPackages = remember {
        mutableStateListOf<String>().apply {
            addAll(allApps.filter { it.categoryId == tab.id }.map { it.packageName })
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(580.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Agrupar Apps en: ${tab.title}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${selectedPackages.size} seleccionadas de ${allApps.size} apps disponibles",
                            fontSize = 11.sp,
                            color = TvCyanAccent
                        )
                    }
                    TvHeaderIconButton(
                        icon = Icons.Default.Close,
                        contentDesc = "Cerrar",
                        onClick = onDismiss
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Scrollable App Selection Checklist
                LazyColumn(
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allApps, key = { it.packageName }) { app ->
                        val isChecked = selectedPackages.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .clickable {
                                    if (isChecked) {
                                        selectedPackages.remove(app.packageName)
                                    } else {
                                        selectedPackages.add(app.packageName)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = resolveAppIcon(app.iconType),
                                    contentDescription = app.label,
                                    tint = try { Color(android.graphics.Color.parseColor(app.iconColorHex)) } catch (e: Exception) { TvCyanAccent },
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = app.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (app.categoryId == tab.id) "Actualmente en esta pestaña" else "En categoría: ${app.categoryId}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedPackages.add(app.packageName)
                                    } else {
                                        selectedPackages.remove(app.packageName)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = TvCyanAccent,
                                    checkmarkColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // Footer Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onSaveAssignments(selectedPackages.toList()) },
                        modifier = Modifier.testTag("save_tab_app_assignments")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Agrupación")
                    }
                }
            }
        }
    }
}

// Dialog to Edit an existing Tab (Name, Icon, Color)
@Composable
fun TabEditDialog(
    tab: CategoryTab,
    onSave: (title: String, iconName: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(tab.title) }
    var selectedIcon by remember { mutableStateOf(tab.iconName) }
    var selectedColor by remember { mutableStateOf(tab.colorHex) }

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
                .width(480.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Pestaña",
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la Pestaña") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_tab_title_input"),
                    singleLine = true
                )

                // Icon Selection
                Text(
                    text = "Icono Representativo:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { (name, iconVector) ->
                        val isSelected = selectedIcon == name
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) TvCyanAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { selectedIcon = name }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = name,
                                tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Color Selection
                Text(
                    text = "Color de Acento:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, selectedIcon, selectedColor)
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_tab_edit_button")
                    ) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

fun resolveTabIcon(iconName: String): ImageVector {
    return when (iconName) {
        "star" -> Icons.Default.Star
        "movie" -> Icons.Default.Movie
        "sports_esports" -> Icons.Default.SportsEsports
        "build" -> Icons.Default.Build
        "music_note" -> Icons.Default.MusicNote
        "tv" -> Icons.Default.Tv
        "folder" -> Icons.Default.Folder
        else -> Icons.Default.Apps
    }
}
