package com.sougata.supplysync.modelslist.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.modelslist.DataType

class ModelsListHelper(
    private val modelName: String,
    private val fragment: Fragment
) {

    // Changes needed when new models are added
    // getWhatToDoOnBind(), getWhatToDoOnFabClick(), getSearchableModelFieldPair()
    // getWhichViewToInflate(), getContentComparator(),

    private val orderedItemHelper = OrderedItemHelper(this.fragment)
    private val supplierHelper = SupplierHelper(this.fragment)
    private val supplierItemHelper = SupplierItemHelper(this.fragment)
    private val supplierPaymentHelper = SupplierPaymentHelper(this.fragment)

    fun getWhatToDoOnBind(): (ViewDataBinding, Model) -> Unit {
        return when (this.modelName) {
            Model.Companion.SUPPLIER -> this.supplierHelper::bind
            Model.Companion.SUPPLIERS_ITEM -> this.supplierItemHelper::bind
            Model.Companion.SUPPLIER_PAYMENT -> this.supplierPaymentHelper::bind
            Model.Companion.ORDERED_ITEM -> this.orderedItemHelper::bind

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getWhatToDoOnFabClick(): () -> Unit {
        return when (this.modelName) {
            Model.Companion.SUPPLIER -> this.supplierHelper.getFabClickHandler()
            Model.Companion.SUPPLIERS_ITEM -> this.supplierItemHelper.getFabClickHandler()
            Model.Companion.SUPPLIER_PAYMENT -> this.supplierPaymentHelper.getFabClickHandler()
            Model.Companion.ORDERED_ITEM -> this.orderedItemHelper.getFabClickHandler()

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getSearchableModelFieldPair(): Array<Triple<String, String, DataType>> {
        return when (this.modelName) {
            Model.Companion.SUPPLIER -> this.supplierHelper.getFieldsPair()
            Model.Companion.SUPPLIERS_ITEM -> this.supplierItemHelper.getFieldsPair()
            Model.Companion.SUPPLIER_PAYMENT -> this.supplierPaymentHelper.getFieldsPair()
            Model.Companion.ORDERED_ITEM -> this.orderedItemHelper.getFieldsPair()

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getWhichViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {

        return when (this.modelName) {
            Model.Companion.SUPPLIER -> this.supplierHelper.getViewToInflate(inflater, parent)
            Model.Companion.SUPPLIERS_ITEM -> this.supplierItemHelper.getViewToInflate(
                inflater,
                parent
            )

            Model.Companion.SUPPLIER_PAYMENT -> this.supplierPaymentHelper.getViewToInflate(
                inflater,
                parent
            )

            Model.Companion.ORDERED_ITEM -> this.orderedItemHelper.getViewToInflate(
                inflater,
                parent
            )

            else -> throw Exception("Unknown model type")
        }
    }

    fun getContentComparator(): (List<Model>, List<Model>, Int, Int) -> Boolean {
        return when (this.modelName) {
            Model.Companion.SUPPLIER -> createComparator<Supplier>(
                *this.supplierHelper.getProperties()
            )

            Model.Companion.SUPPLIERS_ITEM -> createComparator<SupplierItem>(
                *this.supplierItemHelper.getProperties()
            )

            Model.Companion.SUPPLIER_PAYMENT -> createComparator<SupplierPayment>(
                *this.supplierPaymentHelper.getProperties()
            )

            Model.Companion.ORDERED_ITEM -> createComparator<OrderedItem>(
                *this.orderedItemHelper.getProperties()
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