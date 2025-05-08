package com.sougata.supplysync.suppliers.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.pdf.SupplierPdfRepository
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.launch

class SupplierReportsViewModel(application: Application) : AndroidViewModel(application) {

    // This one is for ui
    val purchasedItemsCompChartRangeDate = MutableLiveData("")

    private val supplierRepository = SupplierRepository()
    private val supplierPdfRepository = SupplierPdfRepository()

    val supplierPaymentsListPdf = MutableLiveData<Triple<Int, ByteArray?, String>>()
    val orderedItemsListPdf = MutableLiveData<Triple<Int, ByteArray?, String>>()

    val purchasedItemsCompChartData = MutableLiveData<Triple<BarData?, Int, String>>()
    val purchasedItemsCompChartXStrings = mutableListOf<String>()
    var animatePurchasedItemsCompChart = true

    var pdfByteArray = byteArrayOf()

    init {
        this.loadPast30DaysPurchasedItemsCompChart()
    }

    fun generateSupplierPaymentsPdf(startDateMillis: Long, endDateMillis: Long) {
        this.supplierPaymentsListPdf.value = Triple(Status.STARTED, null, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.supplierRepository.getSupplierPaymentsByRange(
            startTimestamp, endTimestamp
        ) { status, list, message ->
            if (status == Status.SUCCESS) {

                this.viewModelScope.launch {
                    supplierPdfRepository.generateSupplierPaymentsPdf(
                        list,
                    ) { status, byteArray, message ->
                        supplierPaymentsListPdf.value = Triple(status, byteArray, message)
                    }
                }

            }
        }
    }

    fun generateOrderedItemsPdf(startDateMillis: Long, endDateMillis: Long) {
        this.orderedItemsListPdf.value = Triple(Status.STARTED, null, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.supplierRepository.getOrderedItemsByRange(
            startTimestamp,
            endTimestamp
        ) { status, list, message ->
            if (status == Status.SUCCESS) {
                this.viewModelScope.launch {
                    supplierPdfRepository.generateOrderedItemsPdf(
                        list,
                    ) { status, byteArray, message ->
                        orderedItemsListPdf.value = Triple(status, byteArray, message)
                    }

                }
            }
        }
    }

    fun loadPurchasedItemsCompChartData(startDateMillis: Long, endDateMillis: Long) {
        this.purchasedItemsCompChartData.value = Triple(null, Status.STARTED, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.supplierRepository.getFrequencyOfOrderedItemsByRange(
            startTimestamp,
            endTimestamp,
        ) { status, list, message ->
            if (status == Status.FAILED) {

                this.purchasedItemsCompChartData.value = Triple(null, status, message)

            } else {
                val barEntryList = mutableListOf<BarEntry>()

                for ((i, value) in list.withIndex()) {
                    barEntryList.add(BarEntry(i.toFloat(), value.second.toFloat()))
                    purchasedItemsCompChartXStrings.add(value.first)
                }

                val app = getApplication<Application>()

                val barDataSet = BarDataSet(barEntryList, "Comparison").apply {
                    colors = ColorTemplate.JOYFUL_COLORS.toList()
                    valueTextColor = app.getColor(R.color.bw)
                    valueTextSize = 11f
                }

                val barData = BarData().apply {
                    addDataSet(barDataSet)
                }

                val startDateString = DateTime.getDateStringFromTimestamp(startTimestamp)
                val endDateString = DateTime.getDateStringFromTimestamp(endTimestamp)

                this.purchasedItemsCompChartRangeDate.value =
                    "From: $startDateString To: $endDateString"

                this.animatePurchasedItemsCompChart = true
                this.purchasedItemsCompChartData.value = Triple(barData, status, message)
            }
        }
    }

    fun loadPast30DaysPurchasedItemsCompChart() {

        val currentDate = DateTime.getCurrentDate()
        var endYear = currentDate.first
        var endMonth = currentDate.second
        var endDate = currentDate.third

        val pastDate = DateTime.getCalculatedDate(-30, endYear, endMonth, endDate)
        var startYear = pastDate.first
        var startMonth = pastDate.second
        var startDate = pastDate.third

        val startDateMillis = DateTime.getMillisFromDate(startYear, startMonth, startDate)
        val endDateMillis = DateTime.getMillisFromDate(endYear, endMonth, endDate)

        this.loadPurchasedItemsCompChartData(startDateMillis, endDateMillis)
    }

}