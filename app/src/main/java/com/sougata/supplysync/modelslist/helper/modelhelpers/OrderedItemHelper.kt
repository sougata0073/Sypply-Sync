package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemOrderedItemBinding
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlin.reflect.KProperty1

class OrderedItemHelper(
    private val fragment: Fragment,
    private val supplierRepository: SupplierRepository
) : ModelHelper {

    private val context = this.fragment.requireContext()

    override val listHeading: String = "Ordered items"

    @Suppress("UNCHECKED_CAST")
    override fun getProperties(): Array<KProperty1<Model, *>> {
        return arrayOf(
            OrderedItem::supplierItemId, OrderedItem::supplierItemName, OrderedItem::quantity,
            OrderedItem::amount, OrderedItem::supplierId, OrderedItem::supplierName,
            OrderedItem::orderTimestamp, OrderedItem::isReceived
        ) as Array<KProperty1<Model, *>>
    }

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {
        return ItemOrderedItemBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                FieldNames.OrderedItemsCol.ITEM_NAME,
                "Item name",
                FirestoreFieldDataType.STRING
            ),
            Triple(
                FieldNames.OrderedItemsCol.QUANTITY,
                "Quantity",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.OrderedItemsCol.AMOUNT,
                "Amount",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.OrderedItemsCol.SUPPLIER_NAME,
                "Supplier name",
                FirestoreFieldDataType.STRING
            ),
            Triple(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                "Order time",
                FirestoreFieldDataType.TIMESTAMP
            )
        )
    }

    override fun getFilterableFields(): Array<Pair<String, (Model) -> Boolean>> {
        return arrayOf(
            "Received" to { model ->
                (model as OrderedItem).isReceived
            },
            "Not Received" to { model ->
                (model as OrderedItem).isReceived.not()
            }
        )
    }

    override fun getFabClickHandler(): () -> Unit {
        return {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_ADD_KEY, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditOrderedItemFragment,
                bundle,
                AnimationProvider.slideRightLeftNavOptions()
            )
        }
    }

    override fun bind(binding: ViewDataBinding, model: Model) {
        binding as ItemOrderedItemBinding
        model as OrderedItem

        binding.apply {

            itemName.text = model.supplierItemName
            date.text = DateTime.getDateStringFromTimestamp(model.orderTimestamp)
            amount.text = Converters.numberToMoneyString(model.amount)

            if (model.isReceived) {
                receiveStatus.text = "Received"
                receiveStatus.setTextColor(this@OrderedItemHelper.context.getColor(R.color.green))
            } else {
                receiveStatus.text = "Not received"
                receiveStatus.setTextColor(this@OrderedItemHelper.context.getColor(R.color.red))
            }

            root.setOnClickListener {

                val message =
                    "Supplier name: ${model.supplierName}\nItem quantity: ${model.quantity}"

                MaterialAlertDialogBuilder(
                    this@OrderedItemHelper.context,
                    R.style.materialAlertDialogStyle
                ).setTitle(model.supplierItemName)
                    .setMessage(message)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .setNeutralButton("Edit") { dialog, _ ->
                        val bundle = Bundle().apply {
                            putBoolean(KeysAndMessages.TO_EDIT_KEY, true)
                            putParcelable("orderedItem", model)
                        }
                        this@OrderedItemHelper.fragment.findNavController()
                            .navigate(
                                R.id.addEditOrderedItemFragment,
                                bundle,
                                AnimationProvider.slideRightLeftNavOptions()
                            )
                    }.show()
            }
        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.supplierRepository.getOrderedItemsList(
            lastDocumentSnapshot,
            limit,
            onComplete
        )
    }

    override fun fetchListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.supplierRepository.getOrderedItemsListFiltered(
            searchField,
            searchQuery,
            queryDataType,
            lastDocumentSnapshot,
            limit,
            onComplete
        )
    }

    override fun loadFullListOnNewModelAdded(): Boolean {
        return false
    }

}