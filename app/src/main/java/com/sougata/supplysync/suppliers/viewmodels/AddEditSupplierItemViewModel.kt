package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.util.Status

class AddEditSupplierItemViewModel : ViewModel() {

    val name = MutableLiveData("")
    val price = MutableLiveData("")
    val details = MutableLiveData("")

    private val supplierRepository = SupplierRepository()

    val supplierItemAddedIndicator = MutableLiveData<Pair<Int, String>>()
    val supplierItemEditedIndicator = MutableLiveData<Pair<Int, String>>()

    fun addSupplierItem(view: View) {
        val supplierItem = try {
            this.processSupplierItem()
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierItemAddedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.addUpdateSupplierItem(
            supplierItem,
            SupplierRepository.TO_ADD
        ) { status, message ->
            this.supplierItemAddedIndicator.postValue(status to message)
        }
    }

    fun updateSupplierItem(supplierItemId: String, view: View): SupplierItem? {
        val supplierItem = try {
            this.processSupplierItem(supplierItemId)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.supplierItemEditedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.addUpdateSupplierItem(
            supplierItem,
            SupplierRepository.TO_UPDATE
        ) { status, message ->
            this.supplierItemEditedIndicator.postValue(status to message)
        }
        return supplierItem
    }


    private fun processSupplierItem(supplierItemId: String? = null): SupplierItem {
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

        return SupplierItem(name, price, details).apply { id = supplierItemId.orEmpty() }
    }

}