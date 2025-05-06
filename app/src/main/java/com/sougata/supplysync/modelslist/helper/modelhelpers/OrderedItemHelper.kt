package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.databinding.ItemOrderedItemsListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.modelslist.helper.HelperStructure
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.KeysAndMessages
import java.util.Locale
import kotlin.reflect.KProperty1

class OrderedItemHelper(private val fragment: Fragment): HelperStructure {

    private val context = this.fragment.requireContext()

    override val listHeading: String = "Ordered items"

    @Suppress("UNCHECKED_CAST")
    override fun getProperties(): Array<KProperty1<Model, *>> {
        return arrayOf(
            OrderedItem::itemId, OrderedItem::itemName, OrderedItem::quantity,
            OrderedItem::amount, OrderedItem::supplierId, OrderedItem::supplierName,
            OrderedItem::orderTimestamp, OrderedItem::isReceived
        ) as Array<KProperty1<Model, *>>
    }

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {
        return ItemOrderedItemsListBinding.inflate(inflater, parent, false)
    }

    override fun getFieldsPair(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                FieldNames.OrderedItemsCol.ITEM_NAME,
                OrderedItem::itemName.name,
                FirestoreFieldDataType.STRING
            ),
            Triple(
                FieldNames.OrderedItemsCol.QUANTITY,
                OrderedItem::quantity.name,
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.OrderedItemsCol.AMOUNT,
                OrderedItem::amount.name,
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.OrderedItemsCol.SUPPLIER_NAME,
                OrderedItem::supplierName.name,
                FirestoreFieldDataType.STRING
            ),
            Triple(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                OrderedItem::orderTimestamp.name,
                FirestoreFieldDataType.TIMESTAMP
            )
        )
    }

    override fun getFabClickHandler(): () -> Unit {
        return {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_ADD_KEY, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditOrderedItemFragment, bundle, AnimationProvider.slideRightLeftNavOptions()
            )
        }
    }

    override fun bind(binding: ViewDataBinding, model: Model) {
        binding as ItemOrderedItemsListBinding
        model as OrderedItem

        binding.apply {

            itemName.text = model.itemName
            date.text = DateTime.getDateStringFromTimestamp(model.orderTimestamp)
            amount.text = Converters.numberToMoneyString(model.amount)

            root.setOnClickListener {

                val message =
                    "Supplier name: ${model.supplierName}\nItem quantity: ${model.quantity}"

                MaterialAlertDialogBuilder(
                    this@OrderedItemHelper.context,
                    R.style.materialAlertDialogStyle
                ).setTitle(model.itemName)
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

}