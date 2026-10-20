package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder
import kotlinx.coroutines.launch

/**
 * AppGrid: Componente optimizado para Android TV y navegación fluida con D-Pad (control remoto).
 * 
 * Características clave para TV:
 * - LazyVerticalGrid con clipToPadding = false para evitar recortes de elevación y glow al escalar iconos.
 * - Auto-scroll inteligente: centra dinámicamente la fila enfocada para una visibilidad continua en la pantalla de TV.
 * - Columnas fijas o adaptables para movimientos 2D deterministas (arriba, abajo, izquierda, derecha).
 * - Intercepción de botones del control remoto (Enter/Dpad Center para abrir, Menú para opciones).
 * - Barra inferior de estado con pista de navegación para el mando a distancia (10-foot UI).
 */
@Composable
fun AppGrid(
    apps: List<AppItem>,
    onLaunchApp: (AppItem) -> Unit,
    onOpenMenu: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    columnsCount: Int = 5,
    gridState: LazyGridState = rememberLazyGridState(),
    emptyMessage: String = "No se encontraron aplicaciones en esta categoría",
    emptySubMessage: String = "Usa el botón Menú del mando sobre cualquier app para moverla a esta pestaña"
) {
    val coroutineScope = rememberCoroutineScope()
    var focusedApp by remember { mutableStateOf<AppItem?>(null) }
    var focusedIndex by remember { mutableStateOf<Int?>(null) }

    // Smart auto-scroll: when focused item changes, ensure its row is visible comfortably
    LaunchedEffect(focusedIndex) {
        focusedIndex?.let { index ->
            val layoutInfo = gridState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val isVisible = visibleItems.any { it.index == index }

            if (!isVisible) {
                // Smooth scroll to row
                gridState.animateScrollToItem(index)
            } else {
                // If it's near the bottom visible edge, nudge forward so the next row is previewed
                val lastVisible = visibleItems.lastOrNull()?.index ?: 0
                if (index >= lastVisible - 1 && lastVisible < apps.size - 1) {
                    coroutineScope.launch {
                        gridState.animateScrollToItem((index + columnsCount).coerceAtMost(apps.size - 1))
                    }
                }
            }
        }
    }

    // Reset focused item if apps list changes or becomes empty
    LaunchedEffect(apps) {
        if (apps.isEmpty()) {
            focusedApp = null
            focusedIndex = null
        } else if (focusedApp != null && apps.none { it.packageName == focusedApp?.packageName }) {
            focusedApp = apps.firstOrNull()
            focusedIndex = if (apps.isNotEmpty()) 0 else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_grid_container")
    ) {
        if (apps.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = TvCyanAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = emptyMessage,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = emptySubMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Main D-Pad Optimized LazyVerticalGrid
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                state = gridState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y -> {
                                    focusedApp?.let {
                                        onOpenMenu(it)
                                        true
                                    } ?: false
                                }
                                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                    focusedApp?.let {
                                        onLaunchApp(it)
                                        true
                                    } ?: false
                                }
                                else -> false
                            }
                        } else false
                    }
                    .testTag("app_grid_lazy_vertical"),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 10.dp,
                    bottom = 28.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                itemsIndexed(
                    items = apps,
                    key = { _, item -> item.packageName }
                ) { index, app ->
                    TvAppCard(
                        app = app,
                        onLaunch = { onLaunchApp(app) },
                        onOpenMenu = { onOpenMenu(app) },
                        onFocusChanged = { isFocused ->
                            if (isFocused) {
                                focusedApp = app
                                focusedIndex = index
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_grid_item_$index")
                    )
                }
            }
        }

        // TV Remote Navigation Feedback & Shortcut Hints Bar
        AnimatedVisibility(
            visible = focusedApp != null && apps.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            focusedApp?.let { app ->
                val currentIndex = (focusedIndex ?: 0) + 1
                val totalCount = apps.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Current Focused App Info & Position
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TvCyanAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$currentIndex / $totalCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvCyanAccent
                            )
                        }

                        Text(
                            text = app.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "• Categoría: ${app.categoryId}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Right: TV Remote Button Shortcuts
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RemoteButtonHint(
                            icon = Icons.Default.PlayArrow,
                            buttonLabel = "ENTER / OK",
                            actionLabel = "Abrir"
                        )
                        RemoteButtonHint(
                            icon = Icons.Default.Menu,
                            buttonLabel = "MENÚ",
                            actionLabel = "Opciones"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteButtonHint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonLabel: String,
    actionLabel: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = buttonLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = actionLabel,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
