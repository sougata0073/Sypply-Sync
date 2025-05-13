package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditSupplierPaymentViewModel : ViewModel() {

    val amount = MutableLiveData("")
    val date = MutableLiveData("")
    val time = MutableLiveData("")
    val note = MutableLiveData("")
    val supplierName = MutableLiveData("No supplier selected")

    private val supplierRepository = SupplierRepository()

    val supplierPaymentAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val supplierPaymentEditedIndicator = MutableLiveData<Pair<Status, String>>()
    val supplierPaymentDeletedIndicator = MutableLiveData<Pair<Status, String>>()

    fun addSupplierPayment(supplierId: String, supplierName: String, view: View) {

        val supplierPayment = try {
            this.processSupplerPayment(
                UUID.randomUUID().toString(),
                Timestamp.now(),
                supplierId,
                supplierName
            )
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierPaymentAddedIndicator.value = Status.STARTED to ""

        this.supplierRepository.addSupplierPayment(
            supplierPayment
        ) { status, message ->
            this.supplierPaymentAddedIndicator.value = status to message
        }

    }

    fun updateSupplierPayment(
        supplierPaymentId: String,
        timestamp: Timestamp,
        supplierId: String,
        supplierName: String,
        view: View
    ): SupplierPayment? {
        val supplierPayment = try {
            this.processSupplerPayment(supplierPaymentId, timestamp, supplierId, supplierName)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.supplierPaymentEditedIndicator.value = Status.STARTED to ""

        this.supplierRepository.updateSupplierPayment(
            supplierPayment
        ) { status, message ->
            this.supplierPaymentEditedIndicator.value = status to message
        }

        return supplierPayment
    }
    
    fun deleteSupplierPayment(supplierPayment: SupplierPayment) {
        this.supplierPaymentDeletedIndicator.value = Status.STARTED to ""

        this.supplierRepository.deleteSupplierPayment(supplierPayment) { status, message ->
            this.supplierPaymentDeletedIndicator.value = status to message
        }
    }

    private fun processSupplerPayment(
        supplierPaymentId: String,
        timestamp: Timestamp,
        supplierId: String,
        supplierName: String,
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
            throw Exception("Invalid amount")
        }

        var paymentTimestamp: Timestamp

        try {
            paymentTimestamp = DateTime.getTimestampFromDateTimeString(dateString, timeString)
        } catch (e: Exception) {
            throw e
        }

        return SupplierPayment(
            supplierPaymentId, timestamp,
            amount,
            paymentTimestamp,
            note,
            supplierId,
            supplierName
        )
    }

}