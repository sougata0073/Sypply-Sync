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
import com.sougata.supplysync.R
import com.sougata.supplysync.remote.FirestoreFieldNames
import com.sougata.supplysync.databinding.ItemSuppliersListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.util.DataType
import com.sougata.supplysync.modelslist.helper.HelperStructure
import com.sougata.supplysync.suppliers.ui.SupplierProfileBottomSheetFragment
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.KeysAndMessages
import kotlin.reflect.KProperty1

class SupplierHelper(private val fragment: Fragment) :
    HelperStructure {

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

    override fun getFieldsPair(): Array<Triple<String, String, DataType>> {
        return arrayOf(
            Triple(
                FirestoreFieldNames.SuppliersCol.NAME,
                Supplier::name.name,
                DataType.STRING
            ),
            Triple(
                FirestoreFieldNames.SuppliersCol.DUE_AMOUNT,
                Supplier::dueAmount.name,
                DataType.NUMBER
            ),
            Triple(
                FirestoreFieldNames.SuppliersCol.EMAIL,
                Supplier::email.name,
                DataType.STRING
            ),
        )
    }

    override fun getFabClickHandler(): () -> Unit {
        return {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_ADD_KEY, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditSupplierFragment, bundle, AnimationProvider.fragmentAnimationSlideRightLeft()
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
                this@SupplierHelper.context.startActivity(callIntent)
            }

            root.setOnClickListener {
                SupplierProfileBottomSheetFragment.Companion.getInstance(model)
                    .show(this@SupplierHelper.fragmentManager, "supplierProfile")
            }
        }
    }
}