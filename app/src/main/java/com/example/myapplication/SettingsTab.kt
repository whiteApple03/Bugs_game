package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsTab(
    state: SettingsState,
    onStateChange: (SettingsState) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Настройки игры", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            Text("Скорость жуков: ${state.speed.toInt()}")
            Slider(
                value = state.speed,
                onValueChange = { onStateChange(state.copy(speed = it)) },
                valueRange = 1f..10f,
                steps = 8
            )
            Spacer(Modifier.height(8.dp))

            Text("Максимальное количество жуков: ${state.maxCockroaches.toInt()}")
            Slider(
                value = state.maxCockroaches,
                onValueChange = { onStateChange(state.copy(maxCockroaches = it)) },
                valueRange = 1f..20f,
                steps = 18
            )
            Spacer(Modifier.height(8.dp))

            Text("Интервал появления бонусов (сек): ${state.bonusInterval.toInt()}")
            Slider(
                value = state.bonusInterval,
                onValueChange = { onStateChange(state.copy(bonusInterval = it)) },
                valueRange = 5f..30f,
                steps = 24
            )
            Spacer(Modifier.height(8.dp))

            Text("Продолжительность раунда (сек): ${state.roundDuration.toInt()}")
            Slider(
                value = state.roundDuration,
                onValueChange = { onStateChange(state.copy(roundDuration = it)) },
                valueRange = 30f..120f,
                steps = 17
            )
        }
    }
}
