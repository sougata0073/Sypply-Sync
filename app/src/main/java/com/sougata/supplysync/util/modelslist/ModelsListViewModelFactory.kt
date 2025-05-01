package com.sougata.supplysync.util.modelslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sougata.supplysync.util.modelslist.ModelSearchViewModel

@Suppress("UNCHECKED_CAST")
class ModelsListViewModelFactory(
    private val modelName: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return when {
            modelClass.isAssignableFrom(ModelsListViewModel::class.java) ->
                ModelsListViewModel(this.modelName) as T

            modelClass.isAssignableFrom(ModelSearchViewModel::class.java) ->
                ModelSearchViewModel(this.modelName) as T

            else -> throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}