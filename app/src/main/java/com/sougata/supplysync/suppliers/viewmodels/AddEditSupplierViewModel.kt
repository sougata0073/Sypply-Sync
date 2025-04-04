package com.sougata.supplysync.suppliers.viewmodels

import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.firebase.FirestoreRepository
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.Status

class AddEditSupplierViewModel : ViewModel(), Observable {

    @Bindable
    val name = MutableLiveData("")

    @Bindable
    val email = MutableLiveData("")

    @Bindable
    val phone = MutableLiveData("")

    @Bindable
    val dueAmount = MutableLiveData("")

    @Bindable
    val paymentDetails = MutableLiveData("")

    @Bindable
    val note = MutableLiveData("")

    @Bindable
    val profileImageUrl = MutableLiveData("")

    private val firestoreRepository = FirestoreRepository()

    val supplierAddedIndicator = MutableLiveData<Pair<Int, String>>()
    val supplierEditedIndicator = MutableLiveData<Pair<Int, String>>()

    fun addSupplier(view: View) {
        val supplier = try {
            this.processSupplier()
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.supplierAddedIndicator.postValue(Status.STARTED to "")

        this.firestoreRepository.addUpdateSupplier(
            supplier,
            FirestoreRepository.TO_ADD
        ) { status, message ->
            this.supplierAddedIndicator.postValue(status to message)
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

        this.firestoreRepository.addUpdateSupplier(
            supplier,
            FirestoreRepository.TO_UPDATE
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
        val profileImageUrl = this.profileImageUrl.value.orEmpty()

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
            if (profileImageUrl.isEmpty()) Inputs.getRandomImageUrl() else profileImageUrl
        ).apply { id = supplierId.orEmpty() }
    }


    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}


}