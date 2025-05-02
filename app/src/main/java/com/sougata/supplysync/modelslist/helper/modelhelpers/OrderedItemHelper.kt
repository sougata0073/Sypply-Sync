package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sougata.supplysync.R
import com.sougata.supplysync.remote.FirestoreFieldNames
import com.sougata.supplysync.databinding.ItemOrderedItemsListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.util.DataType
import com.sougata.supplysync.modelslist.helper.HelperStructure
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
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

    override fun getFieldsPair(): Array<Triple<String, String, DataType>> {
        return arrayOf(
            Triple(
                FirestoreFieldNames.OrderedItemsCol.ITEM_NAME,
                OrderedItem::itemName.name,
                DataType.STRING
            ),
            Triple(
                FirestoreFieldNames.OrderedItemsCol.QUANTITY,
                OrderedItem::quantity.name,
                DataType.NUMBER
            ),
            Triple(
                FirestoreFieldNames.OrderedItemsCol.AMOUNT,
                OrderedItem::amount.name,
                DataType.NUMBER
            ),
            Triple(
                FirestoreFieldNames.OrderedItemsCol.SUPPLIER_NAME,
                OrderedItem::supplierName.name,
                DataType.STRING
            ),
            Triple(
                FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                OrderedItem::orderTimestamp.name,
                DataType.TIMESTAMP
            )
        )
    }

    override fun getFabClickHandler(): () -> Unit {
        return {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_ADD_KEY, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditOrderedItemFragment, bundle, AnimationProvider.fragmentAnimationSlideRightLeft()
            )
        }
    }

    override fun bind(binding: ViewDataBinding, model: Model) {
        binding as ItemOrderedItemsListBinding
        model as OrderedItem

        binding.apply {

            var year = 0
            var month = 0
            var myDate = 0

            Converters.getDateFromTimestamp(model.orderTimestamp).apply {
                year = first
                month = second
                myDate = third
            }

            val dateString = String.Companion.format(
                Locale.getDefault(),
                "On: %02d-%02d-%04d",
                myDate, month, year
            )

            itemName.text = model.itemName
            date.text = dateString
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
                                AnimationProvider.fragmentAnimationSlideRightLeft()
                            )
                    }.show()
            }
        }
    }

}