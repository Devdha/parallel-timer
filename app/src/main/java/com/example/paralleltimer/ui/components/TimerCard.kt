package com.example.paralleltimer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paralleltimer.R
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
            TimerState.Running -> MaterialTheme.colorScheme.surface
            TimerState.Paused -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        label = "card_bg"
    )

    // Pulse animation for running state
    val infiniteTransition = rememberInfiniteTransition(label = "running_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                when (timer.state) {
                    TimerState.Running -> Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                timerColor.copy(alpha = pulseAlpha),
                                timerColor.copy(alpha = pulseAlpha * 0.5f)
                            )
                        ),
                        shape = cardShape
                    )
                    TimerState.Done -> Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = cardShape
                    )
                    else -> Modifier
                }
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when (timer.state) {
                TimerState.Running -> 8.dp
                TimerState.Done -> 4.dp
                else -> 2.dp
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Label + Edit + State badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color indicator - larger for running
                Box(
                    modifier = Modifier
                        .size(if (timer.state == TimerState.Running) 12.dp else 10.dp)
                        .clip(CircleShape)
                        .background(timerColor)
                        .then(
                            if (timer.state == TimerState.Running) {
                                Modifier.alpha(pulseAlpha)
                            } else Modifier
                        )
                )
                Spacer(Modifier.width(10.dp))

                // Label
                val timerLabel = stringResource(R.string.timer)
                Text(
                    text = timer.label.ifEmpty { timerLabel },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = when (timer.state) {
                        TimerState.Done -> MaterialTheme.colorScheme.onPrimaryContainer
                        TimerState.Paused -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )

                // Edit button - subtle
                val editTimerDesc = stringResource(R.string.edit_timer)
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(28.dp)
                        .semantics { contentDescription = editTimerDesc }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                // State badge - minimal
                when (timer.state) {
                    TimerState.Done -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.complete),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    TimerState.Paused -> {
                        Text(
                            text = stringResource(R.string.paused),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {}
                }
            }

            Spacer(Modifier.height(12.dp))

            // Time display - tabular nums, center aligned
            Text(
                text = formatTime(remainingMs),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 52.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = when (timer.state) {
                    TimerState.Done -> MaterialTheme.colorScheme.onPrimaryContainer
                    TimerState.Paused -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Progress bar - visible for running/paused/done
            if (timer.state == TimerState.Running || timer.state == TimerState.Paused || timer.state == TimerState.Done) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { if (timer.state == TimerState.Done) 1f else progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = when (timer.state) {
                        TimerState.Done -> MaterialTheme.colorScheme.primary
                        TimerState.Running -> timerColor
                        else -> timerColor.copy(alpha = 0.5f)
                    },
                    trackColor = when (timer.state) {
                        TimerState.Done -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    },
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(Modifier.height(16.dp))

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
