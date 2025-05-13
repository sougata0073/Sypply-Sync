package com.sougata.supplysync.customers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditCustomerViewModel : ViewModel() {

    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val phone = MutableLiveData("")
    val receivableAmount = MutableLiveData("")
    val dueOrders = MutableLiveData("")
    val note = MutableLiveData("")

    private val customerRepository = CustomerRepository()

    val customerAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val customerEditedIndicator = MutableLiveData<Pair<Status, String>>()
    val customerDeletedIndicator = MutableLiveData<Pair<Status, String>>()

    var isCustomerAdded = false
    var isCustomerUpdated = false
    var isCustomerDeleted = false

    fun addCustomer(view: View) {
        val customer = try {
            this.processCustomer(UUID.randomUUID().toString(), Timestamp.now())
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.customerAddedIndicator.value = Status.STARTED to ""

        this.customerRepository.addCustomer(customer) { status, message ->
            this.customerAddedIndicator.value = status to message
        }
    }

    fun updateCustomer(customerId: String, timeStamp: Timestamp, view: View): Customer? {
        val customer = try {
            this.processCustomer(customerId, timeStamp)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.customerEditedIndicator.value = Status.STARTED to ""

        this.customerRepository.updateCustomer(customer) { status, message ->
            this.customerEditedIndicator.value = status to message
        }

        return customer
    }

    fun deleteCustomer(customer: Customer) {
        this.customerDeletedIndicator.value = Status.STARTED to ""

        this.customerRepository.deleteCustomer(customer) { status, message ->
            this.customerDeletedIndicator.value = status to message
        }
    }

    private fun processCustomer(customerId: String, timeStamp: Timestamp): Customer {
        val name = this.name.value.orEmpty()
        val email = this.email.value.orEmpty()
        val phone = this.phone.value.orEmpty()
        val receivableAmountString = this.receivableAmount.value.orEmpty()
        val dueOrdersString = this.dueOrders.value.orEmpty()
        val note = this.note.value.orEmpty()

        if (name.isEmpty()) {
            throw Exception("Name can't be empty")
        }

        var receivableAmount = 0.0
        var dueOrders = 0

        if (receivableAmountString.isNotEmpty()) {
            try {
                receivableAmount = receivableAmountString.toDouble()
            } catch (_: Exception) {
                throw Exception("Invalid receivable amount")
            }
        }

        if (dueOrdersString.isNotEmpty()) {
            try {
                dueOrders = dueOrdersString.toInt()
            } catch (_: Exception) {
                throw Exception("Invalid due orders")
            }
        }

        return Customer(
            customerId,
            timeStamp,
            name,
            receivableAmount,
            dueOrders,
            phone,
            email,
            note,
            Inputs.getRandomImageUrl()
        )
    }

}