package com.sougata.supplysync.sharedviewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itextpdf.commons.utils.DateTimeUtil
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status

class ExpensiveValuesViewModel : ViewModel() {

    private val suppliersRepo = SupplierRepository()

    val purchaseAmountByRange = MutableLiveData<Triple<Number?, Int, String>>()
    var purchaseAmountDateRange = ""

    init {
        this.loadPast30DaysPurchaseAmount()
    }

    fun loadPurchaseAmountByRange(startDateMillis: Long, endDateMillis: Long) {
        this.purchaseAmountByRange.value = Triple(null, Status.STARTED, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.suppliersRepo.getPurchaseAmountByRange(
            startTimestamp,
            endTimestamp
        ) { status, amount, message ->

            val startDateString = DateTime.getDateStringFromTimestamp(startTimestamp)
            val endDateString = DateTime.getDateStringFromTimestamp(endTimestamp)
            this.purchaseAmountDateRange = "From: $startDateString To: $endDateString"

            this.purchaseAmountByRange.value = Triple(amount, status, message)

        }
    }

    fun loadPast30DaysPurchaseAmount() {

        val startDateMillis = DateTime.getPastDateInMillis(30)
        val endDateMillis = DateTime.getPastDateInMillis(0)

        this.loadPurchaseAmountByRange(startDateMillis, endDateMillis)
    }

}