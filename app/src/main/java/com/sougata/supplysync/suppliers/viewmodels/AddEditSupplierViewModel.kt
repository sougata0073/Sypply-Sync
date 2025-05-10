package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.firestore.util.Action
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.Status

class AddEditSupplierViewModel() : ViewModel() {

    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val phone = MutableLiveData("")
    val dueAmount = MutableLiveData("")
    val paymentDetails = MutableLiveData("")
    val note = MutableLiveData("")

    private val supplierRepository = SupplierRepository()

    val supplierAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val supplierEditedIndicator = MutableLiveData<Pair<Status, String>>()


    fun addSupplier(view: View) {
        val supplier = try {
            this.processSupplier()
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierAddedIndicator.postValue(Status.STARTED to "")

        supplierRepository.addUpdateSupplier(
            supplier,
            Action.TO_ADD
        ) { status, message ->
            supplierAddedIndicator.postValue(status to message)
        }


    }

    fun updateSupplier(supplierId: String, view: View): Supplier? {
        val supplier = try {
            this.processSupplier(supplierId)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.supplierEditedIndicator.postValue(Status.STARTED to "")

        this.supplierRepository.addUpdateSupplier(
            supplier,
            Action.TO_UPDATE
        ) { status, message ->
            this.supplierEditedIndicator.postValue(status to message)
        }
        return supplier
    }

    private fun processSupplier(supplierId: String? = null): Supplier {
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
            name,
            dueAmount,
            phone,
            email,
            note,
            paymentDetails,
            Inputs.getRandomImageUrl()
        ).apply { id = supplierId.orEmpty() }
    }

//    fun getImageExtension(uri: Uri): String? {
//        val contentResolver = getApplication<Application>().contentResolver
//        val mimeType = contentResolver.getType(uri)
//
//        return MimeTypeMap.getSingleton()
//            .getExtensionFromMimeType(mimeType)
//    }
}