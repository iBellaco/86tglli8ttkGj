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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder

/**
 * Checks whether an app is installed by default (system package, launcher package,
 * or preconfigured default TV suite). Default apps cannot be uninstalled.
 */
fun isAppInstalledByDefault(context: Context, packageName: String): Boolean {
    if (packageName == context.packageName) return true

    val defaultSuitePackages = setOf(
        "com.google.android.youtube.tv",
        "com.netflix.ninja",
        "com.amazon.amazonvideo.livingroom",
        "com.disney.disneyplus",
        "com.spotify.tv.android",
        "com.plexapp.android",
        "tv.twitch.android.app",
        "org.xbmc.kodi",
        "org.videolan.vlc",
        "com.retroarch",
        "com.android.vending",
        "com.android.settings",
        "com.google.android.tv.settings",
        "com.google.android.katniss"
    )
    if (defaultSuitePackages.contains(packageName)) return true

    return try {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
    } catch (e: Exception) {
        defaultSuitePackages.contains(packageName)
    }
}

@Composable
fun AppActionDialog(
    app: AppItem,
    categories: List<CategoryTab>,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleHide: () -> Unit,
    onTogglePinDock: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChangeCategory: (String) -> Unit,
    onUninstall: () -> Unit
) {
    val context = LocalContext.current
    val isDefaultApp = remember(app.packageName) {
        isAppInstalledByDefault(context, app.packageName)
    }
    var showCategorySelector by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.92f)
                .heightIn(max = 540.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header (Fixed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = app.label.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column {
                            Text(
                                text = app.label,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = app.packageName,
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

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                if (!showCategorySelector) {
                    // Scrollable Action List - Ensures "Ocultar Aplicación" is 100% reachable on any screen
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Abrir
                        ActionMenuItem(
                            icon = Icons.Default.PlayArrow,
                            title = "Abrir Aplicación",
                            subtitle = "Iniciar de inmediato en Android TV",
                            onClick = onLaunch,
                            testTag = "action_open"
                        )

                        // 2. Anclar Acceso Directo
                        ActionMenuItem(
                            icon = Icons.Default.PushPin,
                            title = if (app.isPinnedToDock) "Desanclar Acceso Directo" else "Anclar como Acceso Directo",
                            subtitle = if (app.isPinnedToDock) "Quitar de la fila de accesos directos anclados" else "Fijar en la barra superior de accesos directos",
                            onClick = onTogglePinDock,
                            testTag = "action_dock"
                        )

                        // 3. Ocultar Aplicación (Highlighted with Vault Icon)
                        ActionMenuItem(
                            icon = if (app.isHidden) Icons.Default.LockOpen else Icons.Default.Lock,
                            title = if (app.isHidden) "Mostrar Aplicación en el Inicio" else "Ocultar Aplicación (Bóveda)",
                            subtitle = if (app.isHidden) "Regresar a la cuadrícula visible del inicio" else "Mover a la bóveda privada protegida por PIN",
                            onClick = onToggleHide,
                            testTag = "action_hide",
                            danger = !app.isHidden
                        )

                        // 4. Favoritos
                        ActionMenuItem(
                            icon = Icons.Default.Star,
                            title = if (app.isFavorite) "Quitar de Favoritos" else "Marcar como Favorito",
                            subtitle = "Mostrar en pestaña principal de favoritos",
                            onClick = onToggleFavorite,
                            testTag = "action_favorite"
                        )

                        // 5. Mover a otra pestaña
                        ActionMenuItem(
                            icon = Icons.Default.Category,
                            title = "Mover a otra Pestaña",
                            subtitle = "Categoría actual: ${app.categoryId}",
                            onClick = { showCategorySelector = true },
                            testTag = "action_category"
                        )

                        // 6. Desinstalar Aplicación (Solo disponible para aplicaciones no instaladas por defecto)
                        if (!isDefaultApp) {
                            ActionMenuItem(
                                icon = Icons.Default.Delete,
                                title = "Desinstalar Aplicación",
                                subtitle = "Eliminar o desinstalar esta app del dispositivo",
                                onClick = onUninstall,
                                testTag = "action_uninstall",
                                danger = true
                            )
                        }
                    }
                } else {
                    // Category Selection view
                    Text(
                        text = "Seleccionar Pestaña de Destino:",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .weight(1f, fill = false),
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
            .padding(horizontal = 14.dp, vertical = 9.dp)
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
