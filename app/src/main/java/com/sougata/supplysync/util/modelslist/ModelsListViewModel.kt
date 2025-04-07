package com.sougata.supplysync.util.modelslist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.firebase.FirestoreRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModelsListViewModel(private val modelName: String) :
    ViewModel() {

    val firestoreRepository = FirestoreRepository()
    val itemsList = MutableLiveData<Triple<MutableList<Model>, Int, String>>()
    var noMoreElementLeft = false
    var lastDocumentSnapshot: DocumentSnapshot? = null

    init {
        this.loadListItem()
    }

    fun loadListItem() {

        this.itemsList.postValue(Triple(mutableListOf(), Status.STARTED, ""))

        this.callListByModelName(this.itemsList.value)
    }

    fun loadLastAddedData() {
        this.loadListItem()
    }


    private fun callListByModelName(
        value: Triple<MutableList<Model>, Int, String>?
    ) {
        // The datatype will be equal to the parameters of the function
        val fetchList =
            when (this.modelName) {
                Model.SUPPLIER -> firestoreRepository::getSuppliersList
                Model.SUPPLIERS_ITEM -> firestoreRepository::getSupplierItemsList
                Model.SUPPLIER_PAYMENT -> firestoreRepository::getSupplierPaymentsList
                Model.ORDERED_ITEM -> firestoreRepository::getOrderedItemsList
                else -> return
            }

        fetchList(
            this.viewModelScope,

            // Means if value != null return lastDocumentSnapshot if value == null return null
            value?.let { this.lastDocumentSnapshot },

            // Here same as previous but if return result is also null
            // then elvis operator '?:' will return getLoadListItemOnCompleteWhenNull()
            value?.let { getLoadListItemOnCompleteWhenNotNull(it) }
                ?: getLoadListItemOnCompleteWhenNull()
        )
    }


    private fun getLoadListItemOnCompleteWhenNull(): (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit {
        return { status, list, lastDocumentSnapshot, message ->
            if (message == KeysAndMessages.EMPTY_LIST) {

                this.itemsList.postValue(Triple(list, status, message))
                this.noMoreElementLeft = true

            } else {

                this.itemsList.postValue(Triple(list, status, message))
                this.lastDocumentSnapshot = lastDocumentSnapshot

            }
        }
    }

    private fun getLoadListItemOnCompleteWhenNotNull(
        value: Triple<MutableList<Model>, Int, String>
    ): (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit {
        return { status, list, lastDocumentSnapshot, message ->
            if (message == KeysAndMessages.EMPTY_LIST) {

                this.itemsList.postValue(Triple(value.first, status, ""))
                this.noMoreElementLeft = true

            } else {

                val newList = value.first.apply { addAll(list) }
                this.itemsList.postValue(Triple(newList, status, message))
                this.lastDocumentSnapshot = lastDocumentSnapshot

            }
        }
    }
}