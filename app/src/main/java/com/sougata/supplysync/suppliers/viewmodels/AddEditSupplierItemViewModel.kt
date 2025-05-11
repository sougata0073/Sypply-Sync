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

    fun addSupplierItem(view: View) {
        val supplierItem = try {
            this.processSupplierItem(UUID.randomUUID().toString(), Timestamp.now())
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierItemAddedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.addSupplierItem(
            supplierItem
        ) { status, message ->
            this.supplierItemAddedIndicator.postValue(status to message)
        }
    }

    fun updateSupplierItem(supplierItemId: String, timestamp: Timestamp, view: View): SupplierItem? {
        val supplierItem = try {
            this.processSupplierItem(supplierItemId, timestamp)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.supplierItemEditedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.updateSupplierItem(
            supplierItem
        ) { status, message ->
            this.supplierItemEditedIndicator.postValue(status to message)
        }
        return supplierItem
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
                throw Exception("Invalid due amount")
            }
        }

        return SupplierItem(supplierItemId, timestamp, name, price, details)
    }

}