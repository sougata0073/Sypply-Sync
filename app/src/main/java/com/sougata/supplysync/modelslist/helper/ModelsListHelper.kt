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
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.modelslist.helper.modelhelpers.OrderedItemHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.SupplierHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.SupplierItemHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.SupplierPaymentHelper

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
            Model.SUPPLIER -> this.supplierHelper::bind
            Model.SUPPLIERS_ITEM -> this.supplierItemHelper::bind
            Model.SUPPLIER_PAYMENT -> this.supplierPaymentHelper::bind
            Model.ORDERED_ITEM -> this.orderedItemHelper::bind

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getWhatToDoOnFabClick(): () -> Unit {
        return when (this.modelName) {
            Model.SUPPLIER -> this.supplierHelper.getFabClickHandler()
            Model.SUPPLIERS_ITEM -> this.supplierItemHelper.getFabClickHandler()
            Model.SUPPLIER_PAYMENT -> this.supplierPaymentHelper.getFabClickHandler()
            Model.ORDERED_ITEM -> this.orderedItemHelper.getFabClickHandler()

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getSearchableModelFieldPair(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return when (this.modelName) {
            Model.SUPPLIER -> this.supplierHelper.getFieldsPair()
            Model.SUPPLIERS_ITEM -> this.supplierItemHelper.getFieldsPair()
            Model.SUPPLIER_PAYMENT -> this.supplierPaymentHelper.getFieldsPair()
            Model.ORDERED_ITEM -> this.orderedItemHelper.getFieldsPair()

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getWhichViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {

        return when (this.modelName) {
            Model.SUPPLIER -> this.supplierHelper.getViewToInflate(inflater, parent)
            Model.SUPPLIERS_ITEM -> this.supplierItemHelper.getViewToInflate(
                inflater,
                parent
            )

            Model.SUPPLIER_PAYMENT -> this.supplierPaymentHelper.getViewToInflate(
                inflater,
                parent
            )

            Model.ORDERED_ITEM -> this.orderedItemHelper.getViewToInflate(
                inflater,
                parent
            )

            else -> throw Exception("Unknown model type")
        }
    }

    fun getContentComparator(): (List<Model>, List<Model>, Int, Int) -> Boolean {
        return when (this.modelName) {
            Model.SUPPLIER -> createComparator<Supplier>(
                *this.supplierHelper.getProperties()
            )

            Model.SUPPLIERS_ITEM -> createComparator<SupplierItem>(
                *this.supplierItemHelper.getProperties()
            )

            Model.SUPPLIER_PAYMENT -> createComparator<SupplierPayment>(
                *this.supplierPaymentHelper.getProperties()
            )

            Model.ORDERED_ITEM -> createComparator<OrderedItem>(
                *this.orderedItemHelper.getProperties()
            )

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getHeading(): String {
        return when (this.modelName) {
            Model.SUPPLIER -> this.supplierHelper.listHeading
            Model.SUPPLIERS_ITEM -> this.supplierItemHelper.listHeading
            Model.SUPPLIER_PAYMENT -> this.supplierPaymentHelper.listHeading
            Model.ORDERED_ITEM -> this.orderedItemHelper.listHeading

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