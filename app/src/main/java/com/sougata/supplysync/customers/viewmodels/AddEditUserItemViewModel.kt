package com.sougata.supplysync.customers.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.util.Status
import java.util.UUID

class AddEditUserItemViewModel : ViewModel() {

    val name = MutableLiveData("")
    val price = MutableLiveData("")
    val inStock = MutableLiveData("")
    val details = MutableLiveData("")

    private val customerRepository = CustomerRepository()

    val userItemAddedIndicator = MutableLiveData<Pair<Status, String>>()
    val userItemEditedIndicator = MutableLiveData<Pair<Status, String>>()
    val userItemDeletedIndicator = MutableLiveData<Pair<Status, String>>()

    fun addUserItem(view: View) {
        val userItem = try {
            this.processUserItem(UUID.randomUUID().toString(), Timestamp.now())
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return
        }

        this.userItemAddedIndicator.value = Status.STARTED to ""

        this.customerRepository.addUserItem(
            userItem
        ) { status, message ->
            this.userItemAddedIndicator.value = status to message
        }
    }

    fun updateUserItem(userItemId: String, timestamp: Timestamp, view: View): UserItem? {
        val userItem = try {
            this.processUserItem(userItemId, timestamp)
        } catch (e: Exception) {
            Snackbar.make(view, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            return null
        }

        this.userItemEditedIndicator.value = Status.STARTED to ""

        this.customerRepository.updateUserItem(
            userItem
        ) { status, message ->
            this.userItemEditedIndicator.value = status to message
        }
        return userItem
    }

    fun deleteUserItem(userItem: UserItem) {

        this.userItemDeletedIndicator.value = Status.STARTED to ""

        this.customerRepository.deleteUserItem(userItem) { status, message ->
            this.userItemDeletedIndicator.value = status to message
        }
    }

    private fun processUserItem(userItemId: String, timestamp: Timestamp): UserItem {
        val name = this.name.value.orEmpty()
        val priceString = this.price.value.orEmpty()
        val inStockString = this.inStock.value.orEmpty()
        val details = this.details.value.orEmpty()

        if (name.isEmpty()) {
            throw Exception("Name can't be empty")
        }

        var price = 0.0
        var inStock = 0

        if (priceString.isNotEmpty()) {
            try {
                price = priceString.toDouble()
            } catch (_: Exception) {
                throw Exception("Invalid price")
            }
        }

        if (inStockString.isNotEmpty()) {
            try {
                inStock = inStockString.toInt()
            } catch (_: Exception) {
                throw Exception("Invalid number")
            }
        }

        return UserItem(userItemId, timestamp, name, inStock, price, details)
    }

}