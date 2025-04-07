package com.sougata.supplysync.util.modelslist

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemOrderedItemsListBinding
import com.sougata.supplysync.databinding.ItemSupplierItemsListBinding
import com.sougata.supplysync.databinding.ItemSupplierPaymentsListBinding
import com.sougata.supplysync.databinding.ItemSuppliersListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.KeysAndMessages

class ModelsListRecyclerViewAdapter(
    private var itemsList: MutableList<Model>,
    private val onBind: (ViewDataBinding, Model) -> Unit,
    private val modelName: String
) :
    RecyclerView.Adapter<ModelsListRecyclerViewAdapter.MyViewHolder>() {

    private var bottomSheetDialogFragment: BottomSheetDialogFragment? = null

    constructor(
        itemsList: MutableList<Model>,
        onBind: (ViewDataBinding, Model) -> Unit,
        modelName: String,
        bottomSheetDialogFragment: BottomSheetDialogFragment
    ) : this(itemsList, onBind, modelName) {
        this.bottomSheetDialogFragment = bottomSheetDialogFragment
    }


    inner class MyViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Model) {
            onBind(this.binding, model)

            // It will be not null when a select only fragment used this recyclerview adapter
            val bottomSheet = bottomSheetDialogFragment

            if (bottomSheet != null) {

                this.binding.root.setOnClickListener {
                    val bundle = Bundle().apply {
                        putParcelable(KeysAndMessages.MODEL_KEY, model as Parcelable)
                    }
                    bottomSheet.parentFragmentManager.setFragmentResult(
                        KeysAndMessages.ITEM_SELECTED_KEY,
                        bundle
                    )
                    bottomSheet.dismiss()
                }

            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding: ViewDataBinding = getWhatToBind(this.modelName, inflater, parent)

        return MyViewHolder(binding)
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

    fun setData(newItemsList: List<Model>) {
        val diffUtil = ModelsListDifUtil(this.itemsList, newItemsList, this.modelName)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        this.itemsList = newItemsList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun deleteItem(item: Model) {
        val deletedItemIndex = this.itemsList.indexOf(item)
        this.itemsList.remove(item)
        this.notifyItemRemoved(deletedItemIndex)
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

        return when(modelName) {
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