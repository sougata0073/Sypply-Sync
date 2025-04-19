package com.sougata.supplysync.suppliers.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Timestamp
import com.sougata.supplysync.R
import com.sougata.supplysync.cloud.SupplierFirestoreRepository
import com.sougata.supplysync.pdf.SupplierPdfRepository
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class SupplierReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<Application>()

    val purchasedItemsCompChartRangeDate = MutableLiveData("")

    private val supplierFirestoreRepository = SupplierFirestoreRepository()
    private val supplierPdfRepository = SupplierPdfRepository()

    val supplierPaymentsListPdfIndicator = MutableLiveData<Pair<Int, String>>()
    val purchasedItemsCompChartData = MutableLiveData<Triple<BarData?, Int, String>>()
    val purchasedItemsCompChartXStrings = mutableListOf<String>()

    init {
        this.loadThisMonthsPurchasedItemsCompChart()
    }

    fun generateSupplierPaymentsPdf(startDateString: String, endDateString: String) {
        supplierPaymentsListPdfIndicator.postValue(Pair(Status.STARTED, ""))

        var startTimestamp: Timestamp
        var endTimestamp: Timestamp

        try {
            val startDate = Converters.getYearMonthDateFromDateString(startDateString)
            val endDate = Converters.getYearMonthDateFromDateString(endDateString)

//            Log.d("date", startDate.toString())
//            Log.d("date", endDate.toString())

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

//            Log.d("date", startTimestamp.toString())
//            Log.d("date", endTimestamp.toString())

        } catch (_: Exception) {
            this.supplierPaymentsListPdfIndicator.postValue(Pair(Status.FAILED, "Invalid date"))
            return
        }

        this.supplierFirestoreRepository.getSupplierPaymentsByRange(
            startTimestamp, endTimestamp,
            this.viewModelScope
        ) { status, list, message ->
            if (status == Status.SUCCESS) {
//                Log.d("list", list.toString())

                this.viewModelScope.launch {
                    supplierPdfRepository.generateSupplierPaymentsPdf(
                        list,
                        app.getExternalFilesDir(null) ?: return@launch,
                        "supplier_payments.pdf",
                        app.applicationContext
                    ) { status, message ->
                        supplierPaymentsListPdfIndicator.postValue(Pair(status, message))
                    }
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

        this.loadPurchasedItemsCompBarChartData(
            "$startDate-$startMonth-$startYear",
            "$endDate-$endMonth-$endYear"
        )
    }

    fun loadPurchasedItemsCompBarChartData(startDateString: String, endDateString: String) {

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
            this.purchasedItemsCompChartData.postValue(
                Triple(
                    null,
                    Status.FAILED,
                    "Invalid date"
                )
            )
            return
        }

        this.purchasedItemsCompChartData.postValue(Triple(null, Status.STARTED, ""))

        this.supplierFirestoreRepository.getFrequencyOfOrderedItemsByRange(
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

}