package com.tsongkha.spinnerdatepicker

import java.util.*

object DateUtils {
    fun getCalendar(): Calendar {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.time = Date()
        return cal
    }

    fun timeInMillis(): Long {
        val cal = getCalendar()
        return cal.timeInMillis
    }
}