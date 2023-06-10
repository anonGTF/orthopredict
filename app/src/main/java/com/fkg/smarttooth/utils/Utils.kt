package com.fkg.smarttooth.utils

import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun Boolean.toGender(): String = if (this) "Laki-laki" else "Perempuan"

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun String.safeParseDouble(): Double = if (this.isEmpty()) 0.0 else this.toDouble()

    fun Double?.orZero() = this ?: 0.0

    fun Double.twoDecimalPlaces() = String.format("%.2f", this)

    fun Int?.orOne() = this ?: 1

    fun Boolean?.orFalse() = this ?: false

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun getTimeMillis() = System.currentTimeMillis().toString()

}