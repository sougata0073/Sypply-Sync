package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditSupplierItemViewModel : ViewModel() {

    val name = MutableLiveData("")
    val price = MutableLiveData("")
    val details = MutableLiveData("")

    private val supplierRepository = SupplierRepository()

    val supplierItemAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val supplierItemEditedIndicator = MutableLiveData<Pair<Status, String>>()
    val supplierItemDeletedIndicator = MutableLiveData<Pair<Status, String>>()

    fun addSupplierItem(view: View) {
        val supplierItem = try {
            this.processSupplierItem(UUID.randomUUID().toString(), Timestamp.now())
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierItemAddedIndicator.value = Status.STARTED to ""

        this.supplierRepository.addSupplierItem(
            supplierItem
        ) { status, message ->
            this.supplierItemAddedIndicator.value = status to message
        }
    }

    fun updateSupplierItem(supplierItemId: String, timestamp: Timestamp, view: View): SupplierItem? {
        val supplierItem = try {
            this.processSupplierItem(supplierItemId, timestamp)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.supplierItemEditedIndicator.value = Status.STARTED to ""

        this.supplierRepository.updateSupplierItem(
            supplierItem
        ) { status, message ->
            this.supplierItemEditedIndicator.value = status to message
        }
        return supplierItem
    }

    fun deleteSupplierItem(supplierItem: SupplierItem) {

        this.supplierItemDeletedIndicator.value = Status.STARTED to ""

        this.supplierRepository.deleteSupplierItem(supplierItem) { status, message ->
            this.supplierItemDeletedIndicator.value = status to message
        }

    }


    private fun processSupplierItem(supplierItemId: String, timestamp: Timestamp): SupplierItem {
        val name = this.name.value.orEmpty()
        val priceString = this.price.value.orEmpty()
        val details = this.details.value.orEmpty()

        if (name.isEmpty()) {
            throw Exception("Name can't be empty")
        }

        var price = 0.0

        if (priceString.isNotEmpty()) {
            try {
                price = priceString.toDouble()
            } catch (_: Exception) {
                throw Exception("Invalid price")
            }
        }

        return SupplierItem(supplierItemId, timestamp, name, price, details)
    }

}