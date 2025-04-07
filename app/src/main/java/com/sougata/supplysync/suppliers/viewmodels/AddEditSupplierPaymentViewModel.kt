package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.firebase.SupplierFirestoreRepository
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Status

class AddEditSupplierPaymentViewModel : ViewModel(), Observable {

    @Bindable
    val amount = MutableLiveData("")

    @Bindable
    val date = MutableLiveData("")

    @Bindable
    val time = MutableLiveData("")

    @Bindable
    val note = MutableLiveData("")

    @Bindable
    val supplierName = MutableLiveData("")

    private val supplierFirestoreRepository = SupplierFirestoreRepository()

    val supplierPaymentAddedIndicator = MutableLiveData<Pair<Int, String>>()
    val supplierPaymentEditedIndicator = MutableLiveData<Pair<Int, String>>()

    fun addSupplierPayment(supplierId: String, supplierName: String, view: View) {

        val supplierPayment = try {
            this.processSupplerPayment(supplierId, supplierName)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierPaymentAddedIndicator.postValue(Status.STARTED to "")

        this.supplierFirestoreRepository.addUpdateSupplierPayment(
            supplierPayment,
            SupplierFirestoreRepository.TO_ADD
        ) { status, message ->
            this.supplierPaymentAddedIndicator.postValue(status to message)
        }

    }

    fun updateSupplierPayment(
        supplierId: String,
        supplierName: String,
        supplierPaymentId: String,
        view: View
    ): SupplierPayment? {
        val supplierPayment = try {
            this.processSupplerPayment(supplierId, supplierName, supplierPaymentId)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.supplierPaymentEditedIndicator.postValue(Status.STARTED to "")

        this.supplierFirestoreRepository.addUpdateSupplierPayment(
            supplierPayment,
            SupplierFirestoreRepository.TO_UPDATE
        ) { status, message ->
            this.supplierPaymentEditedIndicator.postValue(status to message)
        }

        return supplierPayment
    }

    private fun processSupplerPayment(
        supplierId: String,
        supplierName: String,
        supplierPaymentId: String? = null
    ): SupplierPayment {
        val amountString = this.amount.value.orEmpty()
        val dateString = this.date.value.orEmpty()
        val timeString = this.time.value.orEmpty()
        val note = this.note.value.orEmpty()

        if (amountString.isEmpty()) {
            throw Exception("Amount cannot be empty")
        }

        var amount = 0.0

        try {
            amount = amountString.toDouble()
        } catch (_: Exception) {
            throw Exception("Invalid due amount")
        }

        var year = 0
        var month = 0
        var date = 0

        try {
            val res = Converters.getYearMonthDateFromDateString(dateString)
            year = res.first
            month = res.second
            date = res.third
        } catch (e: Exception) {
            throw e
        }

        var hour = 0
        var minute = 0

        try {
            val res = Converters.getHourMinuteFromTimeString(timeString)
            hour = res.first
            minute = res.second
        } catch (e: Exception) {
            throw e
        }

//        Log.d("mytag", supplierPaymentId.toString())
//
//        if(supplierPaymentId.orEmpty().isNotEmpty()) {
//            month++
//        }

        return SupplierPayment(
            amount, year, month, date, hour, minute, note, supplierId, supplierName
        ).apply { id = supplierPaymentId.orEmpty() }
    }


    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}

}