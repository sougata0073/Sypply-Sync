package com.sougata.supplysync.suppliers.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Timestamp
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.pdf.SupplierPdfRepository
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SupplierReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<Application>()

    // This one is for ui
    val purchasedItemsCompChartRangeDate = MutableLiveData("")

    private val supplierRepository = SupplierRepository()
    private val supplierPdfRepository = SupplierPdfRepository()

    val supplierPaymentsListPdf = MutableLiveData<Triple<Int, ByteArray?, String>>()
    val orderedItemsListPdf = MutableLiveData<Triple<Int, ByteArray?, String>>()

    val purchasedItemsCompChartData = MutableLiveData<Triple<BarData?, Int, String>>()
    val purchasedItemsCompChartXStrings = mutableListOf<String>()

    var pdfByteArray = byteArrayOf()

    init {
        this.loadThisMonthsPurchasedItemsCompChart()
    }

    fun generateSupplierPaymentsPdf(startDateString: String, endDateString: String) {
        this.supplierPaymentsListPdf.value = Triple(Status.STARTED, null, "")

        var startTimestamp: Timestamp
        var endTimestamp: Timestamp

        try {
            startTimestamp = Converters.getTimestampFromDateString(startDateString)
            endTimestamp = Converters.getTimestampFromDateString(endDateString)
        } catch (_: Exception) {
            this.supplierPaymentsListPdf.value = Triple(Status.FAILED, null, "Invalid date")
            return
        }

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

    fun generateOrderedItemsPdf(startDateString: String, endDateString: String) {
        this.orderedItemsListPdf.value = Triple(Status.STARTED, null, "")

        var startTimestamp: Timestamp
        var endTimestamp: Timestamp

        try {
            startTimestamp = Converters.getTimestampFromDateString(startDateString)
            endTimestamp = Converters.getTimestampFromDateString(endDateString)
        } catch (_: Exception) {
            this.orderedItemsListPdf.value = Triple(Status.FAILED, null, "Invalid date")
            return
        }

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

    fun loadPurchasedItemsCompChartData(startDateString: String, endDateString: String) {

        var startTimestamp: Timestamp
        var endTimestamp: Timestamp

        try {

            startTimestamp = Converters.getTimestampFromDateString(startDateString)
            endTimestamp = Converters.getTimestampFromDateString(endDateString)

        } catch (_: Exception) {
            this.purchasedItemsCompChartData.postValue(
                Triple(
                    null,
                    Status.FAILED,
                    "Invalid date"
                )
            )
            return
        }

        this.purchasedItemsCompChartData.value = Triple(null, Status.STARTED, "")

        this.supplierRepository.getFrequencyOfOrderedItemsByRange(
            startTimestamp,
            endTimestamp,
            this.viewModelScope
        ) { status, list, message ->
            if (status == Status.FAILED) {

                this.purchasedItemsCompChartData.postValue(Triple(null, status, message))

            } else {
                this.viewModelScope.launch(Dispatchers.IO) {
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

                    purchasedItemsCompChartData.postValue(Triple(barData, status, message))

                }
            }

        }
    }

    fun loadThisMonthsPurchasedItemsCompChart() {
        val calendar = Calendar.getInstance()

        var startYear = calendar.get(Calendar.YEAR)
        var startMonth = calendar.get(Calendar.MONTH) + 1
        var startDate = 1
        var endYear = startYear
        var endMonth = startMonth + 1
        var endDate = 1

        this.loadPurchasedItemsCompChartData(
            "$startDate-$startMonth-$startYear",
            "$endDate-$endMonth-$endYear"
        )
    }

}