package com.sougata.supplysync.util.modelslist

import androidx.recyclerview.widget.DiffUtil
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment

class ModelsListDifUtil(
    private val oldList: List<Model>,
    private val newList: List<Model>,
    private val modelName: String
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return this.oldList.size
    }

    override fun getNewListSize(): Int {
        return this.newList.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return this.oldList[oldItemPosition].id == this.newList[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {

        val isSame = this.getContentComparator(this.modelName)(
            this.oldList,
            this.newList,
            oldItemPosition,
            newItemPosition
        )

        return isSame
    }

    fun getContentComparator(modelName: String): (List<Model>, List<Model>, Int, Int) -> Boolean {
        return when (modelName) {
            // Passing properties of each model
            Model.SUPPLIER -> createComparator<Supplier>(
                Supplier::name, Supplier::dueAmount, Supplier::phone,
                Supplier::email, Supplier::note, Supplier::paymentDetails,
                Supplier::profileImageUrl, Supplier::timestamp
            )

            Model.SUPPLIERS_ITEM -> createComparator<SupplierItem>(
                SupplierItem::name,
                SupplierItem::price,
                SupplierItem::details,
                SupplierItem::timestamp
            )

            Model.SUPPLIER_PAYMENT -> createComparator<SupplierPayment>(
                SupplierPayment::amount, SupplierPayment::year, SupplierPayment::month,
                SupplierPayment::date, SupplierPayment::hour, SupplierPayment::minute,
                SupplierPayment::note, SupplierPayment::supplierId, SupplierPayment::timestamp
            )

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    private inline fun <reified T : Model> createComparator(
        vararg properties: (T) -> Any?
    ): (List<Model>, List<Model>, Int, Int) -> Boolean {
        return { oldList, newList, oldPos, newPos ->
            val oldItem = oldList[oldPos] as T
            val newItem = newList[newPos] as T
            properties.all { it(oldItem) == it(newItem) }
        }
    }


}