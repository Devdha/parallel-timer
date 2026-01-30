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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.paralleltimer.R
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
                val startTimerDesc = stringResource(R.string.start_timer)
                val deleteTimerDesc = stringResource(R.string.delete_timer)
                FilledTonalButton(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = startTimerDesc }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.start))
                }
                // Delete button for Idle state
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = deleteTimerDesc },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            TimerState.Running -> {
                val pauseTimerDesc = stringResource(R.string.pause_timer)
                val resetTimerDesc = stringResource(R.string.reset_timer)
                val deleteTimerDesc = stringResource(R.string.delete_timer)
                FilledTonalButton(
                    onClick = onPause,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = pauseTimerDesc }
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.pause))
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .height(48.dp)
                        .semantics { contentDescription = resetTimerDesc }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                // Delete button for Running state
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = deleteTimerDesc },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            TimerState.Paused -> {
                val resumeTimerDesc = stringResource(R.string.resume_timer)
                val resetTimerDesc = stringResource(R.string.reset_timer)
                val deleteTimerDesc = stringResource(R.string.delete_timer)
                FilledTonalButton(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = resumeTimerDesc }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.resume))
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .height(48.dp)
                        .semantics { contentDescription = resetTimerDesc }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                // Delete button for Paused state
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = deleteTimerDesc },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            TimerState.Done -> {
                val resetTimerDesc = stringResource(R.string.reset_timer)
                val deleteTimerDesc = stringResource(R.string.delete_timer)
                FilledTonalButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = resetTimerDesc }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.start))
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .height(48.dp)
                        .semantics { contentDescription = deleteTimerDesc },
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
