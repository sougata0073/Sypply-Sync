package com.sougata.supplysync.modelslist.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemInfinityScrollProgressBarBinding
import com.sougata.supplysync.models.DummyModel
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import kotlin.math.abs

class ModelsListRecyclerViewAdapter(
    private var cleanList: MutableList<Model>,
    private var helper: ModelsListHelper,
    private var loadListAgain: MutableLiveData<Boolean>,
    private val extraCallback: ((View, Model) -> Unit)? = null
) :
    RecyclerView.Adapter<ModelsListRecyclerViewAdapter.MyViewHolder>() {

    private var itemsList = this.cleanList.toMutableList()
    private var isFilterActive = false
    private lateinit var filter: (Model) -> Boolean
    private var prevLoadedItemCount = 0

    private val viewTypeLoading = 0
    private val viewTypeNormal = 1

    private val dummyModel = DummyModel()

    inner class MyViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Model) {
            if (model !is DummyModel) {
                helper.getWhatToDoOnBind().invoke(this.binding, model)

                extraCallback?.invoke(this.binding.root, model)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.itemsList[position] is DummyModel) {
            this.viewTypeLoading
        } else {
            this.viewTypeNormal
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            this.viewTypeNormal -> {
                val binding: ViewDataBinding = this.helper.getWhichViewToInflate(inflater, parent)
                return MyViewHolder(binding)
            }

            this.viewTypeLoading -> {
                val binding = DataBindingUtil.inflate<ItemInfinityScrollProgressBarBinding>(
                    inflater, R.layout.item_infinity_scroll_progress_bar, parent, false
                )
                return MyViewHolder(binding)
            }

            else -> throw Exception("Unknown view type")
        }
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.bind(this.itemsList[position])
    }

    override fun getItemCount(): Int {
        return this.itemsList.size
    }

    fun addLoadingAnimation() {
        if (this.itemsList.isNotEmpty() && this.itemsList.last() !is DummyModel) {
            this.itemsList.add(this.dummyModel)
            this.notifyItemInserted(this.itemsList.size - 1)
        }
    }

    fun removeLoadingAnimation() {
        val removedIndex = this.itemsList.indexOf(this.dummyModel)
        if (removedIndex != -1) {
            this.itemsList.removeAt(removedIndex)
            notifyItemRemoved(removedIndex)
        }
    }

    fun removeAllItems() {
        val prevSize = this.itemsList.size
        this.cleanList.clear()
        this.itemsList.clear()
        notifyItemRangeRemoved(0, prevSize)
    }

    fun setItems(newItemsList: MutableList<Model>) {
        this.cleanList = newItemsList

        if (this.isFilterActive) {
            this.filterList(this.filter)
        } else {
            this.setItemsHelper(this.cleanList)
        }
    }

    fun filterList(filter: (Model) -> Boolean) {
        this.isFilterActive = true
        this.filter = filter
        val newList = this.cleanList.filter {
            if (it !is DummyModel) {
                filter(it)
            } else {
                false
            }
        }.toMutableList()

        this.setItemsHelper(newList)
    }

    fun clearFilter() {
        this.isFilterActive = false
        this.setItemsHelper(this.cleanList)
    }

    fun setItemsHelper(newItemsList: MutableList<Model>) {
        this.removeLoadingAnimation()

        val loadedItemCount = abs(this.prevLoadedItemCount - newItemsList.size)
        if (loadedItemCount < 10) {
            this.loadListAgain.value = true
        } else {
            this.loadListAgain.value = false
        }
        this.prevLoadedItemCount = newItemsList.size

        val diffUtil = ModelsListDifUtil(this.itemsList, newItemsList, this.helper)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        this.itemsList = newItemsList
        diffResult.dispatchUpdatesTo(this)
    }

}