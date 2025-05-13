package com.sougata.supplysync.util

import androidx.navigation.NavOptions
import com.google.firebase.Timestamp
import com.sougata.supplysync.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object Inputs {

    fun getAllMonthsName(): List<String> {
        return listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
    }

    fun getAllDaysNames(): List<String> {
        return listOf(
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        )
    }

    fun getRandomImageUrl(width: Int = 300, height: Int = 300): String {
        val num = Random.nextInt(1, 1001)

        return "https://picsum.photos/id/$num/$width/$height"
    }

}