package com.sougata.supplysync.modelslist.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ModelsListRegularViewModel(private val helper: ModelsListHelper) :
    ViewModel() {

    val supplierRepository = SupplierRepository()

    val itemsList = MutableLiveData<Triple<MutableList<Model>?, Int, String>>()
    var noMoreElementLeft = false
    var lastDocumentSnapshot: DocumentSnapshot? = null

    var isFirstTimeListLoaded = true

    var isModelAdded = false
    var isModelUpdated = false
    var isModelRemoved = false

    init {
        this.viewModelScope.launch {
            delay(200)
            loadItemsList()
        }
    }

    fun loadItemsList() {
        if (this.itemsList.value?.first == null) {
            this.itemsList.value = Triple(null, Status.STARTED, "")
        } else {
            this.itemsList.value = Triple(
                this.itemsList.value?.first,
                Status.STARTED,
                ""
            )
        }

        this.callListByModelName(this.itemsList.value)
    }

    private fun callListByModelName(value: Triple<MutableList<Model>?, Int, String>?) {
        val limit: Long = 20
        val fetchList = this.helper.getWhichListToFetch()

        if (value?.first == null) { // Means its the first time the list is loading
            fetchList(null, limit) { status, list, lastDocumentSnapshot, message ->
                if (message == KeysAndMessages.EMPTY_LIST) {
                    this.itemsList.value = Triple(null, status, message)
                    this.noMoreElementLeft = true
                } else {
                    this.itemsList.value = Triple(list, status, message)
                    this.lastDocumentSnapshot = lastDocumentSnapshot
                }
            }
        } else { // Its not the first time the list is loading
            fetchList(
                this.lastDocumentSnapshot,
                limit
            ) { status, list, lastDocumentSnapshot, message ->
                if (message == KeysAndMessages.EMPTY_LIST) {
                    this.itemsList.value = Triple(value.first, status, message)
                    this.noMoreElementLeft = true
                } else {
                    val newList = value.first?.apply { addAll(list!!) }
                    this.itemsList.value = Triple(newList, status, message)
                    this.lastDocumentSnapshot = lastDocumentSnapshot
                }
            }
        }
    }

    fun loadLastAddedData() {
        if (this.helper.getLoadFullListOnNewModelAdded()) {
            this.itemsList.value = Triple(null, Status.NO_CHANGE, "")
            this.noMoreElementLeft = false
            this.lastDocumentSnapshot = null
        }
        this.loadItemsList()
    }

    fun deleteModel(model: Model) {
        val list = this.itemsList.value?.first

        if (list == null) {
            return
        }

        for ((i, item) in list.withIndex()) {
            if (item.id == model.id) {
                list.removeAt(i)
                this.itemsList.value =
                    Triple(
                        list,
                        Status.SUCCESS,
                        if (list.isEmpty()) KeysAndMessages.EMPTY_LIST else KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                break
            }
        }
        if (list.isEmpty()) {
            this.lastDocumentSnapshot = null
            this.noMoreElementLeft = true
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
