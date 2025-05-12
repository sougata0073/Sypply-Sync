package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.databinding.ItemCustomerPaymentBinding
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.util.FirestoreNames
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Status

class CustomerPaymentHelper(
    private val fragment: Fragment,
    private val customerRepository: CustomerRepository
) : ModelHelper  {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading = "Payments received"

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemCustomerPaymentBinding {
        return ItemCustomerPaymentBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                CustomerPayment::amount.name,
                "Amount", FirestoreFieldDataType.NUMBER
            ),
            Triple(
                CustomerPayment::paymentTimestamp.name,
                "Payment timestamp", FirestoreFieldDataType.TIMESTAMP
            ),
            Triple(
                CustomerPayment::customerName.name,
                "Customer name", FirestoreFieldDataType.STRING
            )
        )
    }

    override fun getFilterableFields(): Array<Pair<String, (Model) -> Boolean>> {
        return emptyArray()
    }

    override fun getFabClickHandler(): () -> Unit {
        TODO("Not yet implemented")
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemCustomerPaymentBinding
        model as CustomerPayment

        binding.apply {
            val dateString = DateTime.getDateStringFromTimestamp(model.paymentTimestamp)
            val timeString = DateTime.getTimeStringFromTimestamp(model.paymentTimestamp)

            name.text = "From: ${model.customerName}"
            name.text = "To: ${model.customerName}"
            dateTime.text = "At: $dateString On: $timeString"
            amount.text = Converters.numberToMoneyString(model.amount)

            root.setOnClickListener {

            }
        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        return this.customerRepository.getCustomerPaymentsList(
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
        return this.customerRepository.getCustomerPaymentsListFiltered(
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

        newList as List<CustomerPayment>
        oldList as List<CustomerPayment>

        return when {
            newList[newPosition].timestamp != oldList[oldPosition].timestamp -> false
            newList[newPosition].amount != oldList[oldPosition].amount -> false
            newList[newPosition].paymentTimestamp != oldList[oldPosition].paymentTimestamp -> false
            newList[newPosition].note != oldList[oldPosition].note -> false
            newList[newPosition].customerId != oldList[oldPosition].customerId -> false
            newList[newPosition].customerName != oldList[oldPosition].customerName -> false
            else -> true
        }
    }
}