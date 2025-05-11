package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemOrderBinding
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Status
import kotlin.reflect.KProperty1

class OrderHelper(
    private val fragment: Fragment,
    private val customerRepository: CustomerRepository
) : ModelHelper  {

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
                FieldNames.OrdersCol.USER_ITEM_NAME,
                "Item name", FirestoreFieldDataType.STRING
            ),
            Triple(
                FieldNames.OrdersCol.QUANTITY,
                "Quantity", FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.OrdersCol.AMOUNT,
                "Amount", FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.OrdersCol.DELIVERY_TIMESTAMP,
                "Delivery date", FirestoreFieldDataType.TIMESTAMP
            ),
            Triple(
                FieldNames.OrdersCol.CUSTOMER_NAME,
                "Customer name", FirestoreFieldDataType.STRING
            )
        )
    }

    override fun getFilterableFields(): Array<Pair<String, (Model) -> Boolean>> {
        return arrayOf(
            "Delivered" to { order ->
                (order as Order).isDelivered
            },
            "Not delivered" to { order ->
                (order as Order).isDelivered.not()
            }
        )
    }

    override fun getFabClickHandler(): () -> Unit {
        TODO("Not yet implemented")
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemOrderBinding
        model as Order

        binding.apply {
            itemName.text = model.userItemName
            deliveryDate.text = DateTime.getDateStringFromTimestamp(model.deliveryTimestamp)
            amount.text = Converters.numberToMoneyString(model.amount)

            if(model.isDelivered) {
                deliveryStatus.text = "Delivered"
                deliveryStatus.setTextColor(this@OrderHelper.context.getColor(R.color.green))
            } else {
                deliveryStatus.text = "Not delivered"
                deliveryStatus.setTextColor(this@OrderHelper.context.getColor(R.color.red))
            }

            root.setOnClickListener {

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
            newList[newPosition].isDelivered != oldList[oldPosition].isDelivered -> false
            else -> true
        }
    }
}