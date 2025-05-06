package com.sougata.supplysync.util

import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTime {

    private val calendar = Calendar.getInstance()

    fun getTimestampFromMillis(millis: Long): Timestamp {
        return Timestamp(Date(millis))
    }

    fun getMillisFromDate(year: Int, month: Int, date: Int): Long {
        this.calendar.set(year, month - 1, date)
        return this.calendar.timeInMillis
    }

    fun getDateFromDateString(dateString: String): Triple<Int, Int, Int> {
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

    fun getTimeFromTimeString(timeString: String): Pair<Int, Int> {
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

    fun getTimestampFromDate(
        year: Int,
        month: Int,
        date: Int
    ): Timestamp {

        this.calendar.set(year, month - 1, date)

        return Timestamp(calendar.time)
    }

    fun getTimestampFromDataTime(
        year: Int,
        month: Int,
        date: Int,
        hour: Int,
        minute: Int
    ): Timestamp {

        this.calendar.set(year, month - 1, date, hour, minute)

        return Timestamp(this.calendar.time)
    }

    fun getTimestampFromDateString(dateString: String): Timestamp {
        val date = this.getDateFromDateString(dateString)
        return this.getTimestampFromDate(date.first, date.second, date.third)
    }

    fun getTimestampFromDateTimeString(dateString: String, timeString: String): Timestamp {
        val date = this.getDateFromDateString(dateString)
        val time = this.getTimeFromTimeString(timeString)

        return getTimestampFromDataTime(
            date.first,
            date.second,
            date.third,
            time.first,
            time.second
        )
    }

    fun getDateFromTimestamp(timestamp: Timestamp): Triple<Int, Int, Int> {
        this.calendar.time = timestamp.toDate()

        val year = this.calendar.get(Calendar.YEAR)
        val month = this.calendar.get(Calendar.MONTH) + 1
        val date = this.calendar.get(Calendar.DAY_OF_MONTH)

        return Triple(year, month, date)
    }

    fun getTimeFromTimestamp(timestamp: Timestamp): Pair<Int, Int> {
        this.calendar.time = timestamp.toDate()

        val hour = this.calendar.get(Calendar.HOUR_OF_DAY)
        val minute = this.calendar.get(Calendar.MINUTE)

        return hour to minute
    }

    fun getDateStringFromTimestamp(timestamp: Timestamp): String {
        val date = getDateFromTimestamp(timestamp)

        return getDateStringFromDate(date.first, date.second, date.third)
    }

    fun getTimeStringFromTimestamp(timestamp: Timestamp): String {
        val time = getTimeFromTimestamp(timestamp)

        return getTimeStringFromTime(time.first, time.second)
    }

    fun getCalculatedDate(
        n: Int,
        year: Int,
        month: Int,
        date: Int
    ): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()

        calendar.set(year, month - 1, date)

        calendar.add(Calendar.DAY_OF_MONTH, n)

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val date = calendar.get(Calendar.DAY_OF_MONTH)

        return Triple(year, month, date)
    }

    fun getCurrentDate(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val date = calendar.get(Calendar.DAY_OF_MONTH)

        return Triple(year, month, date)
    }

    fun getDateStringFromDate(year: Int, month: Int, date: Int): String {
        return String.format(
            Locale.getDefault(),
            "%02d-%02d-%04d", date, month, year
        )
    }

    fun getTimeStringFromTime(hour: Int, minute: Int): String {
        return String.format(
            Locale.getDefault(),
            "%02d:%02d", hour, minute
        )
    }


}