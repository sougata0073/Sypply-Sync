package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.R
import com.sougata.supplysync.customers.ui.CustomerProfileBottomSheetFragment
import com.sougata.supplysync.databinding.ItemCustomerBinding
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.util.FirestoreNames
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class CustomerHelper(
    private val fragment: Fragment,
    private val customerRepository: CustomerRepository
) : ModelHelper {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading = "All customers"

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {
        return ItemCustomerBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                Customer::name.name,
                "Name",
                FirestoreFieldDataType.STRING
            ),
            Triple(
                Customer::receivableAmount.name,
                "Receivable amount",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                Customer::dueOrders.name,
                "Due orders",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                Customer::email.name,
                "Email",
                FirestoreFieldDataType.STRING
            ),
        )
    }

    override fun getFilterableFields(): Array<Pair<String, (Model) -> Boolean>> {
        return emptyArray()
    }

    override fun getFabClickHandler(): () -> Unit {
        return {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_ADD_KEY, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditCustomerFragment, bundle, AnimationProvider.slideRightLeftNavOptions()
            )
        }
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemCustomerBinding
        model as Customer

        Glide.with(this.context)
            .load(model.profileImageUrl)
            .placeholder(R.drawable.ic_user_profile)
            .error(R.drawable.ic_user_profile)
            .into(binding.profileImage)

        binding.apply {

            name.text = model.name
            email.text = model.email

            callBtn.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL, "tel:${model.phone}".toUri())
                this@CustomerHelper.context.startActivity(callIntent)
            }

            root.setOnClickListener {
                CustomerProfileBottomSheetFragment.Companion.getInstance(model)
                    .show(this@CustomerHelper.fragmentManager, "customerProfile")
            }

        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.customerRepository.getCustomersList(
            lastDocumentSnapshot, limit, onComplete
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
        this.customerRepository.getCustomersListFiltered(
            searchField, searchQuery, queryDataType, lastDocumentSnapshot, limit, onComplete
        )
    }

    override fun loadFullListOnNewModelAdded(): Boolean {
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun getContentComparator(
        newList: List<Model>,
        oldList: List<Model>,
        newPosition: Int,
        oldPosition: Int
    ): Boolean {

        newList as List<Customer>
        oldList as List<Customer>

        return when {
            newList[newPosition].timestamp != oldList[oldPosition].timestamp -> false
            newList[newPosition].name != oldList[oldPosition].name -> false
            newList[newPosition].receivableAmount != oldList[oldPosition].receivableAmount -> false
            newList[newPosition].dueOrders != oldList[oldPosition].dueOrders -> false
            newList[newPosition].phone != oldList[oldPosition].phone -> false
            newList[newPosition].email != oldList[oldPosition].email -> false
            newList[newPosition].note != oldList[oldPosition].note -> false
            newList[newPosition].profileImageUrl != oldList[oldPosition].profileImageUrl -> false
            else -> true
        }
    }

}