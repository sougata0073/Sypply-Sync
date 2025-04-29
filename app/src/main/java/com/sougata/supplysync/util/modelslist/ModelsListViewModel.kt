package com.sougata.supplysync.util.modelslist

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.cloud.SupplierFirestoreRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class ModelsListViewModel(private val modelName: String) :
    ViewModel() {

    val supplierFirestoreRepository = SupplierFirestoreRepository()
    val itemsList = MutableLiveData<Triple<MutableList<Model>?, Int, String>>()
    var noMoreElementLeft = false
    var lastDocumentSnapshot: DocumentSnapshot? = null

    var isFirstTimeListLoaded = true

    var isModelAdded = false
    var isModelUpdated = false
    var isModelRemoved = false

    init {
        this.loadListItem()
//        Log.d("list", "init")
    }

    fun loadListItem() {
        if (this.itemsList.value == null) {
            this.itemsList.postValue(Triple(null, Status.STARTED, ""))
        } else {
            this.itemsList.postValue(Triple(this.itemsList.value?.first, Status.STARTED, ""))
        }

        this.callListByModelName(this.itemsList.value)
    }

    fun loadLastAddedData() {
        this.loadListItem()
    }

    private fun callListByModelName(
        value: Triple<MutableList<Model>?, Int, String>?
    ) {
        val limit: Long = 20
        val fetchList =
            when (this.modelName) {
                Model.SUPPLIER -> supplierFirestoreRepository::getSuppliersList
                Model.SUPPLIERS_ITEM -> supplierFirestoreRepository::getSupplierItemsList
                Model.SUPPLIER_PAYMENT -> supplierFirestoreRepository::getSupplierPaymentsList
                Model.ORDERED_ITEM -> supplierFirestoreRepository::getOrderedItemsList
                else -> return
            }


        if (value == null) { // Means its the first time the list is loading
            fetchList(null, limit) { status, list, lastDocumentSnapshot, message ->
//                Log.d("list", status.toString() + message + list.toString())
                if (message == KeysAndMessages.EMPTY_LIST) {
                    this.itemsList.postValue(Triple(null, status, message))
                    this.noMoreElementLeft = true
                } else {
                    this.itemsList.postValue(Triple(list, status, message))
                    this.lastDocumentSnapshot = lastDocumentSnapshot
                }
            }
        } else { // Its not the first time the list is loading
            fetchList(
                this.lastDocumentSnapshot,
                limit
            ) { status, list, lastDocumentSnapshot, message ->
                if (message == KeysAndMessages.EMPTY_LIST) {
                    this.itemsList.postValue(Triple(value.first, status, message))
                    this.noMoreElementLeft = true
                } else {
                    val newList = value.first?.apply { addAll(list!!) }
                    this.itemsList.postValue(Triple(newList, status, message))
                    this.lastDocumentSnapshot = lastDocumentSnapshot
                }
            }
        }
    }

    fun deleteModel(model: Model) {
        val list = this.itemsList.value?.first

        if (list == null) {
            return
        }

        for ((i, item) in list.withIndex()) {
            if (item.id == model.id) {
                list.removeAt(i)
                this.itemsList.postValue(
                    Triple(
                        list,
                        Status.SUCCESS,
                        if (list.isEmpty()) KeysAndMessages.EMPTY_LIST else KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                )
                return
            }
        }
    }

    fun updateModel(model: Model) {
        val list = this.itemsList.value?.first

        if (list == null) {
            return
        }

        for ((i, item) in list.withIndex()) {
            if (item.id == model.id) {
                list[i] = model
                this.itemsList.postValue(
                    Triple(list, Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
                )
                return
            }
        }
    }

}