package com.animeplayer.app.ui.audio

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.animeplayer.app.player.audio.AudioPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsSheet(
    onDismiss: () -> Unit,
    viewModel: AudioViewModel = hiltViewModel()
) {
    val state by viewModel.audioState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Audio Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Global Toggles & Effects
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Equalizer")
                Switch(
                    checked = state.isEqEnabled,
                    onCheckedChange = { viewModel.toggleEq(it) }
                )
            }
            
            // Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(AudioPresets.MOVIE, AudioPresets.ANIME, AudioPresets.FLAT).forEach { preset ->
                    FilterChip(
                        selected = false, // In a real app, track selected preset
                        onClick = { viewModel.applyPreset(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // EQ Bands
            if (state.eqBands.isNotEmpty() && state.isEqEnabled) {
                Text(
                    text = "Equalizer Bands",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.eqBands.forEach { band ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(60.dp)
                        ) {
                            Slider(
                                value = band.level.toFloat(),
                                onValueChange = { viewModel.setEqBandLevel(band.index, it.toInt().toShort()) },
                                valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                                modifier = Modifier
                                    .width(150.dp)
                                    .rotate(-90f)
                                    .height(60.dp)
                            )
                            Spacer(modifier = Modifier.height(60.dp)) // Spacing for rotated slider
                            Text(
                                text = "${band.centerFreq / 1000}Hz",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${band.level}mB",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bass Boost
            EffectControl(
                title = "Bass Boost",
                enabled = state.isBassEnabled,
                value = state.bassStrength.toFloat(),
                range = 0f..1000f,
                onToggle = { viewModel.toggleBass(it) },
                onValueChange = { viewModel.setBassStrength(it.toInt().toShort()) }
            )

            // Virtualizer
            EffectControl(
                title = "Surround (Virtualizer)",
                enabled = state.isVirtualizerEnabled,
                value = state.virtualizerStrength.toFloat(),
                range = 0f..1000f,
                onToggle = { viewModel.toggleVirtualizer(it) },
                onValueChange = { viewModel.setVirtualizerStrength(it.toInt().toShort()) }
            )
            
            // Loudness Enhancer
            EffectControl(
                title = "Loudness Enhancer",
                enabled = state.isLoudnessEnabled,
                value = state.loudnessGain.toFloat(),
                range = 0f..1000f, // Simplified range for UI
                onToggle = { viewModel.toggleLoudness(it) },
                onValueChange = { viewModel.setLoudnessGain(it.toInt()) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EffectControl(
    title: String,
    enabled: Boolean,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onToggle: (Boolean) -> Unit,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
        if (enabled) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range
            )
        }
    }
}
