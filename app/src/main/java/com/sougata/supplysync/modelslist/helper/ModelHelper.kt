package com.sougata.supplysync.modelslist.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Status
import kotlin.reflect.KProperty1

interface ModelHelper {
    val listHeading: String
    fun getProperties(): Array<KProperty1<Model, *>>
    fun getViewToInflate(inflater: LayoutInflater, parent: ViewGroup): ViewDataBinding
    fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>>
    fun getFilterableFields(): Array<Pair<String, (Model) -> Boolean>>
    fun getFabClickHandler(): () -> Unit
    fun bind(binding: ViewDataBinding, model: Model)
    fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    )
    fun fetchListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    )
    fun loadFullListOnNewModelAdded(): Boolean
}