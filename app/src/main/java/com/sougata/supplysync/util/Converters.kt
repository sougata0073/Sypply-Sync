package com.sougata.supplysync.util

import com.google.firebase.Timestamp
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale


//fun main() {
//
//
//
//}


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
                    month = list[1].toInt()
                    date = list[0].toInt()
                } catch (_: Exception) {
                    throw Exception("Invalid date")
                }
            }
        } else {
            val calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH) + 1
            date = calendar.get(Calendar.DAY_OF_MONTH)
        }

        return Triple(year, month, date)
    }

    fun getHourMinuteFromTimeString(timeString: String): Pair<Int, Int> {

        var hour = 0
        var minute = 0

        if (timeString.isNotEmpty()) {
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

    fun getShortedNumberString(num: Double): String {
        val df = DecimalFormat("#.##")
        return when {
            num >= 1_00_00_000 -> "${df.format(num / 1_00_00_000.0)} Cr"
            num >= 1_00_000 -> "${df.format(num / 1_00_000.0)} L"
            num >= 1_000 -> "${df.format(num / 1_000.0)} K"
            else -> num.toString()
        }
    }

    fun getDateFromTimestamp(timestamp: Timestamp): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.time = timestamp.toDate()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val date = calendar.get(Calendar.DAY_OF_MONTH)

        return Triple(year, month, date)
    }

    fun getTimeFromTimestamp(timestamp: Timestamp): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.time = timestamp.toDate()

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return hour to minute
    }

    fun getTimestampFromDataTime(
        year: Int,
        month: Int,
        date: Int,
        hour: Int,
        minute: Int
    ): Timestamp {

        val calendar = Calendar.getInstance()

        calendar.set(year, month - 1, date, hour, minute)

        return Timestamp(calendar.time)
    }

    fun getTimestampFromDate(
        year: Int,
        month: Int,
        date: Int
    ): Timestamp {

        val calendar = Calendar.getInstance()

        calendar.set(year, month - 1, date)

        return Timestamp(calendar.time)
    }
}

//fun main() {
//    print(getShortedNumberString(100000001.0))
//}