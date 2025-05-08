package com.sougata.supplysync.modelslist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sougata.supplysync.modelslist.helper.ModelsListHelper

@Suppress("UNCHECKED_CAST")
class ModelsListViewModelFactory(
    private val modelsListHelper: ModelsListHelper
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return when {
            modelClass.isAssignableFrom(ModelsListRegularViewModel::class.java) ->
                ModelsListRegularViewModel(this.modelsListHelper) as T

            modelClass.isAssignableFrom(ModelSearchViewModel::class.java) ->
                ModelSearchViewModel(this.modelsListHelper) as T

            else -> throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}