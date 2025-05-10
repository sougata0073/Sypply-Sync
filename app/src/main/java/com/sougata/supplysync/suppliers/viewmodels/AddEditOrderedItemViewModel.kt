package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.firestore.util.Action
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status

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
            this.processOrderedItem(itemId, itemName, supplierId, supplierName)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.orderedItemAddedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.addUpdateOrderedItem(
            orderedItem,
            Action.TO_ADD
        ) { status, message ->
            this.orderedItemAddedIndicator.postValue(status to message)
        }

    }

    fun updateOrderedItem(
        itemId: String,
        itemName: String,
        supplierId: String,
        supplierName: String,
        orderedItemId: String,
        view: View
    ): OrderedItem? {
        val orderedItem = try {
            this.processOrderedItem(itemId, itemName, supplierId, supplierName, orderedItemId)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.orderedItemEditedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.addUpdateOrderedItem(
            orderedItem,
            Action.TO_UPDATE
        ) { status, message ->
            this.orderedItemEditedIndicator.postValue(status to message)
        }

        return orderedItem
    }


    private fun processOrderedItem(
        itemId: String,
        itemName: String,
        supplierId: String,
        supplierName: String,
        orderedItemId: String? = null
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
            supplierItemId = itemId,
            supplierItemName = itemName,
            quantity = quantity,
            amount = amount,
            supplierId = supplierId,
            supplierName = supplierName,
            orderTimestamp = orderTimestamp,
            isReceived = isReceived
        ).apply { id = orderedItemId.orEmpty() }
    }


}