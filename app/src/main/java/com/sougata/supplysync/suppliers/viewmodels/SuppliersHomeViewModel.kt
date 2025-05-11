package com.sougata.supplysync.suppliers.viewmodels

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class SuppliersHomeViewModel : ViewModel() {

    val supplierRepository = SupplierRepository()

    val numberOfSuppliers = MutableLiveData<Triple<Number?, Status, String>>()
    val dueAmountToSuppliers = MutableLiveData<Triple<Number?, Status, String>>()

    val allApiCallFinished = MutableLiveData(false)

    // Change the value according to the number of api calls
    private val totalApiCalls = 2

    private var apiCallFinishCount = 0
        set(value) {
            field = value
            if (field == this.totalApiCalls) {
                this.allApiCallFinished.value = true
            }
        }

    init {
        this.loadNumberOfSuppliers()
        this.loadDueAmountToSuppliers()
    }

    fun loadNumberOfSuppliers() {
        this.numberOfSuppliers.value = Triple(0, Status.STARTED, "")

        this.supplierRepository.getNumberOfSuppliers { status, count, message ->
            this.numberOfSuppliers.value = Triple(count, status, message)
            this.apiCallFinishCount++
        }
    }

    fun loadDueAmountToSuppliers() {
        this.dueAmountToSuppliers.value = Triple(0.0, Status.STARTED, "")

        this.supplierRepository.getDueAmountToSuppliers { status, amount, message ->
            this.dueAmountToSuppliers.value = Triple(amount, status, message)
            this.apiCallFinishCount++
        }
    }

    fun onSuppliersListClick(view: View) {

        val bundle = Bundle().apply {
            putString(KeysAndMessages.MODEL_NAME_KEY, Model.SUPPLIER)
        }

        view.findNavController()
            .navigate(R.id.modelsListFragment, bundle, AnimationProvider.slideRightLeftNavOptions())
    }

    fun onItemsListClick(view: View) {

        val bundle = Bundle().apply {
            putString(KeysAndMessages.MODEL_NAME_KEY, Model.SUPPLIERS_ITEM)
        }

        view.findNavController()
            .navigate(
                R.id.modelsListFragment,
                bundle,
                AnimationProvider.slideRightLeftNavOptions()
            )
    }

    fun onPaymentsListClick(view: View) {

        val bundle = Bundle().apply {
            putString(KeysAndMessages.MODEL_NAME_KEY, Model.SUPPLIER_PAYMENT)
        }

        view.findNavController()
            .navigate(
                R.id.modelsListFragment,
                bundle,
                AnimationProvider.slideRightLeftNavOptions()
            )
    }

    fun onOrderedItemsClick(view: View) {
        val bundle = Bundle().apply {
            putString(KeysAndMessages.MODEL_NAME_KEY, Model.ORDERED_ITEM)
        }

        view.findNavController()
            .navigate(
                R.id.modelsListFragment, bundle, AnimationProvider.slideRightLeftNavOptions()
            )
    }

    fun onReportsClick(view: View) {
        view.findNavController()
            .navigate(R.id.action_suppliersHomeFragment_to_suppliersReportsFragment)
    }

//    override fun onCleared() {
//        super.onCleared()
//
//        Log.d("api", "view model destroyed")
//    }

}