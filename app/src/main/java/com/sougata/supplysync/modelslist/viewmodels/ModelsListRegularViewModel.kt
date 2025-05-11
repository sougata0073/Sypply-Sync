package com.sougata.supplysync.modelslist.viewmodels

import android.util.Log
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
import java.util.UUID
import kotlin.uuid.Uuid

class ModelsListRegularViewModel(private val helper: ModelsListHelper) :
    ViewModel() {

    val supplierRepository = SupplierRepository()

    val itemsList = MutableLiveData<Triple<MutableList<Model>?, Status, String>>()
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

    private fun callListByModelName(value: Triple<MutableList<Model>?, Status, String>?) {
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
        val data = this.itemsList.value
        if (data == null) {
            return
        }
        val prevStatus = data.second
        val prevMessage = data.third

        val newList = data.first
        var deleteIndex = -1

        if (newList == null) {
            return
        }

        for ((i, item) in newList.withIndex()) {
            if (item.id == model.id) {
                deleteIndex = i
                break
            }
        }

        if (deleteIndex != -1) {
            newList.removeAt(deleteIndex)
            if (newList.isEmpty()) {
                this.lastDocumentSnapshot = null
                this.noMoreElementLeft = true
            }
            this.itemsList.value =
                Triple(newList, prevStatus, prevMessage)
        }
    }

    fun updateModel(model: Model) {
        val prevData = this.itemsList.value
        if (prevData == null) {
            return
        }
        val prevStatus = prevData.second
        val prevMessage = prevData.third

        val newList = prevData.first

        if (newList == null) {
            return
        }

        for ((i, item) in newList.withIndex()) {
            if (item.id == model.id) {
                newList[i] = model
                break
            }
        }

        this.itemsList.value =
            Triple(newList, prevStatus, prevMessage)
    }
}
