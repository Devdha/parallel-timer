package com.example.paralleltimer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimerDialog(
    initialLabel: String,
    initialColorIndex: Int,
    onDismiss: () -> Unit,
    onSave: (label: String, colorIndex: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var label by rememberSaveable { mutableStateOf(initialLabel) }
    var colorIndex by rememberSaveable { mutableIntStateOf(initialColorIndex) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Edit Timer",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(24.dp))

            // Label input
            OutlinedTextField(
                value = label,
                onValueChange = { if (it.length <= 20) label = it },
                label = { Text("Timer name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { onSave(label, colorIndex) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
