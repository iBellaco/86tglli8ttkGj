package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvAppCard(
    app: AppItem,
    onLaunch: (AppItem) -> Unit,
    onOpenMenu: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    isDockItem: Boolean = false,
    onFocusChanged: ((Boolean) -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        onFocusChanged?.invoke(isFocused)
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "appScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) TvFocusBorder else Color.Transparent,
        label = "appBorder"
    )

    val context = LocalContext.current
    val realAppBitmap = remember(app.packageName) {
        getAppIconBitmap(context, app.packageName)
    }

    val brandColor = try {
        Color(android.graphics.Color.parseColor(app.iconColorHex))
    } catch (e: Exception) {
        TvCyanAccent
    }

    val coroutineScope = rememberCoroutineScope()
    var longPressJob by remember { mutableStateOf<Job?>(null) }
    var menuOpenedByHold by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(if (isDockItem) 78.dp else 88.dp)
            .scale(scale)
            .onKeyEvent { keyEvent ->
                val keyCode = keyEvent.nativeKeyEvent.keyCode
                val isCenterKey = keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER

                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_MENU) {
                        onOpenMenu(app)
                        true
                    } else if (isCenterKey) {
                        if (keyEvent.nativeKeyEvent.repeatCount == 0) {
                            menuOpenedByHold = false
                            longPressJob?.cancel()
                            longPressJob = coroutineScope.launch {
                                // Holding for 3 seconds opens the app menu
                                delay(3000L)
                                menuOpenedByHold = true
                                onOpenMenu(app)
                            }
                        }
                        true
                    } else false
                } else if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                    if (isCenterKey) {
                        longPressJob?.cancel()
                        longPressJob = null
                        if (!menuOpenedByHold) {
                            // Released before 3 seconds: normal launch
                            onLaunch(app)
                        }
                        menuOpenedByHold = false
                        true
                    } else false
                } else false
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onLaunch(app) },
                onLongClick = { onOpenMenu(app) }
            )
            .focusable(interactionSource = interactionSource)
            .testTag("app_card_${app.packageName.replace('.', '_')}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Compact App Tile Box
        Box(
            modifier = Modifier
                .size(if (isDockItem) 60.dp else 70.dp)
                .shadow(
                    elevation = if (isFocused) 10.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = if (isFocused) TvCyanAccent else Color.Transparent
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isFocused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                )
                .border(
                    width = if (isFocused) 2.5.dp else 0.dp,
                    color = if (isFocused) borderColor else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (realAppBitmap != null) {
                // Real Android installed application icon
                Image(
                    bitmap = realAppBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(if (isDockItem) 46.dp else 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                // Distinctive Icon Background Accent Circle (fallback)
                Box(
                    modifier = Modifier
                        .size(if (isDockItem) 46.dp else 56.dp)
                        .clip(CircleShape)
                        .background(brandColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = resolveAppIcon(app.iconType),
                        contentDescription = app.label,
                        tint = brandColor,
                        modifier = Modifier.size(if (isDockItem) 28.dp else 34.dp)
                    )
                }
            }

            // Top Badges (Favorite & Pin)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (app.isFavorite) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorito",
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                if (app.isPinnedToDock) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(TvCyanAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Anclada",
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Bottom Hidden Indicator (if viewed inside hidden list)
            if (app.isHidden) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Oculta",
                        tint = Color.White,
                        modifier = Modifier.padding(2.dp).size(9.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // App Title: Static, clean, crisp and fast (no slow marquee animations or extra animators)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isFocused) TvCyanAccent.copy(alpha = 0.18f) else Color.Transparent)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.label,
                fontSize = if (isDockItem) 11.sp else 11.5.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                color = if (isFocused) TvCyanAccent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun resolveAppIcon(iconType: String): ImageVector {
    return when (iconType) {
        "YOUTUBE" -> Icons.Default.PlayArrow
        "NETFLIX" -> Icons.Default.Movie
        "PRIME" -> Icons.Default.Videocam
        "DISNEY" -> Icons.Default.Tv
        "SPOTIFY" -> Icons.Default.MusicNote
        "PLEX" -> Icons.Default.Movie
        "TWITCH" -> Icons.Default.Tv
        "KODI" -> Icons.Default.PlayArrow
        "VLC" -> Icons.Default.PlayArrow
        "RETROARCH" -> Icons.Default.SportsEsports
        "GAMES" -> Icons.Default.SportsEsports
        "SETTINGS" -> Icons.Default.Settings
        "FILES" -> Icons.Default.Folder
        "BROWSER" -> Icons.Default.Language
        "TOOLS" -> Icons.Default.Build
        else -> Icons.Default.Tv
    }
}

private val iconCache = android.util.LruCache<String, androidx.compose.ui.graphics.ImageBitmap>(64)

fun clearAppIconCache() {
    iconCache.evictAll()
}

fun getAppIconBitmap(context: android.content.Context, packageName: String): androidx.compose.ui.graphics.ImageBitmap? {
    iconCache.get(packageName)?.let { return it }

    return try {
        val pm = context.packageManager
        val drawable = try {
            pm.getApplicationBanner(packageName)
        } catch (e: Exception) {
            null
        } ?: pm.getApplicationIcon(packageName)

        val imageBitmap = if (drawable is android.graphics.drawable.BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            drawable.bitmap.asImageBitmap()
        } else {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
            val bitmap = android.graphics.Bitmap.createBitmap(
                width.coerceIn(48, 256),
                height.coerceIn(48, 256),
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }

        imageBitmap?.also { iconCache.put(packageName, it) }
    } catch (e: Exception) {
        null
    }
}

