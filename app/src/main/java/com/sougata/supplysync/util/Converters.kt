package com.sougata.supplysync.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object Converters {
    fun numberToDouble(num: Number) = num.toDouble()

    fun numberToLong(number: Number) = number.toLong()

    fun numberToInt(number: Number) = number.toInt()

    fun numberToMoneyString(value: Number?): String {

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        return if (value == null) {
            currencyFormat.format("0.00")
        } else {
            currencyFormat.format(value)
        }
    }

    fun getShortedNumberString(num: Double): String {
        val df = DecimalFormat("#.##")
        return when {
            num >= 1_00_00_000 -> "${df.format(num / 1_00_00_000.0)} Cr"
            num >= 1_00_000 -> "${df.format(num / 1_00_000.0)} L"
            num >= 1_000 -> "${df.format(num / 1_000.0)} K"
            else -> num.toString()
        }
    }

}
