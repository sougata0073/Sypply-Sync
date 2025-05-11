package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditSupplierViewModel() : ViewModel() {

    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val phone = MutableLiveData("")
    val dueAmount = MutableLiveData("")
    val paymentDetails = MutableLiveData("")
    val note = MutableLiveData("")

    val supplierRepository = SupplierRepository()

    val supplierAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val supplierEditedIndicator = MutableLiveData<Pair<Status, String>>()

    var isSupplierAdded = false
    var isSupplierUpdated = false
    var isSupplierDeleted = false

    fun addSupplier(view: View) {
        val supplier = try {
            this.processSupplier(UUID.randomUUID().toString(), Timestamp.now())
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierAddedIndicator.value = Status.STARTED to ""

        this.supplierRepository.addSupplier(
            supplier
        ) { status, message ->
            this.supplierAddedIndicator.value = status to message
        }
    }

    fun updateSupplier(supplierId: String, timestamp: Timestamp, view: View): Supplier? {
        val supplier = try {
            this.processSupplier(supplierId, timestamp)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.supplierEditedIndicator.value = Status.STARTED to ""

        this.supplierRepository.updateSupplier(
            supplier
        ) { status, message ->
            this.supplierEditedIndicator.value = status to message
        }
        return supplier
    }

    private fun processSupplier(supplierId: String, timestamp: Timestamp): Supplier {
        val name = this.name.value.orEmpty()
        val email = this.email.value.orEmpty()
        val phone = this.phone.value.orEmpty()
        val dueAmountString = this.dueAmount.value.orEmpty()
        val paymentDetails = this.paymentDetails.value.orEmpty()
        val note = this.note.value.orEmpty()

        if (name.isEmpty()) {
            throw Exception("Name can't be empty")
        }

        var dueAmount = 0.0

        if (dueAmountString.isNotEmpty()) {
            try {
                dueAmount = dueAmountString.toDouble()
            } catch (_: Exception) {
                throw Exception("Invalid due amount")
            }
        }

        return Supplier(
            supplierId,
            timestamp,
            name,
            dueAmount,
            phone,
            email,
            note,
            paymentDetails,
            Inputs.getRandomImageUrl()
        )
    }

}