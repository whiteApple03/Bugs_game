package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fio: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: Long,
    val zodiac: String
)