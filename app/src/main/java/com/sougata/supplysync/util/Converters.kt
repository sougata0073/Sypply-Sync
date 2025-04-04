package com.sougata.supplysync.util

import android.util.Log
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object Converters {
    fun numberToDouble(num: Number) = num.toDouble()

    fun numberToLong(number: Number) = number.toLong()

    fun numberToInt(number: Number) = number.toInt()


    fun numberToMoneyString(value: Number): String {

//        Log.d("ConvertersLog", value.toString())

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        return currencyFormat.format(value)

    }

    fun getYearMonthDateFromDateString(dateString: String): Triple<Int, Int, Int> {
        var year = 0
        var month = 0
        var date = 0

        if (dateString.isNotEmpty()) {
            val list = dateString.split('-', '/')
            if (list.size != 3) {
                throw Exception("Invalid date")
            } else {
                try {
                    year = list[2].toInt()
                    month = list[1].toInt() - 1
                    date = list[0].toInt()
                } catch (_: Exception) {
                    throw Exception("Invalid date")
                }
            }
        } else {
            val calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            date = calendar.get(Calendar.DAY_OF_MONTH)
        }

        return Triple(year, month, date)
    }

    fun getHourMinuteFromTimeString(timeString: String): Pair<Int, Int> {

        var hour = 0
        var minute = 0

        if(timeString.isNotEmpty()) {
            val list = timeString.split(':')
            if (list.size != 2) {
                throw Exception("Invalid time")
            } else {
                try {
                    hour = list[0].toInt()
                    minute = list[1].toInt()
                } catch (_: Exception) {
                    throw Exception("Invalid time")
                }
            }
        } else {
            val calendar = Calendar.getInstance()
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }

        return hour to minute
    }
}