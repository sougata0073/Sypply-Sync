package com.sougata.supplysync.sharedviewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status

class CommonDataViewModel : ViewModel() {

    private val suppliersRepo = SupplierRepository()
    private val customerRepo = CustomerRepository()

    val purchaseAmountByRange = MutableLiveData<Triple<Number?, Status, String>>()
    val salesAmountByRange = MutableLiveData<Triple<Number?, Status, String>>()
    val numberOfOrdersToReceive = MutableLiveData<Triple<Number?, Status, String>>()
    val numberOfOrdersToDeliver = MutableLiveData<Triple<Number?, Status, String>>()

    var purchaseAmountDateRange = ""
    var salesAmountDateRange = ""

    init {
        this.loadPast30DaysPurchaseAmount()
        this.loadPast30DaysSalesAmount()
        this.loadOrdersToReceive()
        this.loadOrdersToDeliver()
    }

    fun loadPurchaseAmountByRange(startDateMillis: Long, endDateMillis: Long) {
        this.purchaseAmountByRange.value = Triple(0.0, Status.STARTED, "")

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

    fun loadSalesAmountByRange(startDateMillis: Long, endDateMillis: Long) {
        this.salesAmountByRange.value = Triple(0.0, Status.STARTED, "")

        var startTimestamp = DateTime.getTimestampFromMillis(startDateMillis)
        var endTimestamp = DateTime.getTimestampFromMillis(endDateMillis)

        this.customerRepo.getSalesAmountByRange(
            startTimestamp,
            endTimestamp
        ) { status, amount, message ->
            val startDateString = DateTime.getDateStringFromTimestamp(startTimestamp)
            val endDateString = DateTime.getDateStringFromTimestamp(endTimestamp)
            this.salesAmountDateRange = "From: $startDateString To: $endDateString"

            this.salesAmountByRange.value = Triple(amount, status, message)
        }
    }

    fun loadOrdersToReceive() {
        this.numberOfOrdersToReceive.value = Triple(0, Status.STARTED, "")

        this.suppliersRepo.getOrdersToReceive { status, count, message ->
            this.numberOfOrdersToReceive.value = Triple(count, status, message)
        }
    }

    fun loadOrdersToDeliver() {
        this.numberOfOrdersToDeliver.value = Triple(0, Status.STARTED, "")

        this.customerRepo.getOrdersToDeliver { status, count, message ->
            this.numberOfOrdersToDeliver.value = Triple(count, status, message)
        }
    }

    fun loadPast30DaysPurchaseAmount() {

        val startDateMillis = DateTime.getPastDateInMillis(30)
        val endDateMillis = DateTime.getPastDateInMillis(0)

        this.loadPurchaseAmountByRange(startDateMillis, endDateMillis)
    }

    fun loadPast30DaysSalesAmount() {
        val startDateMillis = DateTime.getPastDateInMillis(30)
        val endDateMillis = DateTime.getPastDateInMillis(0)

        this.loadSalesAmountByRange(startDateMillis, endDateMillis)
    }

}