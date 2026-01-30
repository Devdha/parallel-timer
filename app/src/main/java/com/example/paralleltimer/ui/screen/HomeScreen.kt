package com.example.paralleltimer.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.paralleltimer.R
import com.example.paralleltimer.domain.model.TimerDisplayItem
import com.example.paralleltimer.domain.model.TimerPreset
import com.example.paralleltimer.ui.components.AddPresetDialog
import com.example.paralleltimer.ui.components.AddTimerDialog
import com.example.paralleltimer.ui.components.EditTimerDialog
import com.example.paralleltimer.ui.components.GroupFilterChips
import com.example.paralleltimer.ui.components.PresetChips
import com.example.paralleltimer.ui.components.StatisticsBottomSheet
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
    onClearSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showAddPresetDialog by rememberSaveable { mutableStateOf(false) }
    var presetToDelete by rememberSaveable { mutableStateOf<TimerPreset?>(null) }
    var timerToEdit by rememberSaveable { mutableStateOf<TimerDisplayItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle snackbar message
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { deletedLabel ->
            val displayLabel = deletedLabel.ifEmpty { context.getString(R.string.timer) }
            val message = context.getString(R.string.timer_deleted, displayLabel)
            val undoLabel = context.getString(R.string.undo)
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    onAction(TimerAction.UndoDelete)
                }
                SnackbarResult.Dismissed -> {
                    onClearSnackbar()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.inversePrimary
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_timer),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick Start Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.quick_start),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.tap_to_create),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onAction(TimerAction.ToggleStatistics) }) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = stringResource(R.string.statistics)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

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
            }

            Spacer(Modifier.height(16.dp))

            // Group Filter Section
            GroupFilterChips(
                groups = uiState.groups,
                selectedGroupId = uiState.selectedGroupId,
                onGroupSelected = { groupId -> onAction(TimerAction.SelectGroup(groupId)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Timer list section
            if (uiState.timers.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    val runningCount = uiState.timers.count { it.timer.state == com.example.paralleltimer.domain.model.TimerState.Running }
                    SectionHeader(
                        icon = Icons.Outlined.Timer,
                        title = stringResource(R.string.your_timers),
                        subtitle = stringResource(R.string.running_count, runningCount)
                    )
                }
                Spacer(Modifier.height(8.dp))
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
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 80.dp
                    )
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
            onCreate = { label, colorIndex, durationMs, groupId ->
                onAction(TimerAction.CreateCustom(label, colorIndex, durationMs, groupId))
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
            title = { Text(stringResource(R.string.delete_preset)) },
            text = { Text(stringResource(R.string.delete_preset_confirm, preset.label)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePreset(preset)
                        presetToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Edit Timer Dialog
    timerToEdit?.let { timerDisplay ->
        EditTimerDialog(
            initialLabel = timerDisplay.timer.label,
            initialColorIndex = timerDisplay.timer.colorIndex,
            initialGroupId = timerDisplay.timer.groupId,
            onDismiss = { timerToEdit = null },
            onSave = { label, colorIndex, groupId ->
                onAction(TimerAction.Edit(timerDisplay.timer.id, label, colorIndex, groupId))
                timerToEdit = null
            }
        )
    }

    // Statistics Bottom Sheet
    if (uiState.showStatistics) {
        StatisticsBottomSheet(
            statistics = uiState.statistics,
            onDismiss = { onAction(TimerAction.ToggleStatistics) }
        )
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                text = stringResource(R.string.no_timers_yet),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.empty_state_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
