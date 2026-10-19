package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvFocusBorder
import java.util.Locale

@Composable
fun VoiceSearchDialog(
    transcript: String,
    onVoiceResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(true) }
    var manualQuery by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Escuchando comando de voz...") }

    // Audio Wave Animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Speech Recognizer Intent Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenWords = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = spokenWords?.firstOrNull() ?: ""
            if (recognized.isNotBlank()) {
                onVoiceResult(recognized)
            } else {
                statusText = "No se detectó audio. Escribe tu comando abajo."
            }
        } else {
            statusText = "Micrófono cerrado. Puedes usar el teclado en pantalla."
        }
        isListening = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Di el nombre de una app o comando")
                }
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                statusText = "Reconocimiento de voz no disponible en este hardware. Usa el teclado:"
                isListening = false
            }
        } else {
            statusText = "Permiso de micrófono denegado. Usa el teclado:"
            isListening = false
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, TvFocusBorder, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Control por Voz Inteligente",
                        fontSize = 18.sp,
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

                // Pulsing Mic Icon
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(if (isListening) TvCyanAccent.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isListening) TvCyanAccent else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Micrófono",
                            tint = if (isListening) Color.Black else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Quick Suggestion Chips
                Text(
                    text = "Comandos sugeridos para control remoto:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VoicePromptChip(
                        text = "Abrir Netflix",
                        onClick = { onVoiceResult("Abrir Netflix") },
                        modifier = Modifier.weight(1f)
                    )
                    VoicePromptChip(
                        text = "Modo Noche",
                        onClick = { onVoiceResult("Modo Noche") },
                        modifier = Modifier.weight(1f)
                    )
                    VoicePromptChip(
                        text = "Ultra Ahorro",
                        onClick = { onVoiceResult("Ultra Ahorro") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Keyboard Fallback Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualQuery,
                        onValueChange = { manualQuery = it },
                        placeholder = { Text("O escribe un comando o app...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("voice_manual_input"),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (manualQuery.isNotBlank()) {
                                onVoiceResult(manualQuery)
                            }
                        },
                        modifier = Modifier.testTag("voice_manual_submit")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            }
        }
    }
}

@Composable
fun VoicePromptChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TvCyanAccent,
            maxLines = 1
        )
    }
}
