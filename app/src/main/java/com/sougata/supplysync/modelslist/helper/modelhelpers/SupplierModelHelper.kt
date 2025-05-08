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
import com.sougata.supplysync.databinding.ItemSuppliersListBinding
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.suppliers.ui.SupplierProfileBottomSheetFragment
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import kotlin.reflect.KProperty1

class SupplierModelHelper(
    private val fragment: Fragment,
    private val supplierRepository: SupplierRepository
) :
    ModelHelper {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading: String = "All suppliers"

    @Suppress("UNCHECKED_CAST")
    override fun getProperties(): Array<KProperty1<Model, *>> {
        return arrayOf(
            Supplier::name, Supplier::dueAmount, Supplier::phone,
            Supplier::email, Supplier::note, Supplier::paymentDetails,
            Supplier::profileImageUrl, Supplier::timestamp
        ) as Array<KProperty1<Model, *>>
    }

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {
        return ItemSuppliersListBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                FieldNames.SuppliersCol.NAME,
                "Name",
                FirestoreFieldDataType.STRING
            ),
            Triple(
                FieldNames.SuppliersCol.DUE_AMOUNT,
                "Due amount",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                FieldNames.SuppliersCol.EMAIL,
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
                R.id.addEditSupplierFragment, bundle, AnimationProvider.slideRightLeftNavOptions()
            )
        }
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemSuppliersListBinding
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
                this@SupplierModelHelper.context.startActivity(callIntent)
            }

            root.setOnClickListener {
                SupplierProfileBottomSheetFragment.Companion.getInstance(model)
                    .show(this@SupplierModelHelper.fragmentManager, "supplierProfile")
            }
        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
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
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.supplierRepository.getSuppliersListFiltered(
            searchField, searchQuery, queryDataType, lastDocumentSnapshot, limit, onComplete
        )
    }

    override fun loadFullListOnNewModelAdded(): Boolean {
        return false
    }
}