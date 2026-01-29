package com.example.paralleltimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimerDialog(
    onDismiss: () -> Unit,
    onCreate: (label: String, colorIndex: Int, durationMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var label by rememberSaveable { mutableStateOf("") }
    var colorIndex by rememberSaveable { mutableIntStateOf(0) }
    var durationMs by rememberSaveable { mutableLongStateOf(5 * 60 * 1000L) } // 5 min default

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "New Timer",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(24.dp))

            // Label input
            OutlinedTextField(
                value = label,
                onValueChange = { if (it.length <= 20) label = it },
                label = { Text("Timer name (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("${label.length}/20") }
            )

            Spacer(Modifier.height(16.dp))

            // Color picker
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
            ColorPicker(
                selectedIndex = colorIndex,
                onColorSelected = { colorIndex = it }
            )

            Spacer(Modifier.height(24.dp))

            // Duration picker
            Text(
                text = "Duration",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
            DurationPicker(
                durationMs = durationMs,
                onDurationChange = { durationMs = it },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onCreate(label, colorIndex, durationMs)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
