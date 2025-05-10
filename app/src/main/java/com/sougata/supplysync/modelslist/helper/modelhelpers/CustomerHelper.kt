package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemCustomerBinding
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Status
import kotlin.reflect.KProperty1

class CustomerHelper(
    private val fragment: Fragment,
    private val customerRepository: CustomerRepository
) : ModelHelper {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading = "All customers"

    @Suppress("UNCHECKED_CAST")
    override fun getProperties(): Array<KProperty1<Model, *>> {
        return arrayOf(
            Customer::name, Customer::receivableAmount, Customer::dueOrders,
            Customer::phone, Customer::email, Customer::note, Customer::profileImageUrl
        ) as Array<KProperty1<Model, *>>
    }

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {
        return ItemCustomerBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                FieldNames.CustomersCol.NAME,
                "Name",
                FirestoreFieldDataType.STRING
            ),
            Triple(
                FieldNames.CustomersCol.RECEIVABLE_AMOUNT,
                "Receivable amount",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.CustomersCol.DUE_ORDERS,
                "Due orders",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.CustomersCol.EMAIL,
                "Email",
                FirestoreFieldDataType.STRING
            ),
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

}