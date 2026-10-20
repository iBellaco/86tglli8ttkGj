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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder

@Composable
fun AppActionDialog(
    app: AppItem,
    categories: List<CategoryTab>,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleHide: () -> Unit,
    onTogglePinDock: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChangeCategory: (String) -> Unit
) {
    var showCategorySelector by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(440.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = app.label,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = app.packageName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TvHeaderIconButton(
                        icon = Icons.Default.Close,
                        contentDesc = "Cerrar",
                        onClick = onDismiss
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                if (!showCategorySelector) {
                    // Action List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionMenuItem(
                            icon = Icons.Default.PlayArrow,
                            title = "Abrir Aplicación",
                            subtitle = "Iniciar de inmediato en Android TV",
                            onClick = onLaunch,
                            testTag = "action_open"
                        )

                        ActionMenuItem(
                            icon = Icons.Default.PushPin,
                            title = if (app.isPinnedToDock) "Desfijar de Acceso Rápido" else "Fijar en Acceso Rápido",
                            subtitle = "Fijar icono en la barra principal del Home",
                            onClick = onTogglePinDock,
                            testTag = "action_dock"
                        )

                        ActionMenuItem(
                            icon = Icons.Default.Star,
                            title = if (app.isFavorite) "Quitar de Favoritos" else "Marcar como Favorito",
                            subtitle = "Mostrar en pestaña principal de favoritos",
                            onClick = onToggleFavorite,
                            testTag = "action_favorite"
                        )

                        ActionMenuItem(
                            icon = if (app.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            title = if (app.isHidden) "Mostrar Aplicación en el Inicio" else "Ocultar Aplicación",
                            subtitle = if (app.isHidden) "Regresar app a las pestañas visibles" else "¿Deseas ocultar esta app? Se moverá a la bóveda protegida por PIN",
                            onClick = onToggleHide,
                            testTag = "action_hide",
                            danger = !app.isHidden
                        )

                        ActionMenuItem(
                            icon = Icons.Default.Category,
                            title = "Mover a otra Pestaña",
                            subtitle = "Categoría actual: ${app.categoryId}",
                            onClick = { showCategorySelector = true },
                            testTag = "action_category"
                        )
                    }
                } else {
                    // Category Selection view
                    Text(
                        text = "Seleccionar Pestaña:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            ActionMenuItem(
                                icon = Icons.Default.Category,
                                title = cat.title,
                                subtitle = if (app.categoryId == cat.id) "Categoría actual" else "Toca para mover aquí",
                                onClick = { onChangeCategory(cat.id) },
                                testTag = "cat_opt_${cat.id}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String,
    danger: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> TvCyanAccent
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        label = "itemBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isFocused) 1.02f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) TvFocusBorder else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isFocused) Color.Black else if (danger) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) Color.Black else if (danger) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isFocused) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
