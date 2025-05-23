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
import com.sougata.supplysync.databinding.ItemSupplierBinding
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.suppliers.ui.SupplierProfileBottomSheetFragment
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class SupplierHelper(
    private val fragment: Fragment,
    private val supplierRepository: SupplierRepository
) :
    ModelHelper {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading: String = "All suppliers"

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemSupplierBinding {
        return ItemSupplierBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                Supplier::name.name,
                "Name",
                FirestoreFieldDataType.STRING
            ),
            Triple(
                Supplier::dueAmount.name,
                "Due amount",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                Supplier::email.name,
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
                putBoolean(Keys.TO_ADD, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditSupplierFragment, bundle, AnimationProvider.slideRightLeftNavOptions()
            )
        }
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemSupplierBinding
        model as Supplier

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
                this@SupplierHelper.context.startActivity(callIntent)
            }

            root.setOnClickListener {
                SupplierProfileBottomSheetFragment.Companion.getInstance(model)
                    .show(this@SupplierHelper.fragmentManager, "supplierProfile")
            }
        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.supplierRepository.getSuppliersList(
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
        this.supplierRepository.getSuppliersListFiltered(
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
        newList as List<Supplier>
        oldList as List<Supplier>

        return when {
            newList[newPosition].timestamp != oldList[oldPosition].timestamp -> false
            newList[newPosition].name != oldList[oldPosition].name -> false
            newList[newPosition].dueAmount != oldList[oldPosition].dueAmount -> false
            newList[newPosition].phone != oldList[oldPosition].phone -> false
            newList[newPosition].email != oldList[oldPosition].email -> false
            newList[newPosition].note != oldList[oldPosition].note -> false
            newList[newPosition].paymentDetails != oldList[oldPosition].paymentDetails -> false
            newList[newPosition].profileImageUrl != oldList[oldPosition].profileImageUrl -> false
            else -> true
        }
    }
}