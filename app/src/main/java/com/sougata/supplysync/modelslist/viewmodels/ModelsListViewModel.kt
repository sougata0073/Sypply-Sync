package com.sougata.supplysync.modelslist.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ModelsListViewModel(private val helper: ModelsListHelper) : ViewModel() {

    private val pageLoadLimit: Long = 20
    private val listLoaderNormal = this.helper.getWhichListToFetch()
    private val listLoaderSearch = this.helper.getWhichListToFetchFiltered()

    // Normal
    var itemsListNormal: MutableList<Model>? = null
    var noMoreItemNormal = false
    var lastDocNormal: DocumentSnapshot? = null

    // Search
    var isSearchActive = false
    var isSearchClicked = false
    var itemsListSearch: MutableList<Model>? = null
    var noMoreItemSearch = false
    var lastDocSearch: DocumentSnapshot? = null

    var prevSearchQuery = ""
    var prevSearchField = ""
    var prevQueryDataType = FirestoreFieldDataType.STRING

    // Clean
    val itemsList = MutableLiveData<Pair<List<Model>, Status>>()
    val noMoreItem: Boolean
        get() {
            return if (this.isSearchActive) {
                this.noMoreItemSearch
            } else {
                this.noMoreItemNormal
            }
        }

    // Filter
    var isFilterActive = false
    var filter: ((Model) -> Boolean)? = null

    // Flags
    var isFirstTimeListLoad = true
    var isFirstTimeListLoadNormal = true
    var isFirstTimeListLoadSearch = true
    var isModelAdded = false
    var isModelUpdated = false
    var isModelRemoved = false

    val loadFullListOnNewModelAdded = this.helper.getLoadFullListOnNewModelAdded()


    init {
        this.viewModelScope.launch {
            delay(200)
            loadItemsList()
        }
    }

    fun loadItemsList(
        searchData: Triple<String, String, FirestoreFieldDataType>? = null
    ) {
        if (searchData != null) {
            this.isSearchActive = true
            this.loadItemsListSearch(
                searchData.first,
                searchData.second,
                searchData.third
            )
        } else {
            if (this.isSearchActive) {
                this.isSearchActive = false
                this.prevSearchQuery = ""
                this.prevSearchField = ""
                this.prevQueryDataType = FirestoreFieldDataType.STRING
                this.itemsList.value = Pair(mutableListOf(), Status.NO_CHANGE)
            }
            this.loadItemsListNormal()
        }
    }

    fun loadItemsListNormal() {
        if (this.isFirstTimeListLoadNormal) {
            this.loadListNormalFirstTime()
        } else {
            this.loadListNormalNextTime()
        }
    }

    fun loadItemsListSearch(
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
            this.itemsListSearch = null
            this.itemsList.value = Pair(mutableListOf(), Status.NO_CHANGE)
            this.noMoreItemSearch = false
            this.lastDocSearch = null
            this.isFirstTimeListLoadSearch = true

            this.prevSearchField = searchField
            this.prevSearchQuery = searchQuery
            this.prevQueryDataType = queryDataType
        }

        if (this.isFirstTimeListLoadSearch) {
            this.loadListSearchFirstTime(searchField, searchQuery, queryDataType)
        } else {
            this.loadListSearchNextTime(searchField, searchQuery, queryDataType)
        }
    }

    fun setItems(list: List<Model>, status: Status) {
        if (this.isSearchActive) {
            this.itemsList.value = Pair(list, status)
        } else if (this.isFilterActive) {
            val newList = list.filter { this.filter!!(it) }.toMutableList()
            this.itemsList.value = Pair(newList, status)
        } else {
            this.itemsList.value = Pair(list, status)
        }
    }

    fun activateSearchClick() {
        this.isSearchClicked = true
        this.closeFilter()
    }

    fun activateSearch() {
        if (this.isSearchActive) {
            return
        }

        this.isSearchActive = true
        this.isSearchClicked = true
        this.prevSearchQuery = ""
        this.prevSearchField = ""
        this.prevQueryDataType = FirestoreFieldDataType.STRING
        this.itemsListSearch = null
    }

    fun closeSearch() {
        if (!this.isSearchActive) {
            return
        }

        this.isFirstTimeListLoadSearch = true
        this.isSearchActive = false
        this.isSearchClicked = false
        this.prevSearchQuery = ""
        this.prevSearchField = ""
        this.prevQueryDataType = FirestoreFieldDataType.STRING
        this.itemsListSearch = null

        this.setItems(this.itemsListNormal!!, Status.SUCCESS)
    }

    fun activateFilter(filter: (Model) -> Boolean) {
        this.isFilterActive = true
        this.filter = filter

        this.setItems(this.itemsListNormal!!, Status.SUCCESS)
    }

    fun closeFilter() {
        this.isFilterActive = false
        this.filter = null

        this.setItems(this.itemsListNormal!!, Status.SUCCESS)
    }

    private fun loadListNormalFirstTime() {
        this.itemsList.value = Pair(mutableListOf(), Status.STARTED)

        this.listLoaderNormal(null, this.pageLoadLimit) { status, list, lastDoc, message ->
            if (status == Status.SUCCESS) {
                if (list!!.isEmpty()) {
                    this.noMoreItemNormal = true
                    this.lastDocNormal = null
                    this.itemsListNormal = null
                    this.itemsList.value = Pair(mutableListOf(), Status.SUCCESS)
                } else {
                    this.isFirstTimeListLoadNormal = false
                    this.noMoreItemNormal = false
                    this.lastDocNormal = lastDoc
                    this.itemsListNormal = list
                    this.setItems(list, Status.SUCCESS)
                }
            } else if (status == Status.FAILED) {
                this.itemsList.value = Pair(mutableListOf(), Status.FAILED)
            }
        }
    }

    private fun loadListNormalNextTime() {
        this.itemsList.value = Pair(mutableListOf(), Status.STARTED)

        this.listLoaderNormal(
            this.lastDocNormal,
            this.pageLoadLimit
        ) { status, list, lastDoc, message ->
            if (status == Status.SUCCESS) {
                if (list!!.isEmpty()) {
                    this.noMoreItemNormal = true
                    this.setItems(this.itemsListNormal!!, Status.SUCCESS)
                } else {
                    this.noMoreItemNormal = false
                    this.lastDocNormal = lastDoc
                    this.itemsListNormal!!.addAll(list)
                    this.setItems(this.itemsListNormal!!, Status.SUCCESS)
                }
            } else if (status == Status.FAILED) {
                this.itemsList.value = Pair(this.itemsListNormal!!, Status.FAILED)
            }
        }
    }

    private fun loadListSearchFirstTime(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType
    ) {
        this.itemsList.value = Pair(mutableListOf(), Status.STARTED)

        this.listLoaderSearch(
            searchField,
            searchQuery,
            queryDataType,
            null,
            this.pageLoadLimit
        ) { status, list, lastDoc, message ->
            if (status == Status.SUCCESS) {
                if (list!!.isEmpty()) {
                    this.noMoreItemSearch = true
                    this.lastDocSearch = null
                    this.itemsListSearch = null
                    this.setItems(mutableListOf(), Status.SUCCESS)
                } else {
                    this.isFirstTimeListLoadSearch = false
                    this.noMoreItemSearch = false
                    this.lastDocSearch = lastDoc
                    this.itemsListSearch = list
                    this.setItems(list, Status.SUCCESS)
                }
            } else if (status == Status.FAILED) {
                this.itemsList.value = Pair(mutableListOf(), Status.FAILED)
            }
        }
    }

    private fun loadListSearchNextTime(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType
    ) {
        this.itemsList.value = Pair(mutableListOf(), Status.STARTED)

        this.listLoaderSearch(
            searchField,
            searchQuery,
            queryDataType,
            this.lastDocSearch,
            this.pageLoadLimit
        ) { status, list, lastDoc, message ->
            if (status == Status.SUCCESS) {
                if (list!!.isEmpty()) {
                    this.noMoreItemSearch = true
                    this.setItems(this.itemsListSearch!!, Status.SUCCESS)
                } else {
                    this.noMoreItemSearch = false
                    this.lastDocSearch = lastDoc
                    this.itemsListSearch!!.addAll(list)
                    this.setItems(this.itemsListSearch!!, Status.SUCCESS)
                }
            } else if (status == Status.FAILED) {
                this.itemsList.value = Pair(this.itemsListNormal!!, Status.FAILED)
            }
        }
    }

    fun deleteModel(model: Model) {
        var isListUpdated = false

        this.itemsListNormal?.remove(model)

        if (this.isSearchActive) {
            isListUpdated = true
            this.itemsListSearch?.remove(model)
            this.setItems(this.itemsListSearch!!, Status.SUCCESS)
        }
        if (!isListUpdated) {
            this.setItems(this.itemsListNormal!!, Status.SUCCESS)
        }
    }

    fun updateModel(model: Model) {
        var isListUpdated = false

        var updateIndexNormal = -1

        this.itemsListNormal?.forEachIndexed { index, item ->
            if (item.id == model.id) {
                updateIndexNormal = index
                return@forEachIndexed
            }
        }

        if (updateIndexNormal != -1) {
            this.itemsListNormal?.set(updateIndexNormal, model)
        }

        if (this.isSearchActive) {
            var updateIndexSearch = -1
            this.itemsListSearch?.forEachIndexed { index, item ->
                if (item.id == model.id) {
                    updateIndexSearch = index
                    return@forEachIndexed
                }
            }
            if (updateIndexSearch != -1) {
                isListUpdated = true
                this.itemsListSearch?.set(updateIndexSearch, model)
                this.setItems(this.itemsListSearch!!, Status.SUCCESS)
            }
        }

        if (!isListUpdated) {
            this.setItems(this.itemsListNormal!!, Status.SUCCESS)
        }

    }

    fun loadLastAddedModel() {
        if (this.isSearchActive) {
            if (this.loadFullListOnNewModelAdded) {
                this.isFirstTimeListLoadSearch = true
                this.noMoreItemSearch = false
                this.lastDocSearch = null
                this.itemsListSearch = null
                this.itemsList.value = Pair(mutableListOf(), Status.NO_CHANGE)
            }
            this.loadItemsListSearch(
                this.prevSearchField,
                this.prevSearchQuery,
                this.prevQueryDataType
            )
        }
        if (this.loadFullListOnNewModelAdded) {
            this.isFirstTimeListLoadNormal = true
            this.noMoreItemNormal = false
            this.lastDocNormal = null
            this.itemsListNormal = null
            this.itemsList.value = Pair(mutableListOf(), Status.NO_CHANGE)
        }
        this.loadItemsListNormal()
    }
}