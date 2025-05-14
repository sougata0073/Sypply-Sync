package com.sougata.supplysync.customers.viewmodels

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class CustomersHomeViewModel : ViewModel() {

    private val customerRepository = CustomerRepository()

    val ordersToDeliver = MutableLiveData<Triple<Number?, Status, String>>()
    val numberOfCustomers = MutableLiveData<Triple<Number?, Status, String>>()
    val receivableAmountFromCustomers = MutableLiveData<Triple<Number?, Status, String>>()

    val allApiCallFinished = MutableLiveData(false)

    private val totalApiCalls = 3

    private var apiCallFinishCount = 0
        set(value) {
            field = value
            if (field == this.totalApiCalls) {
                this.allApiCallFinished.value = true
            }
        }

    init {
        this.loadOrdersToDeliver()
        this.loadNumberOfCustomers()
        this.loadReceivableAmountFromCustomers()
    }

    fun loadOrdersToDeliver() {
        this.ordersToDeliver.value = Triple(0, Status.STARTED, "")

        this.customerRepository.getOrdersToDeliver { status, count, message ->
            this.ordersToDeliver.value = Triple(count, status, message)
            this.apiCallFinishCount++
        }
    }

    fun loadNumberOfCustomers() {
        this.numberOfCustomers.value = Triple(0, Status.STARTED, "")

        this.customerRepository.getNumberOfCustomers { status, count, message ->
            this.numberOfCustomers.value = Triple(count, status, message)
            this.apiCallFinishCount++
        }
    }

    fun loadReceivableAmountFromCustomers() {
        this.receivableAmountFromCustomers.value = Triple(0.0, Status.STARTED, "")

        this.customerRepository.getReceivableAmountFromCustomers { status, amount, message ->
            this.receivableAmountFromCustomers.value = Triple(amount, status, message)
            this.apiCallFinishCount++
        }

    }

    fun onCustomersListBtnClicked(view: View) {
        val bundle = Bundle().apply {
            putString(Keys.MODEL_NAME, Model.CUSTOMER)
        }

        view.findNavController()
            .navigate(R.id.modelsListFragment, bundle, AnimationProvider.slideRightLeftNavOptions())
    }

    fun onOrdersListBtnClicked(view: View) {
        val bundle = Bundle().apply {
            putString(Keys.MODEL_NAME, Model.ORDER)
        }

        view.findNavController()
            .navigate(R.id.modelsListFragment, bundle, AnimationProvider.slideRightLeftNavOptions())
    }

    fun onPaymentsListBtnClicked(view: View) {
        val bundle = Bundle().apply {
            putString(Keys.MODEL_NAME, Model.CUSTOMER_PAYMENT)
        }

        view.findNavController()
            .navigate(R.id.modelsListFragment, bundle, AnimationProvider.slideRightLeftNavOptions())
    }

    fun onItemsListBtnClicked(view: View) {
        val bundle = Bundle().apply {
            putString(Keys.MODEL_NAME, Model.USER_ITEM)
        }

        view.findNavController()
            .navigate(R.id.modelsListFragment, bundle, AnimationProvider.slideRightLeftNavOptions())
    }

    fun onReportsBtnClicked(view: View) {
        view.findNavController()
            .navigate(R.id.action_customersHomeFragment_to_customersReportsFragment)
    }

}