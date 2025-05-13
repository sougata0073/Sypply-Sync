package com.sougata.supplysync.home.viewmodels

import android.app.Application
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val supplierRepo = SupplierRepository()
    private val customerRepo = CustomerRepository()

    val purchaseChartData = MutableLiveData<Triple<LineData?, Status, String>>()
    var purchaseChartDateRange = ""
    var animatePurchaseChart = true

    val salesChartData = MutableLiveData<Triple<LineData?, Status, String>>()
    var salesChartDateRange = ""
    var animateSalesChart = true

    init {
        this.loadPast30DaysPurchaseChart()
        this.loadPast30DaysSaleChart()
    }

    fun loadPast30DaysPurchaseChart() {

        val startDateMillis = DateTime.getPastDateInMillis(30)
        val endDateMillis = DateTime.getPastDateInMillis(0)

        this.loadPurchaseLineChartData(startDateMillis, endDateMillis)
    }

    fun loadPast30DaysSaleChart() {
        val startDateMillis = DateTime.getPastDateInMillis(30)
        val endDateMillis = DateTime.getPastDateInMillis(0)

        this.loadSalesLineChartData(startDateMillis, endDateMillis)
    }

    fun loadPurchaseLineChartData(startDateMillis: Long, endDateMillis: Long) {
        this.purchaseChartData.value = Triple(null, Status.STARTED, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.supplierRepo.getPurchaseAmountListByRange(
            startTimestamp,
            endTimestamp
        ) { status, list, message ->
            if (status == Status.FAILED) {
                this.purchaseChartData.value = Triple(null, status, message)
            } else {

                val lineDataSet =
                    this.getDecoratedLineDataset(
                        list!!,
                        R.color.primary_color,
                        R.drawable.line_chart_gradient_normal
                    )

                val lineData = LineData().apply { addDataSet(lineDataSet) }

                val startDateString = DateTime.getDateStringFromTimestamp(startTimestamp)
                val endDateString = DateTime.getDateStringFromTimestamp(endTimestamp)

                this.purchaseChartDateRange = "From: $startDateString To: $endDateString"

                this.animatePurchaseChart = true
                this.purchaseChartData.value = Triple(lineData, status, message)
            }
        }
    }

    fun loadSalesLineChartData(startDateMillis: Long, endDateMillis: Long) {
        this.salesChartData.value = Triple(null, Status.STARTED, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.customerRepo.getSalesAmountListByRange(
            startTimestamp,
            endTimestamp
        ) { status, list, message ->
            if (status == Status.FAILED) {
                this.salesChartData.value = Triple(null, status, message)
            } else {

                val lineDataSet =
                    this.getDecoratedLineDataset(
                        list!!,
                        R.color.green_profit,
                        R.drawable.line_chart_gradient_profit
                    )

                val lineData = LineData().apply { addDataSet(lineDataSet) }

                val startDateString = DateTime.getDateStringFromTimestamp(startTimestamp)
                val endDateString = DateTime.getDateStringFromTimestamp(endTimestamp)

                this.salesChartDateRange = "From: $startDateString To: $endDateString"

                this.animateSalesChart = true
                this.salesChartData.value = Triple(lineData, status, message)
            }
        }
    }

    private fun getDecoratedLineDataset(
        dataList: List<Double>,
        chartColorId: Int,
        gradientDrawableId: Int
    ): LineDataSet {
        val app = getApplication<Application>()

        val entryList = mutableListOf<Entry>()

        for ((i, value) in dataList.withIndex()) {
            entryList.add(Entry(i.toFloat(), value.toFloat()))
        }

        return LineDataSet(entryList, "Amount").apply {
            color = app.getColor(chartColorId)
            setDrawFilled(true)
            setDrawCircles(false)
            setDrawValues(false)
            valueTextColor = app.getColor(R.color.bw)
            valueTextSize = 11f
            mode = LineDataSet.Mode.CUBIC_BEZIER

            fillDrawable =
                AppCompatResources.getDrawable(app, gradientDrawableId)
        }
    }

}