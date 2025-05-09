package com.sougata.supplysync.home.viewmodels

import android.app.Application
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val supplierRepository = SupplierRepository()

    var purchaseChartDateRange = ""

    val purchaseChartData = MutableLiveData<Triple<LineData?, Int, String>>()
    var animatePurchaseChart = true

    val numberOfOrdersToReceive = MutableLiveData<Triple<Number, Int, String>>()

    init {
        this.loadPast30DaysPurchaseChart()
        this.loadOrdersToReceive()
    }

    fun loadPast30DaysPurchaseChart() {

        val startDateMillis = DateTime.getPastDateInMillis(30)
        val endDateMillis = DateTime.getPastDateInMillis(0)

        this.loadPurchaseLineChartData(startDateMillis, endDateMillis)
    }

    fun loadOrdersToReceive() {
        this.numberOfOrdersToReceive.postValue(Triple(0, Status.STARTED, ""))

        this.supplierRepository.getOrdersToReceive { status, count, message ->
            this.numberOfOrdersToReceive.postValue(Triple(count, status, message))
        }
    }

    fun loadPurchaseLineChartData(startDateMillis: Long, endDateMillis: Long) {
        this.purchaseChartData.value = Triple(null, Status.STARTED, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.supplierRepository.getPurchaseAmountsListByRange(
            startTimestamp,
            endTimestamp
        ) { status, list, message ->

            if (status == Status.FAILED) {
                this.purchaseChartData.postValue(Triple(null, status, message))
            } else {
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

                val startDateString = DateTime.getDateStringFromTimestamp(startTimestamp)
                val endDateString = DateTime.getDateStringFromTimestamp(endTimestamp)

                this.purchaseChartDateRange = "From: $startDateString To: $endDateString"

                this.animatePurchaseChart = true
                this.purchaseChartData.value = Triple(lineData, status, message)
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