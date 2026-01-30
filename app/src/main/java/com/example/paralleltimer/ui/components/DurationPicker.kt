package com.example.paralleltimer.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paralleltimer.R

private val quickPresets = listOf(
    1L * 60 * 1000 to "1m",
    3L * 60 * 1000 to "3m",
    5L * 60 * 1000 to "5m",
    10L * 60 * 1000 to "10m",
    15L * 60 * 1000 to "15m",
    30L * 60 * 1000 to "30m"
)

@Composable
fun DurationPicker(
    durationMs: Long,
    onDurationChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (durationMs / 1000).toInt()
    var minutes by remember(durationMs) { mutableStateOf((totalSeconds / 60).toString()) }
    var seconds by remember(durationMs) { mutableStateOf((totalSeconds % 60).toString().padStart(2, '0')) }

    fun updateDuration() {
        val mins = minutes.toIntOrNull() ?: 0
        val secs = seconds.toIntOrNull() ?: 0
        val newDurationMs = ((mins * 60 + secs) * 1000L).coerceAtLeast(1000L)
        onDurationChange(newDurationMs)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Quick preset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPresets.forEach { (ms, label) ->
                val isSelected = durationMs == ms
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "preset_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                            } else Modifier
                        )
                        .clickable { onDurationChange(ms) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Custom input
        Text(
            text = stringResource(R.string.or_set_custom),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = minutes,
                onValueChange = {
                    if (it.length <= 3 && it.all { c -> c.isDigit() }) {
                        minutes = it
                        updateDuration()
                    }
                },
                modifier = Modifier.width(100.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                label = { Text(stringResource(R.string.minutes_label)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Text(
                text = ":",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            OutlinedTextField(
                value = seconds,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        val secs = it.toIntOrNull() ?: 0
                        if (secs < 60) {
                            seconds = it
                            updateDuration()
                        }
                    }
                },
                modifier = Modifier.width(100.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                label = { Text(stringResource(R.string.seconds_label)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }
    }
}
