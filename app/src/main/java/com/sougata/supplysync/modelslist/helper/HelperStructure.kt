package com.sougata.supplysync.modelslist.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.FirestoreFieldDataType
import kotlin.reflect.KProperty1

interface HelperStructure {
    val listHeading: String
    fun getProperties(): Array<KProperty1<Model, *>>
    fun getViewToInflate(inflater: LayoutInflater, parent: ViewGroup): ViewDataBinding
    fun getFieldsPair(): Array<Triple<String, String, FirestoreFieldDataType>>
    fun getFabClickHandler(): () -> Unit
    fun bind(binding: ViewDataBinding, model: Model)
}