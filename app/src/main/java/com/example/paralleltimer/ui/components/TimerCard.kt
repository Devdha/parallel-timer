package com.example.paralleltimer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paralleltimer.domain.model.TimerDisplayItem
import com.example.paralleltimer.domain.model.TimerState
import com.example.paralleltimer.ui.theme.timerColors

@Composable
fun TimerCard(
    timerDisplay: TimerDisplayItem,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timer = timerDisplay.timer
    val remainingMs = timerDisplay.displayRemainingMs
    val timerColor = timerColors.getOrElse(timer.colorIndex) { timerColors[0] }

    val progress by animateFloatAsState(
        targetValue = if (timer.durationMs > 0) {
            remainingMs.toFloat() / timer.durationMs.toFloat()
        } else 0f,
        animationSpec = tween(100),
        label = "progress"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (timer.state) {
            TimerState.Done -> MaterialTheme.colorScheme.primaryContainer
            TimerState.Running -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "card_bg"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (timer.state == TimerState.Running) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Color indicator + Label + Edit button + State badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(timerColor)
                )
                Spacer(Modifier.width(12.dp))

                // Label and running status
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timer.label.ifEmpty { "Timer" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Edit button
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(32.dp)
                                .semantics { contentDescription = "Edit timer" }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (timer.state == TimerState.Running) {
                        Text(
                            text = "Running",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // State badge
                when (timer.state) {
                    TimerState.Done -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Complete",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    TimerState.Paused -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Paused",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    else -> {}
                }
            }

            Spacer(Modifier.height(16.dp))

            // Time display - monospace for consistent width
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatTime(remainingMs),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    color = when (timer.state) {
                        TimerState.Done -> MaterialTheme.colorScheme.primary
                        TimerState.Running -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    }
                )
            }

            // Progress bar for running/paused timers
            if (timer.state != TimerState.Idle && timer.state != TimerState.Done) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = timerColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(Modifier.height(20.dp))

            // Controls
            TimerControls(
                state = timer.state,
                onStart = onStart,
                onPause = onPause,
                onReset = onReset,
                onDelete = onDelete
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
