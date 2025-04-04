package com.sougata.supplysync.suppliers.viewmodels

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.sougata.supplysync.R
import com.sougata.supplysync.firebase.FirestoreRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.modelslist.ModelsListFragment

class SuppliersHomeViewModel : ViewModel() {

    val firestoreRepository = FirestoreRepository()

    val numberOfSuppliers = MutableLiveData<Triple<Int, Int, String>>()
    val dueAmountToSuppliers = MutableLiveData<Triple<Double, Int, String>>()

    init {
        this.loadNumberOfSuppliers()
        this.loadDueAmountToSuppliers()
    }

    fun loadNumberOfSuppliers() {
//        Log.d("api", "num supplier")
        this.numberOfSuppliers.postValue(Triple(0, Status.STARTED, ""))

        this.firestoreRepository.getNumberOfSuppliers { status, count, message ->
            this.numberOfSuppliers.postValue(Triple(count, status, message))
        }
    }

    fun loadDueAmountToSuppliers() {
//        Log.d("api", "due amount supplier")
        this.dueAmountToSuppliers.postValue(Triple(0.0, Status.STARTED, ""))

        this.firestoreRepository.getDueAmountToSuppliers { status, amount, message ->
            this.dueAmountToSuppliers.postValue(Triple(amount, status, message))
        }
    }

    fun onSuppliersListClick(view: View) {

        val bundle = Bundle().apply {
            putString(ModelsListFragment.MODEL_NAME_KEY, Model.SUPPLIER)
        }

        view.findNavController()
            .navigate(R.id.modelsListFragment, bundle, Inputs.getFragmentAnimations())
    }

    fun onItemsListClick(view: View) {

        val bundle = Bundle().apply {
            putString(ModelsListFragment.MODEL_NAME_KEY, Model.SUPPLIERS_ITEM)
        }

        view.findNavController()
            .navigate(
                R.id.modelsListFragment,
                bundle,
                Inputs.getFragmentAnimations()
            )
    }

    fun onPaymentsListClick(view: View) {

        val bundle = Bundle().apply {
            putString(ModelsListFragment.MODEL_NAME_KEY, Model.SUPPLIER_PAYMENT)
        }

        view.findNavController()
            .navigate(
                R.id.modelsListFragment,
                bundle,
                Inputs.getFragmentAnimations()
            )
    }

    fun onOrderedItemsClick(view: View) {
        view.findNavController()
            .navigate(R.id.modelsListFragment)
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