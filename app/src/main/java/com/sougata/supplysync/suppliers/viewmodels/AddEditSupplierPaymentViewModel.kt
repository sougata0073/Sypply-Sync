package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status

class AddEditSupplierPaymentViewModel : ViewModel() {

    val amount = MutableLiveData("")
    val date = MutableLiveData("")
    val time = MutableLiveData("")
    val note = MutableLiveData("")
    val supplierName = MutableLiveData("No supplier selected")

    private val supplierRepository = SupplierRepository()

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

        this.supplierRepository.addUpdateSupplierPayment(
            supplierPayment,
            SupplierRepository.TO_ADD
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

        this.supplierRepository.addUpdateSupplierPayment(
            supplierPayment,
            SupplierRepository.TO_UPDATE
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

        var paymentTimestamp: Timestamp

        try {
            paymentTimestamp = DateTime.getTimestampFromDateTimeString(dateString, timeString)
        } catch (e: Exception) {
            throw e
        }

        return SupplierPayment(
            amount = amount,
            paymentTimestamp = paymentTimestamp,
            note = note,
            supplierId = supplierId,
            supplierName = supplierName
        ).apply { id = supplierPaymentId.orEmpty() }
    }

}