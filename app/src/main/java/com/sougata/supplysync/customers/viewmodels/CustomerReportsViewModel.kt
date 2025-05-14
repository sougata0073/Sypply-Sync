package com.sougata.supplysync.customers.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.pdf.PdfRepository
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.launch

class CustomerReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val customerRepo = CustomerRepository()
    private val pdfRepository = PdfRepository()

    val paymentsReceivedListPdf = MutableLiveData<Triple<Status, ByteArray?, String>>()
    val salesListPdf = MutableLiveData<Triple<Status, ByteArray?, String>>()

    val receivedOrdersItemsCompChartData = MutableLiveData<Triple<BarData?, Status, String>>()
    val receivedOrdersItemsCompChartXStrings = mutableListOf<String>()
    var animateReceivedOrdersItemsCompChart = true
    var receivedOrdersItemsCompChartDateRange = ""

    var pdfByteArray = byteArrayOf()

    init {
        this.loadPast30DaysReceivedOrdersItemsCompChart()
    }

    fun generatePaymentsReceivedPdf(startDateMillis: Long, endDateMillis: Long) {
        this.paymentsReceivedListPdf.value = Triple(Status.STARTED, null, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.customerRepo.getPaymentsReceivedByRange(
            startTimestamp, endTimestamp
        ) { status, list, message ->
            if (status == Status.SUCCESS) {
                this.viewModelScope.launch {
                    pdfRepository.generatePaymentsReceivedPdf(
                        list!!,
                    ) { status, byteArray, message ->
                        paymentsReceivedListPdf.value = Triple(status, byteArray, message)
                    }
                }
            }
        }
    }

    fun generateSalesListPdf(startDateMillis: Long, endDateMillis: Long) {
        this.salesListPdf.value = Triple(Status.STARTED, null, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.customerRepo.getDeliveredOrdersByRange(
            startTimestamp,
            endTimestamp
        ) { status, list, message ->
            if (status == Status.SUCCESS) {
                this.viewModelScope.launch {
                    pdfRepository.generateSalesPdf(
                        list!!
                    ) { status, byteArray, message ->
                        salesListPdf.value = Triple(status, byteArray, message)
                    }
                }
            }
        }
    }

    fun loadReceivedOrdersItemsCompChartData(startDateMillis: Long, endDateMillis: Long) {
        this.receivedOrdersItemsCompChartData.value = Triple(null, Status.STARTED, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.customerRepo.getReceivedOrdersItemsFrequencyByRange(
            startTimestamp,
            endTimestamp,
        ) { status, list, message ->
            if (status == Status.FAILED) {
                this.receivedOrdersItemsCompChartData.value = Triple(null, status, message)
            } else {
                val barEntryList = mutableListOf<BarEntry>()

                for ((i, value) in list!!.withIndex()) {
                    barEntryList.add(BarEntry(i.toFloat(), value.second.toFloat()))
                    receivedOrdersItemsCompChartXStrings.add(value.first)
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

                this.receivedOrdersItemsCompChartDateRange =
                    "From: $startDateString To: $endDateString"

                this.animateReceivedOrdersItemsCompChart = true
                this.receivedOrdersItemsCompChartData.value = Triple(barData, status, message)
            }
        }
    }

    fun loadPast30DaysReceivedOrdersItemsCompChart() {
        val startDateMillis = DateTime.getPastDateInMillis(30)
        val endDateMillis = DateTime.getPastDateInMillis(0)

        this.loadReceivedOrdersItemsCompChartData(startDateMillis, endDateMillis)

    }

}