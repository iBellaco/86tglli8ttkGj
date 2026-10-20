package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder

/**
 * TV Search Bar located directly beneath the clock / header.
 * 
 * CRITICAL FOR ANDROID TV:
 * When navigating with the D-pad remote and passing over this bar, the on-screen
 * software keyboard does NOT automatically appear. The keyboard ONLY opens when
 * the user explicitly selects / clicks the search bar (DPAD_CENTER / ENTER / tap).
 */
@Composable
fun TvSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenPlayStore: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val textFieldFocusRequester = remember { FocusRequester() }

    val containerInteractionSource = remember { MutableInteractionSource() }
    val isContainerFocused by containerInteractionSource.collectIsFocusedAsState()

    // Request keyboard when explicit editing is activated
    LaunchedEffect(isEditing) {
        if (isEditing) {
            textFieldFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Search Bar Container
        // When not editing: focusable bar that highlights with TV focus border without popping up keyboard
        // When user presses DPAD_CENTER / Enter or clicks: enters editing mode and shows keyboard
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .scale(if (isContainerFocused || isEditing) 1.02f else 1f)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    if (isContainerFocused || isEditing)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
                .border(
                    width = if (isContainerFocused || isEditing) 2.dp else 1.dp,
                    color = if (isContainerFocused || isEditing) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(26.dp)
                )
                .onKeyEvent { keyEvent ->
                    if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                if (!isEditing) {
                                    isEditing = true
                                    true
                                } else false
                            }
                            KeyEvent.KEYCODE_BACK -> {
                                if (isEditing) {
                                    isEditing = false
                                    keyboardController?.hide()
                                    true
                                } else false
                            }
                            else -> false
                        }
                    } else false
                }
                .clickable(
                    interactionSource = containerInteractionSource,
                    indication = null,
                    onClick = {
                        isEditing = true
                    }
                )
                .focusable(enabled = !isEditing, interactionSource = containerInteractionSource)
                .testTag("app_search_field_container"),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar Aplicaciones",
                    tint = if (isContainerFocused || isEditing) TvCyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (isEditing) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(textFieldFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && isEditing) {
                                        isEditing = false
                                    }
                                }
                                .testTag("app_search_text_input"),
                            textStyle = TextStyle(
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(TvCyanAccent),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    isEditing = false
                                    keyboardController?.hide()
                                }
                            )
                        )
                    } else {
                        // Static display when not actively typing — prevents keyboard from opening during navigation
                        Text(
                            text = if (searchQuery.isNotEmpty()) searchQuery else "Buscar aplicaciones o juegos... (Pulsa OK)",
                            fontSize = 14.sp,
                            fontWeight = if (searchQuery.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (searchQuery.isNotEmpty())
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onSearchQueryChange("")
                            isEditing = false
                            keyboardController?.hide()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar Búsqueda",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 1. Microphone Button for Voice Search (Icon-only, no text, placed before Play Store)
        TvSearchIconOnlyButton(
            icon = Icons.Default.Mic,
            contentDescription = "Búsqueda por Voz",
            accentColor = TvCyanAccent,
            onClick = onVoiceSearchClick,
            testTag = "voice_search_bar_button"
        )

        // 2. Android TV Play Store Button
        TvSearchActionButton(
            icon = Icons.Default.Storefront,
            label = "Play Store",
            accentColor = Color(0xFF00C853), // Play Store Green
            onClick = onOpenPlayStore,
            testTag = "tv_play_store_button"
        )
    }
}

@Composable
fun TvSearchIconOnlyButton(
    icon: ImageVector,
    contentDescription: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> accentColor
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        label = "iconBtnBg"
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(if (isFocused) 1.12f else 1f)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isFocused) Color.Black else accentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun TvSearchActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> accentColor
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        label = "btnBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.Black
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "contentColor"
    )

    Row(
        modifier = Modifier
            .height(52.dp)
            .scale(if (isFocused) 1.08f else 1f)
            .clip(RoundedCornerShape(26.dp))
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isFocused) Color.Black else accentColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
