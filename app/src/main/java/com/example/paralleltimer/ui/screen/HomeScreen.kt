package com.example.paralleltimer.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paralleltimer.domain.model.TimerDisplayItem
import com.example.paralleltimer.domain.model.TimerPreset
import com.example.paralleltimer.ui.components.AddPresetDialog
import com.example.paralleltimer.ui.components.AddTimerDialog
import com.example.paralleltimer.ui.components.EditTimerDialog
import com.example.paralleltimer.ui.components.PresetChips
import com.example.paralleltimer.ui.components.TimerCard
import com.example.paralleltimer.ui.viewmodel.TimerAction
import com.example.paralleltimer.ui.viewmodel.TimerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: TimerUiState,
    onAction: (TimerAction) -> Unit,
    onAddPreset: (String, Long) -> Unit,
    onDeletePreset: (TimerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showAddPresetDialog by rememberSaveable { mutableStateOf(false) }
    var presetToDelete by rememberSaveable { mutableStateOf<TimerPreset?>(null) }
    var timerToEdit by rememberSaveable { mutableStateOf<TimerDisplayItem?>(null) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add timer",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Preset chips section
            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            PresetChips(
                presets = uiState.presets,
                onPresetClick = { preset ->
                    onAction(TimerAction.CreateFromPreset(preset.durationMs, preset.label))
                },
                onPresetLongClick = { preset ->
                    presetToDelete = preset
                },
                onAddClick = { showAddPresetDialog = true }
            )

            Spacer(Modifier.height(16.dp))

            // Timer list section
            if (uiState.timers.isNotEmpty()) {
                Text(
                    text = "Your Timers",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.timers.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState()
            }

            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.timers.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = uiState.timers,
                        key = { it.timer.id }
                    ) { timerDisplay ->
                        TimerCard(
                            timerDisplay = timerDisplay,
                            onStart = { onAction(TimerAction.Start(timerDisplay.timer.id)) },
                            onPause = { onAction(TimerAction.Pause(timerDisplay.timer.id)) },
                            onReset = { onAction(TimerAction.Reset(timerDisplay.timer.id)) },
                            onDelete = { onAction(TimerAction.Delete(timerDisplay.timer.id)) },
                            onEdit = { timerToEdit = timerDisplay }
                        )
                    }
                }
            }
        }
    }

    // Add Timer Dialog
    if (showAddDialog) {
        AddTimerDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { label, colorIndex, durationMs ->
                onAction(TimerAction.CreateCustom(label, colorIndex, durationMs))
            }
        )
    }

    // Add Preset Dialog
    if (showAddPresetDialog) {
        AddPresetDialog(
            onDismiss = { showAddPresetDialog = false },
            onAdd = { label, durationMs ->
                onAddPreset(label, durationMs)
                showAddPresetDialog = false
            }
        )
    }

    // Delete Preset Confirmation Dialog
    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Delete Preset") },
            text = { Text("Delete \"${preset.label}\" preset?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePreset(preset)
                        presetToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Timer Dialog
    timerToEdit?.let { timerDisplay ->
        EditTimerDialog(
            initialLabel = timerDisplay.timer.label,
            initialColorIndex = timerDisplay.timer.colorIndex,
            onDismiss = { timerToEdit = null },
            onSave = { label, colorIndex ->
                onAction(TimerAction.Edit(timerDisplay.timer.id, label, colorIndex))
                timerToEdit = null
            }
        )
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.HourglassEmpty,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "No timers yet",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Use the quick start presets above\nor tap + to create a custom timer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
