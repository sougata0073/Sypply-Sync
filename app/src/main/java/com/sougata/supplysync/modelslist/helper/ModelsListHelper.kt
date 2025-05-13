package com.sougata.supplysync.modelslist.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.modelhelpers.CustomerHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.CustomerPaymentHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.OrderHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.OrderedItemHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.SupplierHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.SupplierItemHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.SupplierPaymentHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.UserItemHelper

class ModelsListHelper(
    private val modelName: String,
    private val fragment: Fragment
) {

    val supplierRepository by lazy { SupplierRepository() }
    val customerRepository by lazy { CustomerRepository() }

    // Add new helpers to the map

    private val orderedItemHelper by lazy {
        OrderedItemHelper(
            this.fragment,
            this.supplierRepository
        )
    }

    private val supplierHelper by lazy {
        SupplierHelper(this.fragment, this.supplierRepository)
    }
    private val supplierItemHelper by lazy {
        SupplierItemHelper(
            this.fragment,
            this.supplierRepository
        )
    }
    private val supplierPaymentHelper by lazy {
        SupplierPaymentHelper(
            this.fragment,
            this.supplierRepository
        )
    }
    private val customerHelper by lazy {
        CustomerHelper(
            this.fragment,
            this.customerRepository
        )
    }
    private val customerPaymentHelper by lazy {
        CustomerPaymentHelper(
            this.fragment,
            this.customerRepository
        )
    }
    private val orderHelper by lazy {
        OrderHelper(
            this.fragment,
            this.customerRepository
        )
    }
    private val userItemHelper by lazy {
        UserItemHelper(
            this.fragment,
            this.customerRepository
        )
    }

    private fun getHelper() =
        when (this.modelName) {
            Model.SUPPLIER -> this.supplierHelper
            Model.SUPPLIER_ITEM -> this.supplierItemHelper
            Model.SUPPLIER_PAYMENT -> this.supplierPaymentHelper
            Model.ORDERED_ITEM -> this.orderedItemHelper
            Model.CUSTOMER -> this.customerHelper
            Model.CUSTOMER_PAYMENT -> this.customerPaymentHelper
            Model.ORDER -> this.orderHelper
            Model.USER_ITEM -> this.userItemHelper
            else -> throw IllegalArgumentException("Unknown model type")
        }


    fun bind(binding: ViewDataBinding, model: Model) {
        val helper = getHelper()

        helper.bind(binding, model)
    }

    fun getWhatToDoOnFabClick() = this.getHelper().getFabClickHandler()

    fun getSearchableFieldPairs() = this.getHelper().getSearchableFieldPairs()

    fun getFilterableFields() = this.getHelper().getFilterableFields()

    fun getWhichViewToInflate(inflater: LayoutInflater, parent: ViewGroup) =
        this.getHelper().getViewToInflate(inflater, parent)

    fun getHeading() = this.getHelper().listHeading

    fun getWhichListToFetch() = this.getHelper()::fetchList

    fun getWhichListToFetchFiltered() = this.getHelper()::fetchListFiltered

    fun getLoadFullListOnNewModelAdded() = this.getHelper().loadFullListOnNewModelAdded()

    fun getContentComparator(
        newList: List<Model>,
        oldList: List<Model>,
        newPosition: Int,
        oldPosition: Int
    ): Boolean {
        val helper = getHelper()
        return helper.getContentComparator(
            newList,
            oldList,
            newPosition,
            oldPosition
        )
    }

}