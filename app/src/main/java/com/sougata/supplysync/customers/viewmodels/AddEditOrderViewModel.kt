package com.sougata.supplysync.customers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditOrderViewModel : ViewModel() {

    val amount = MutableLiveData("")
    val quantity = MutableLiveData("")
    val date = MutableLiveData("")
    val isDelivered = MutableLiveData<Boolean>()
    val itemName = MutableLiveData("No item selected")
    val customerName = MutableLiveData("No customer selected")

    private val customerRepo = CustomerRepository()

    val orderAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val orderEditedIndicator = MutableLiveData<Pair<Status, String>>()
    val orderDeletedIndicator = MutableLiveData<Pair<Status, String>>()

    fun addOrder(
        itemId: String,
        itemName: String,
        customerId: String,
        customerName: String,
        view: View
    ) {
        val order = try {
            this.processOrder(
                UUID.randomUUID().toString(),
                Timestamp.now(),
                itemId,
                itemName,
                customerId,
                customerName
            )
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.orderAddedIndicator.value = Status.STARTED to ""

        this.customerRepo.addOrder(order) { status, message ->
            this.orderAddedIndicator.value = status to message
        }
    }

    fun updateOrder(
        orderId: String, timestamp: Timestamp, userItemId: String, userItemName: String,
        customerId: String, customerName: String, view: View
    ): Order? {
        val order = try {
            this.processOrder(
                orderId,
                timestamp,
                userItemId,
                userItemName,
                customerId,
                customerName
            )
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.orderEditedIndicator.value = Status.STARTED to ""

        this.customerRepo.updateOrder(order) { status, message ->
            this.orderEditedIndicator.value = status to message
        }

        return order
    }

    fun deleteOrder(order: Order) {
        this.orderDeletedIndicator.value = Status.STARTED to ""

        this.customerRepo.deleteOrder(order) { status, message ->
            this.orderDeletedIndicator.value = status to message
        }
    }

    private fun processOrder(
        orderId: String,
        timestamp: Timestamp,
        itemId: String,
        itemName: String,
        customerId: String,
        customerName: String,
    ): Order {
        val amountString = this.amount.value.orEmpty()
        val quantityString = this.quantity.value.orEmpty()
        val dateString = this.date.value.orEmpty()
        val isDelivered = this.isDelivered.value ?: false

        if (amountString.isEmpty()) {
            throw Exception("Amount cannot be empty")
        }

        if (quantityString.isEmpty()) {
            throw Exception("Quantity cannot be empty")
        }

        var amount = 0.0
        var quantity = 0

        try {
            amount = amountString.toDouble()
        } catch (_: Exception) {
            throw Exception("Invalid amount")
        }

        try {
            quantity = quantityString.toInt()
        } catch (_: Exception) {
            throw Exception("Invalid quantity")
        }

        var orderTimestamp: Timestamp

        try {
            orderTimestamp = DateTime.getTimestampFromDateString(dateString)
        } catch (e: Exception) {
            throw e
        }

        return Order(
            id = orderId,
            timestamp = timestamp,
            userItemId = itemId,
            userItemName = itemName,
            quantity = quantity,
            amount = amount,
            customerId = customerId,
            customerName = customerName,
            deliveryTimestamp = orderTimestamp,
            delivered = isDelivered
        )
    }

}