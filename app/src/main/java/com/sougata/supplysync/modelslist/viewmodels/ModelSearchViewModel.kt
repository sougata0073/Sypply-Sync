package com.sougata.supplysync.modelslist.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class ModelSearchViewModel(private val helper: ModelsListHelper) : ViewModel() {

    val supplierRepository = SupplierRepository()

    val itemsList = MutableLiveData<Triple<MutableList<Model>?, Status, String>>()
    var noMoreElementLeft = false
    var lastDocumentSnapshot: DocumentSnapshot? = null

    var prevSearchField = ""
    var prevSearchQuery = ""
    var prevQueryDataType = FirestoreFieldDataType.STRING

    var isSearchActive = false
    var isSearchClicked = false

    var isFirstTimeListLoaded = true

    fun loadItemsList(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType
    ) {

        if (searchQuery.isEmpty()) {
            return
        }

        if (searchField != this.prevSearchField ||
            searchQuery != this.prevSearchQuery ||
            queryDataType != this.prevQueryDataType
        ) {
            this.itemsList.value = Triple(null, Status.NO_CHANGE, "")
            this.noMoreElementLeft = false
            this.lastDocumentSnapshot = null

            this.prevSearchField = searchField
            this.prevSearchQuery = searchQuery
            this.prevQueryDataType = queryDataType
        }

        if (this.itemsList.value?.first == null) {
            this.itemsList.value = Triple(null, Status.STARTED, "")
        } else {
            this.itemsList.value =
                Triple(
                    this.itemsList.value?.first,
                    Status.STARTED,
                    ""
                )
        }

        this.callListByModelName(
            searchField,
            searchQuery,
            queryDataType,
            this.itemsList.value
        )
    }

    private fun callListByModelName(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        value: Triple<MutableList<Model>?, Status, String>?
    ) {
        val limit: Long = 20
        val fetchList = this.helper.getWhichListToFetchFiltered()

        if (value?.first == null) {
            fetchList(
                searchField, searchQuery, queryDataType, null, limit
            ) { status, list, lastDocumentSnapshot, message ->
                if (message == KeysAndMessages.EMPTY_LIST) {
                    this.itemsList.value = Triple(null, status, message)
                    this.noMoreElementLeft = true
                } else {
                    this.itemsList.value = Triple(list, status, message)
                    this.lastDocumentSnapshot = lastDocumentSnapshot
                }
            }
        } else {
            fetchList(
                searchField, searchQuery, queryDataType, this.lastDocumentSnapshot, limit
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
        this.loadItemsList(this.prevSearchField, this.prevSearchQuery, this.prevQueryDataType)
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