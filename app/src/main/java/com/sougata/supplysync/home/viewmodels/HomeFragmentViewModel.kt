package com.sougata.supplysync.home.viewmodels

import android.app.Application
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.Timestamp
import com.sougata.supplysync.R
import com.sougata.supplysync.firebase.FirestoreRepository
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepository = FirestoreRepository()

//    fun getBarChartData(context: Context): BarData {
//        val list = listOf(
//            BarEntry(0f, 10f), BarEntry(1f, 20f), BarEntry(2f, 30f), BarEntry(3f, 40f),
//            BarEntry(4f, 50f), BarEntry(5f, 60f), BarEntry(6f, 70f)
//        )
//
//        val barDataSet = BarDataSet(list, "Amount").apply {
//            color = context.getColor(R.color.primary_color)
//            valueTextColor = context.getColor(R.color.bw)
//            valueTextSize = 11f
//        }
//
//        return BarData().apply { addDataSet(barDataSet) }
//    }

    val purchaseChartRangeDate = MutableLiveData("")

    val purchaseChartData = MutableLiveData<Triple<LineData?, Int, String>>()

    init {
        this.loadThisMonthsPurchaseChart()
    }

    fun loadThisMonthsPurchaseChart() {

        val calendar = Calendar.getInstance()

        var startYear = calendar.get(Calendar.YEAR)
        var startMonth = calendar.get(Calendar.MONTH) + 1
        var startDate = 1
        var endYear = startYear
        var endMonth = startMonth + 1
        var endDate = 1

        this.loadPurchaseLineChartData(
            "$startDate-$startMonth-$startYear",
            "$endDate-$endMonth-$endYear"
        )

        this.purchaseChartRangeDate.postValue(
            String.format(
                Locale.getDefault(),
                "From: %02d-%02d-%04d To: %02d-%02d-%04d",
                startDate,
                startMonth,
                startYear,
                endDate,
                endMonth,
                endYear
            )
        )
    }


    fun loadPurchaseLineChartData(startDateString: String, endDateString: String) {

        var startTimestamp: Timestamp
        var endTimestamp: Timestamp

        try {
            val startDate = Converters.getYearMonthDateFromDateString(startDateString)
            val endDate = Converters.getYearMonthDateFromDateString(endDateString)

            startTimestamp = Converters.getTimestampFromDate(
                startDate.first,
                startDate.second,
                startDate.third
            )
            endTimestamp = Converters.getTimestampFromDate(
                endDate.first,
                endDate.second,
                endDate.third
            )

        } catch (_: Exception) {
            this.purchaseChartData.postValue(
                Triple(
                    null,
                    Status.FAILED,
                    "Invalid date"
                )
            )
            return
        }

        this.purchaseChartData.postValue(Triple(null, Status.STARTED, ""))

        this.firestoreRepository.getPurchaseAmountByRange(
            startTimestamp,
            endTimestamp,
            viewModelScope
        ) { status, list, message ->

            if (status == Status.FAILED) {
                this.purchaseChartData.postValue(Triple(null, status, message))
            } else {
//                Log.d("list", list.toString())
                viewModelScope.launch(Dispatchers.IO) {
                    val entryList = mutableListOf<Entry>()

                    for ((i, value) in list.withIndex()) {
                        val y = value.toFloat()
                        entryList.add(Entry(i.toFloat(), y))
                    }

                    val app = getApplication<Application>()

                    val lineDataSet = LineDataSet(entryList, "Amount").apply {
                        color = app.getColor(R.color.primary_color)
                        setDrawFilled(true)
                        fillColor = app.getColor(R.color.primary_color)
                        setDrawCircles(false)
                        setDrawValues(false)
                        valueTextColor = app.getColor(R.color.bw)
                        valueTextSize = 11f
                        mode = LineDataSet.Mode.CUBIC_BEZIER

                        fillDrawable =
                            AppCompatResources.getDrawable(app, R.drawable.line_chart_gradient_bg)
                    }

                    val lineData = LineData().apply { addDataSet(lineDataSet) }

                    purchaseChartData.postValue(Triple(lineData, status, message))

                }

            }
        }
    }


//    fun getLineChartData(context: Context): LineData {
//
//        val list = listOf(
//            Entry(0f, 10F), Entry(1f, 20f), Entry(2f, 5f), Entry(3f, 15f),
//            Entry(4f, 50f), Entry(5f, 30f), Entry(6f, 25f), Entry(7f, 40f),
//            Entry(8f, 69f), Entry(9f, 75f), Entry(10f, 50f), Entry(11f, 60f),
//            Entry(12f, 70f), Entry(13f, 80f), Entry(14f, 90f), Entry(15f, 100f)
//        )
//
//        val lineDataSet = LineDataSet(list, "Amount").apply {
//            color = context.getColor(R.color.primary_color)
//            setDrawFilled(true)
//            fillColor = context.getColor(R.color.primary_color)
//            setDrawCircles(false)
//            setDrawValues(false)
//            valueTextColor = context.getColor(R.color.bw)
//            valueTextSize = 11f
//            mode = LineDataSet.Mode.CUBIC_BEZIER
//
//            fillDrawable =
//                AppCompatResources.getDrawable(context, R.drawable.line_chart_gradient_bg)
//        }
//
//        return LineData().apply { addDataSet(lineDataSet) }
//
//    }

}