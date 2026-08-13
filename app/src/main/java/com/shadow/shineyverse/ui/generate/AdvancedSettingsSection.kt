package com.shadow.shineyverse.ui.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AdvancedSettingsSection(
    state: GenerateUiState,
    onNegativeChange: (String) -> Unit,
    onWidthChange: (Int) -> Unit,
    onHeightChange: (Int) -> Unit,
    onStepsChange: (Int) -> Unit,
    onCfgChange: (Float) -> Unit,
    onSamplerChange: (String) -> Unit,
    onSchedulerChange: (String) -> Unit,
    onLengthChange: (Int) -> Unit,
    onFpsChange: (Int) -> Unit,
    onSeedChange: (Long) -> Unit,
    onRandomizeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Advanced settings", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.negative,
            onValueChange = onNegativeChange,
            label = { Text("Negative prompt") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IntField("Width", state.width, onWidthChange, Modifier.weight(1f))
            IntField("Height", state.height, onHeightChange, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IntField("Steps", state.steps, onStepsChange, Modifier.weight(1f))
            FloatField("CFG", state.cfg, onCfgChange, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.sampler,
                onValueChange = onSamplerChange,
                label = { Text("Sampler") },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.scheduler,
                onValueChange = onSchedulerChange,
                label = { Text("Scheduler") },
                modifier = Modifier.weight(1f),
            )
        }
        if (state.isVideoModel) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IntField("Length (frames)", state.length, onLengthChange, Modifier.weight(1f))
                IntField("FPS", state.fps, onFpsChange, Modifier.weight(1f))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.seed.toString(),
                onValueChange = { text -> text.toLongOrNull()?.let(onSeedChange) },
                label = { Text("Seed") },
                enabled = !state.randomizeSeed,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        Row {
            Checkbox(checked = state.randomizeSeed, onCheckedChange = onRandomizeToggle)
            Text("Randomize seed each run", modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun IntField(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { text -> text.toIntOrNull()?.let(onChange) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun FloatField(label: String, value: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { text -> text.toFloatOrNull()?.let(onChange) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}
