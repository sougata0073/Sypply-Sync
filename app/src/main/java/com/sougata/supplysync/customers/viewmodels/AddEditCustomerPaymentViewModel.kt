package com.sougata.supplysync.customers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditCustomerPaymentViewModel : ViewModel() {

    val amount = MutableLiveData("")
    val date = MutableLiveData("")
    val time = MutableLiveData("")
    val note = MutableLiveData("")
    val customerName = MutableLiveData("No customer selected")

    private val customerRepo = CustomerRepository()

    val customerPaymentAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val customerPaymentEditedIndicator = MutableLiveData<Pair<Status, String>>()
    val customerPaymentDeletedIndicator = MutableLiveData<Pair<Status, String>>()

    fun addCustomerPayment(customerId: String, customerName: String, view: View) {
        val customerPayment = try {
            this.processCustomerPayment(
                UUID.randomUUID().toString(),
                Timestamp.now(), customerId, customerName
            )
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.customerPaymentAddedIndicator.value = Status.STARTED to ""

        this.customerRepo.addCustomerPayment(customerPayment) { status, message ->
            this.customerPaymentAddedIndicator.value = status to message
        }
    }

    fun updateCustomerPayment(
        customerPaymentId: String,
        timestamp: Timestamp,
        customerId: String,
        customerName: String,
        view: View
    ): CustomerPayment? {
        val customerPayment = try {
            this.processCustomerPayment(
                customerPaymentId,
                timestamp, customerId, customerName
            )
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.customerPaymentEditedIndicator.value = Status.STARTED to ""

        this.customerRepo.updateCustomerPayment(customerPayment) { status, message ->
            this.customerPaymentEditedIndicator.value = status to message
        }

        return customerPayment
    }

    fun deleteCustomerPayment(customerPayment: CustomerPayment) {
        this.customerPaymentDeletedIndicator.value = Status.STARTED to ""

        this.customerRepo.deleteCustomerPayment(customerPayment) { status, message ->
            this.customerPaymentDeletedIndicator.value = status to message
        }
    }

    private fun processCustomerPayment(
        customerPaymentId: String,
        timestamp: Timestamp,
        customerId: String,
        customerName: String,
    ): CustomerPayment {
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

        return CustomerPayment(
            customerPaymentId, timestamp,
            amount,
            paymentTimestamp,
            note,
            customerId,
            customerName
        )
    }
}