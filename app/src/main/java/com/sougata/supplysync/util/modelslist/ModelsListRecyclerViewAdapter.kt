package com.sougata.supplysync.util.modelslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemInfinityScrollProgressBarBinding
import com.sougata.supplysync.databinding.ItemOrderedItemsListBinding
import com.sougata.supplysync.databinding.ItemSupplierItemsListBinding
import com.sougata.supplysync.databinding.ItemSupplierPaymentsListBinding
import com.sougata.supplysync.databinding.ItemSuppliersListBinding
import com.sougata.supplysync.models.DummyModel
import com.sougata.supplysync.models.Model

class ModelsListRecyclerViewAdapter(
    private var itemsList: MutableList<Model>,
    private val onBind: (ViewDataBinding, Model) -> Unit,
    private val modelName: String,
    private val customCallback: ((View, Model) -> Unit)? = null
) :
    RecyclerView.Adapter<ModelsListRecyclerViewAdapter.MyViewHolder>() {

    private val viewTypeLoading = 0
    private val viewTypeNormal = 1

    inner class MyViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Model) {
            if (model !is DummyModel) {
                onBind(this.binding, model)

                customCallback?.invoke(this.binding.root, model)
            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (this.itemsList[position] is DummyModel) {
            viewTypeLoading
        } else {
            viewTypeNormal
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            viewTypeNormal -> {
                val binding: ViewDataBinding = getWhatToBind(this.modelName, inflater, parent)
                return MyViewHolder(binding)
            }

            viewTypeLoading -> {
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
            val prevSize = this.itemsList.size

            this.itemsList.add(DummyModel())

            notifyItemInserted(prevSize)

//            Log.d("loader", "Yes")
        }
    }

    fun setItems(newItemsList: List<Model>) {
        if (this.itemsList.isNotEmpty() && this.itemsList.last() is DummyModel) {
            this.deleteItem(this.itemsList.size - 1)
        }
        val diffUtil = ModelsListDifUtil(this.itemsList, newItemsList, this.modelName)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        this.itemsList = newItemsList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun deleteItem(index: Int) {
        this.itemsList.removeAt(index)
        this.notifyItemRemoved(index)
//        Log.d("loader", "Yes adapter delete")
    }

    fun updateItem(item: Model, index: Int) {
        this.itemsList[index] = item
        notifyItemChanged(index)
    }

    private fun getWhatToBind(
        modelName: String,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {

        return when (modelName) {
            Model.SUPPLIER -> DataBindingUtil.inflate<ItemSuppliersListBinding>(
                inflater, R.layout.item_suppliers_list, parent, false
            )

            Model.SUPPLIERS_ITEM -> DataBindingUtil.inflate<ItemSupplierItemsListBinding>(
                inflater, R.layout.item_supplier_items_list, parent, false
            )

            Model.SUPPLIER_PAYMENT -> DataBindingUtil.inflate<ItemSupplierPaymentsListBinding>(
                inflater, R.layout.item_supplier_payments_list, parent, false
            )

            Model.ORDERED_ITEM -> DataBindingUtil.inflate<ItemOrderedItemsListBinding>(
                inflater, R.layout.item_ordered_items_list, parent, false
            )

            else -> throw Exception("Unknown model type")
        }

    }
}