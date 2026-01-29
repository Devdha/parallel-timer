package com.example.paralleltimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.paralleltimer.domain.model.TimerState

@Composable
fun TimerControls(
    state: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (state) {
            TimerState.Idle -> {
                FilledTonalButton(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = "Start timer" }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Start")
                }
                // Delete button for Idle state
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "Delete timer" },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            TimerState.Running -> {
                FilledTonalButton(
                    onClick = onPause,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = "Pause timer" }
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Pause")
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .height(48.dp)
                        .semantics { contentDescription = "Reset timer" }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                // Delete button for Running state
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "Delete timer" },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            TimerState.Paused -> {
                FilledTonalButton(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = "Resume timer" }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Resume")
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .height(48.dp)
                        .semantics { contentDescription = "Reset timer" }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                // Delete button for Paused state
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "Delete timer" },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            TimerState.Done -> {
                FilledTonalButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = "Reset timer" }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Reset")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .height(48.dp)
                        .semantics { contentDescription = "Delete timer" },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}
