package com.example.ui.components

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppItem
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder

@Composable
fun HiddenAppsDialog(
    hiddenApps: List<AppItem>,
    isUnlocked: Boolean,
    pinError: String?,
    onUnlockWithPin: (String) -> Boolean,
    onLockAgain: () -> Unit,
    onUnhideApp: (AppItem) -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }

    LaunchedEffect(pinError) {
        if (pinError != null) {
            enteredPin = ""
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(560.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Bóveda",
                            tint = if (isUnlocked) TvCyanAccent else Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Bóveda de Aplicaciones Ocultas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isUnlocked) "${hiddenApps.size} apps protegidas" else "Introduce el código PIN de 4 dígitos",
                                fontSize = 12.sp,
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

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                if (!isUnlocked) {
                    // PIN Entry Keypad
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // PIN Display Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            for (i in 0 until 4) {
                                val filled = i < enteredPin.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) TvCyanAccent else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, TvFocusBorder, CircleShape)
                                )
                            }
                        }

                        if (pinError != null) {
                            Text(
                                text = pinError,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "PIN por defecto: 0000",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        }

                        // TV Remote Friendly Keypad Grid
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val rows = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("C", "0", "OK")
                            )

                            for (row in rows) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    for (digit in row) {
                                        PinKeypadButton(
                                            label = digit,
                                            onClick = {
                                                when (digit) {
                                                    "C" -> {
                                                        if (enteredPin.isNotEmpty()) {
                                                            enteredPin = enteredPin.dropLast(1)
                                                        }
                                                    }
                                                    "OK" -> {
                                                        if (enteredPin.length == 4) {
                                                            val ok = onUnlockWithPin(enteredPin)
                                                            if (!ok) {
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        if (enteredPin.length < 4) {
                                                            val next = enteredPin + digit
                                                            if (next.length == 4) {
                                                                val ok = onUnlockWithPin(next)
                                                                if (!ok) {
                                                                    enteredPin = ""
                                                                } else {
                                                                    enteredPin = next
                                                                }
                                                            } else {
                                                                enteredPin = next
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Unlocked State: List of hidden apps
                    if (hiddenApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay aplicaciones ocultas actualmente.\nUsa el botón Menú del mando sobre cualquier app para ocultarla.",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.height(240.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(hiddenApps, key = { it.packageName }) { app ->
                                TvAppCard(
                                    app = app,
                                    onLaunch = onLaunchApp,
                                    onOpenMenu = onUnhideApp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onLockAgain,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bloquear Bóveda")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinKeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(if (isFocused) 1.15f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFocused) TvCyanAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) TvFocusBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .testTag("keypad_$label"),
        contentAlignment = Alignment.Center
    ) {
        if (label == "C") {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Borrar",
                tint = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = label,
                fontSize = if (label == "OK") 14.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
