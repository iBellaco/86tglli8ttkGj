package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.TvFocusBorder

@Composable
fun NewTabDialog(
    onAddTab: (title: String, iconName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tabName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("apps") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(440.dp)
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

                Text(
                    text = "Sugerencias rápidas:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VoicePromptChip(
                        text = "Deportes",
                        onClick = { tabName = "Deportes"; selectedIcon = "sports_esports" },
                        modifier = Modifier.weight(1f)
                    )
                    VoicePromptChip(
                        text = "Niños / Familia",
                        onClick = { tabName = "Niños"; selectedIcon = "movie" },
                        modifier = Modifier.weight(1f)
                    )
                    VoicePromptChip(
                        text = "Educación",
                        onClick = { tabName = "Educación"; selectedIcon = "build" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (tabName.isNotBlank()) {
                                onAddTab(tabName.trim(), selectedIcon)
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
