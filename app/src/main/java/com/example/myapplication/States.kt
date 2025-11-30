package com.example.myapplication

import androidx.compose.ui.text.input.TextFieldValue

data class RegistrationState(
    var fio: TextFieldValue = TextFieldValue(""),
    var gender: String = "Мужской",
    var course: String = "1 курс",
    var difficulty: Float = 1f,
    var birthDate: Long = System.currentTimeMillis(),
    var showResult: Boolean = false
)

data class SettingsState(
    var speed: Float = 1f,
    var maxCockroaches: Float = 5f,
    var bonusInterval: Float = 10f,
    var roundDuration: Float = 60f
)