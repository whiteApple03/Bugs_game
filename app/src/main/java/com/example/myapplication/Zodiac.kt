package com.example.myapplication

import java.util.Calendar

enum class ZodiacSign(val displayName: String, val imageRes: Int) {
    Aries("Овен", R.drawable.aries),
    Taurus("Телец", R.drawable.taurus),
    Gemini("Близнецы", R.drawable.gemini),
    Cancer("Рак", R.drawable.cancer),
    Leo("Лев", R.drawable.leo),
    Virgo("Дева", R.drawable.virgo),
    Libra("Весы", R.drawable.libra),
    Scorpio("Скорпион", R.drawable.scorpio),
    Sagittarius("Стрелец", R.drawable.sagittarius),
    Capricorn("Козерог", R.drawable.capricorn),
    Aquarius("Водолей", R.drawable.aquarius),
    Pisces("Рыбы", R.drawable.pisces)
}

fun getZodiacSign(date: Calendar): ZodiacSign {
    val day = date.get(Calendar.DAY_OF_MONTH)
    val month = date.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
    return when {
        (month == 3 && day >= 21) || (month == 4 && day <= 19) -> ZodiacSign.Aries
        (month == 4 && day >= 20) || (month == 5 && day <= 20) -> ZodiacSign.Taurus
        (month == 5 && day >= 21) || (month == 6 && day <= 20) -> ZodiacSign.Gemini
        (month == 6 && day >= 21) || (month == 7 && day <= 22) -> ZodiacSign.Cancer
        (month == 7 && day >= 23) || (month == 8 && day <= 22) -> ZodiacSign.Leo
        (month == 8 && day >= 23) || (month == 9 && day <= 22) -> ZodiacSign.Virgo
        (month == 9 && day >= 23) || (month == 10 && day <= 22) -> ZodiacSign.Libra
        (month == 10 && day >= 23) || (month == 11 && day <= 21) -> ZodiacSign.Scorpio
        (month == 11 && day >= 22) || (month == 12 && day <= 21) -> ZodiacSign.Sagittarius
        (month == 12 && day >= 22) || (month == 1 && day <= 19) -> ZodiacSign.Capricorn
        (month == 1 && day >= 20) || (month == 2 && day <= 18) -> ZodiacSign.Aquarius
        else -> ZodiacSign.Pisces
    }
}