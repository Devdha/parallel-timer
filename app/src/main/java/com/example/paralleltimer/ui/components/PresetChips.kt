package com.example.paralleltimer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.paralleltimer.domain.model.TimerPreset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetChips(
    presets: List<TimerPreset>,
    onPresetClick: (TimerPreset) -> Unit,
    onPresetLongClick: (TimerPreset) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(presets, key = { "${it.label}_${it.durationMs}" }) { preset ->
            AssistChip(
                onClick = { onPresetClick(preset) },
                label = {
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = null,
                modifier = Modifier
                    .height(40.dp)
                    .combinedClickable(
                        onClick = { onPresetClick(preset) },
                        onLongClick = { onPresetLongClick(preset) }
                    )
                    .semantics { contentDescription = "Create ${preset.label} timer. Long press to delete." }
            )
        }

        // Add preset chip
        item {
            AssistChip(
                onClick = onAddClick,
                label = {
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = null,
                modifier = Modifier
                    .height(40.dp)
                    .semantics { contentDescription = "Add new preset" }
            )
        }
    }
}
