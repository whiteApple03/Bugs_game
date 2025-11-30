package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter // Added import
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar // Ensure Calendar is imported
import java.util.Date // Might be needed for System.currentTimeMillis() if not already covered

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationTab(
    state: RegistrationState,
    onStateChange: (RegistrationState) -> Unit
) {
    val initialCalendar = Calendar.getInstance().apply { timeInMillis = state.birthDate }
    val zodiac = getZodiacSign(initialCalendar)
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.birthDate,
        initialDisplayMode = DisplayMode.Picker,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }

            override fun isSelectableYear(year: Int): Boolean {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                return year <= currentYear
            }
        }
    )

    LaunchedEffect(state.birthDate) {
        if (datePickerState.selectedDateMillis != state.birthDate) {
            datePickerState.selectedDateMillis = state.birthDate
        }
    }

    val formattedDate = remember(state.birthDate) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(state.birthDate),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.fio,
                onValueChange = { onStateChange(state.copy(fio = it)) },
                label = { Text("ФИО") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.fio.text.isBlank() && state.showResult
            )
            if (state.fio.text.isBlank() && state.showResult) {
                Text("Поле ФИО не может быть пустым", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            Text("Пол:")
            val genderOptions = listOf("Мужской", "Женский")
            Row {
                genderOptions.forEach { option ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (state.gender == option),
                                onClick = { onStateChange(state.copy(gender = option)) }
                            )
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (state.gender == option),
                            onClick = { onStateChange(state.copy(gender = option)) }
                        )
                        Text(option)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Курс:")
            var expanded by remember { mutableStateOf(false) }
            val courseOptions = listOf("1 курс", "2 курс", "3 курс", "4 курс")
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(state.course)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    courseOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onStateChange(state.copy(course = option))
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Уровень сложности: ${state.difficulty.toInt()}")
            Slider(
                value = state.difficulty,
                onValueChange = { onStateChange(state.copy(difficulty = it)) },
                valueRange = 1f..10f,
                steps = 8
            )
            Spacer(Modifier.height(8.dp))
            Text("Дата рождения:")
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formattedDate)
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onStateChange(state.copy(birthDate = it))
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Отмена")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = true
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Знак зодиака: ${zodiac.displayName}")
            Image(
                painter = painterResource(id = zodiac.imageRes),
                contentDescription = zodiac.displayName,
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(LocalContentColor.current)
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (state.fio.text.isNotBlank()) {
                    onStateChange(state.copy(showResult = true))
                    val player = Player(
                        fio = state.fio.text,
                        gender = state.gender,
                        course = state.course,
                        difficulty = state.difficulty.toInt(),
                        birthDate = state.birthDate,
                        zodiac = zodiac
                    )
                    Toast.makeText(context, "Игрок ${player.fio} зарегистрирован", Toast.LENGTH_LONG).show()

                } else {
                    Toast.makeText(context, "Пожалуйста, заполните ФИО", Toast.LENGTH_SHORT).show()
                    onStateChange(state.copy(showResult = true))
                }
            }) {
                Text("Зарегистрироваться")
            }
            if (state.showResult && state.fio.text.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "ФИО: ${state.fio.text}\n" +
                            "Пол: ${state.gender}\n" +
                            "Курс: ${state.course}\n" +
                            "Сложность: ${state.difficulty.toInt()}\n" +
                            "Дата рождения: $formattedDate\n" +
                            "Знак зодиака: ${zodiac.displayName}"
                )
            }
        }
    }
}
