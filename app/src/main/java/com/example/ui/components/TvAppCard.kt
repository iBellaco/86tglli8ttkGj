package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    isDockItem: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "appScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) TvFocusBorder else Color.Transparent,
        label = "appBorder"
    )

    val brandColor = try {
        Color(android.graphics.Color.parseColor(app.iconColorHex))
    } catch (e: Exception) {
        TvCyanAccent
    }

    Column(
        modifier = modifier
            .width(if (isDockItem) 110.dp else 135.dp)
            .scale(scale)
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MENU) {
                        onOpenMenu(app)
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
        // App Tile Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(
                    elevation = if (isFocused) 16.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = if (isFocused) TvCyanAccent else Color.Black
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isFocused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = if (isFocused) 3.dp else 1.dp,
                    color = if (isFocused) borderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Distinctive Icon Background Accent Circle
            Box(
                modifier = Modifier
                    .size(if (isDockItem) 54.dp else 68.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resolveAppIcon(app.iconType),
                    contentDescription = app.label,
                    tint = brandColor,
                    modifier = Modifier.size(if (isDockItem) 32.dp else 40.dp)
                )
            }

            // Top Badges (Favorite & Pin)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (app.isFavorite) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorito",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                if (app.isPinnedToDock) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(TvCyanAccent.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Fijada en Dock",
                            tint = Color.Black,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // Bottom Hidden Indicator (if viewed inside hidden list)
            if (app.isHidden) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Oculta",
                        tint = Color.White,
                        modifier = Modifier.padding(3.dp).size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App Title
        Text(
            text = app.label,
            fontSize = if (isDockItem) 12.sp else 13.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            color = if (isFocused) TvCyanAccent else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
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
