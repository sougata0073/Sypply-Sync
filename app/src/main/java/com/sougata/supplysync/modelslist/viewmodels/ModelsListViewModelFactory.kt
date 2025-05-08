package com.sougata.supplysync.modelslist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class ModelsListViewModelFactory(
    private val modelName: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return when {
            modelClass.isAssignableFrom(ModelsListRegularViewModel::class.java) ->
                ModelsListRegularViewModel(this.modelName) as T

            modelClass.isAssignableFrom(ModelSearchViewModel::class.java) ->
                ModelSearchViewModel(this.modelName) as T

            else -> throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}