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
import com.sougata.supplysync.databinding.ItemOrderBinding
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.modelslist.helper.modelhelpers.OrderedItemHelper
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class OrderHelper(
    private val fragment: Fragment,
    private val customerRepository: CustomerRepository
) : ModelHelper {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading = "Orders"

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemOrderBinding {
        return ItemOrderBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                Order::userItemName.name,
                "Item name", FirestoreFieldDataType.STRING
            ),
            Triple(
                Order::quantity.name,
                "Quantity", FirestoreFieldDataType.NUMBER
            ),
            Triple(
                Order::amount.name,
                "Amount", FirestoreFieldDataType.NUMBER
            ),
            Triple(
                Order::deliveryTimestamp.name,
                "Delivery date", FirestoreFieldDataType.TIMESTAMP
            ),
            Triple(
                Order::customerName.name,
                "Customer name", FirestoreFieldDataType.STRING
            )
        )
    }

    override fun getFilterableFields(): Array<Pair<String, (Model) -> Boolean>> {
        return arrayOf(
            "Delivered" to { order ->
                (order as Order).delivered
            },
            "Not delivered" to { order ->
                (order as Order).delivered.not()
            }
        )
    }

    override fun getFabClickHandler(): () -> Unit {
        return {
            val bundle = Bundle().apply {
                putBoolean(Keys.TO_ADD, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditOrderFragment,
                bundle,
                AnimationProvider.slideRightLeftNavOptions()
            )
        }
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemOrderBinding
        model as Order

        binding.apply {
            itemName.text = model.userItemName
            deliveryDate.text =
                "Delivery date: ${DateTime.getDateStringFromTimestamp(model.deliveryTimestamp)}"
            amount.text = Converters.numberToMoneyString(model.amount)

            if (model.delivered) {
                deliveryStatus.text = "Delivered"
                deliveryStatus.setTextColor(this@OrderHelper.context.getColor(R.color.green))
            } else {
                deliveryStatus.text = "Not delivered"
                deliveryStatus.setTextColor(this@OrderHelper.context.getColor(R.color.red))
            }

            val message = """
                Item name: ${model.userItemName}
                Quantity: ${model.quantity}
                Amount: ${Converters.numberToMoneyString(model.amount)}
                Delivery date: ${DateTime.getDateStringFromTimestamp(model.deliveryTimestamp)}
                ${if (model.delivered) "Status: Delivered" else "Status: Not delivered"}
            """.trimIndent()

            root.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@OrderHelper.context,
                    R.style.materialAlertDialogStyle
                )
                    .setMessage(message)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .setNeutralButton("Edit") { dialog, _ ->

                        val bundle = Bundle().apply {
                            putBoolean(Keys.TO_EDIT, true)
                            putParcelable(Model.ORDER, model)
                        }
                        this@OrderHelper.fragment.findNavController()
                            .navigate(
                                R.id.addEditOrderFragment,
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
        this.customerRepository.getOrdersList(
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
        this.customerRepository.getOrdersListFiltered(
            searchField,
            searchQuery,
            queryDataType,
            lastDocumentSnapshot,
            limit,
            onComplete
        )
    }

    override fun loadFullListOnNewModelAdded(): Boolean {
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun getContentComparator(
        newList: List<Model>,
        oldList: List<Model>,
        newPosition: Int,
        oldPosition: Int
    ): Boolean {
        newList as List<Order>
        oldList as List<Order>

        return when {
            newList[newPosition].timestamp != oldList[oldPosition].timestamp -> false
            newList[newPosition].userItemId != oldList[oldPosition].userItemId -> false
            newList[newPosition].userItemName != oldList[oldPosition].userItemName -> false
            newList[newPosition].quantity != oldList[oldPosition].quantity -> false
            newList[newPosition].amount != oldList[oldPosition].amount -> false
            newList[newPosition].customerId != oldList[oldPosition].customerId -> false
            newList[newPosition].customerName != oldList[oldPosition].customerName -> false
            newList[newPosition].deliveryTimestamp != oldList[oldPosition].deliveryTimestamp -> false
            newList[newPosition].delivered != oldList[oldPosition].delivered -> false
            else -> true
        }
    }
}