package com.example.paralleltimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DurationPicker(
    durationMs: Long,
    onDurationChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (durationMs / 1000).toInt()
    var minutes by remember(durationMs) { mutableStateOf((totalSeconds / 60).toString()) }
    var seconds by remember(durationMs) { mutableStateOf((totalSeconds % 60).toString()) }

    fun updateDuration() {
        val mins = minutes.toIntOrNull() ?: 0
        val secs = seconds.toIntOrNull() ?: 0
        val newDurationMs = ((mins * 60 + secs) * 1000L).coerceAtLeast(1000L)
        onDurationChange(newDurationMs)
    }

    Row(
        modifier = modifier,
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
            modifier = Modifier.width(80.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            label = { Text("Min") }
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
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
            modifier = Modifier.width(80.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            label = { Text("Sec") }
        )
    }
}
