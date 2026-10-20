package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
 * AppGrid: Componente unificado y optimizado para Android TV y mando D-Pad.
 * Desliza suavemente la pantalla completa (cabecera + accesos rápidos + pestañas + cuadrícula de apps)
 * para garantizar visibilidad total y continua de todas las aplicaciones.
 */
@Composable
fun AppGrid(
    apps: List<AppItem>,
    onLaunchApp: (AppItem) -> Unit,
    onOpenMenu: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    columnsCount: Int = 6,
    gridState: LazyGridState = rememberLazyGridState(),
    emptyMessage: String = "No se encontraron aplicaciones en esta categoría",
    emptySubMessage: String = "Usa el botón Menú del mando sobre cualquier app para moverla a esta pestaña",
    headerContent: (@Composable () -> Unit)? = null
) {
    var focusedApp by remember { mutableStateOf<AppItem?>(null) }
    var focusedIndex by remember { mutableStateOf<Int?>(null) }

    // Reset focused item if apps list changes
    LaunchedEffect(apps) {
        if (apps.isEmpty()) {
            focusedApp = null
            focusedIndex = null
        } else if (focusedApp != null && apps.none { it.packageName == focusedApp?.packageName }) {
            focusedApp = apps.firstOrNull()
            focusedIndex = if (apps.isNotEmpty()) 0 else null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_grid_container")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y -> {
                                focusedApp?.let {
                                    onOpenMenu(it)
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
                top = 6.dp,
                bottom = 72.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Unified Top Header: Scrolls with the entire screen
            if (headerContent != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    headerContent()
                }
            }

            if (apps.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = TvCyanAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = emptyMessage,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = emptySubMessage,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
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

        // TV Remote Navigation Feedback & Shortcut Hints Bar as non-intrusive floating pill
        AnimatedVisibility(
            visible = focusedApp != null && apps.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            focusedApp?.let { app ->
                val currentIndex = (focusedIndex ?: 0) + 1
                val totalCount = apps.size

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TvCyanAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$currentIndex / $totalCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TvCyanAccent
                                )
                            }
                            Text(
                                text = app.label,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = TvCyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "OK: Abrir",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Menú: Opciones / Anclar",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
