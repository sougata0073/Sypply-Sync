package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditOrderedItemViewModel : ViewModel() {

    val amount = MutableLiveData("")
    val quantity = MutableLiveData("")
    val date = MutableLiveData("")
    val isReceived = MutableLiveData<Boolean>()
    val itemName = MutableLiveData("No item selected")
    val supplierName = MutableLiveData("No supplier selected")

    private val supplierRepository = SupplierRepository()

    val orderedItemAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val orderedItemEditedIndicator = MutableLiveData<Pair<Status, String>>()

    fun addOrderedItem(
        itemId: String,
        itemName: String,
        supplierId: String,
        supplierName: String,
        view: View
    ) {

        val orderedItem = try {
            this.processOrderedItem(
                UUID.randomUUID().toString(),
                Timestamp.now(),
                itemId,
                itemName,
                supplierId,
                supplierName
            )
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.orderedItemAddedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.addOrderedItem(
            orderedItem
        ) { status, message ->
            this.orderedItemAddedIndicator.postValue(status to message)
        }

    }

    fun updateOrderedItem(
        orderedItemId: String,
        timestamp: Timestamp,
        supplierItemId: String,
        supplierItemName: String,
        supplierId: String,
        supplierName: String,
        view: View
    ): OrderedItem? {
        val orderedItem = try {
            this.processOrderedItem(
                orderedItemId,
                timestamp,
                supplierItemId,
                supplierItemName,
                supplierId,
                supplierName
            )
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.orderedItemEditedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.updateOrderedItem(
            orderedItem
        ) { status, message ->
            this.orderedItemEditedIndicator.postValue(status to message)
        }

        return orderedItem
    }


    private fun processOrderedItem(
        orderedItemId: String,
        timestamp: Timestamp,
        itemId: String,
        itemName: String,
        supplierId: String,
        supplierName: String,
    ): OrderedItem {
        val amountString = this.amount.value.orEmpty()
        val quantityString = this.quantity.value.orEmpty()
        val dateString = this.date.value.orEmpty()
        val isReceived = this.isReceived.value ?: false

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
            throw Exception("Invalid due amount")
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

        return OrderedItem(
            id = orderedItemId,
            timestamp = timestamp,
            supplierItemId = itemId,
            supplierItemName = itemName,
            quantity = quantity,
            amount = amount,
            supplierId = supplierId,
            supplierName = supplierName,
            orderTimestamp = orderTimestamp,
            received = isReceived
        )
    }


}