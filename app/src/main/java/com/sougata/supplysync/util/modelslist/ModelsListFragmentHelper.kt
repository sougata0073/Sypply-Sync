package com.sougata.supplysync.util.modelslist

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemSupplierItemsListBinding
import com.sougata.supplysync.databinding.ItemSupplierPaymentsListBinding
import com.sougata.supplysync.databinding.ItemSuppliersListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.suppliers.bottomsheets.AddEditSupplierItemBottomSheetFragment
import com.sougata.supplysync.suppliers.bottomsheets.SupplierProfileBottomSheetFragment
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.KeysAndMessages
import java.util.Locale

class ModelsListFragmentHelper(
    private val modelName: String,
    private val fragment: Fragment
) {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    fun getWhichViewModelToCreate(): ModelsListViewModel {

        val viewModelFactory = ModelsListViewModelFactory(this.modelName)

        return ViewModelProvider(
            this.fragment,
            viewModelFactory
        )[ModelsListViewModel::class.java]
    }

    fun getWhatToOnBind(): (ViewDataBinding, Model) -> Unit {

        // The when block will return something whose
        // datatype is equal the function (e.g. this::bindSupplier)
        return when (this.modelName) {
            Model.SUPPLIER -> this::bindSupplier
            Model.SUPPLIERS_ITEM -> this::bindSupplierItem
            Model.SUPPLIER_PAYMENT -> this::bindSupplierPayment
            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    private fun bindSupplier(binding: ViewDataBinding, model: Model) {
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
                this@ModelsListFragmentHelper.context.startActivity(callIntent)
            }

            root.setOnClickListener {
                SupplierProfileBottomSheetFragment.getInstance(model)
                    .show(this@ModelsListFragmentHelper.fragmentManager, "supplierProfile")
            }
        }
    }

    private fun bindSupplierItem(binding: ViewDataBinding, model: Model) {
        binding as ItemSupplierItemsListBinding
        model as SupplierItem

        binding.apply {
            name.text = model.name
            details.text = model.details
            price.text = Converters.numberToMoneyString(model.price)
        }
    }

    private fun bindSupplierPayment(binding: ViewDataBinding, model: Model) {
        binding as ItemSupplierPaymentsListBinding
        model as SupplierPayment

        binding.apply {
            val dateString = String.format(
                Locale.getDefault(),
                "On: %02d-%02d-%04d",
                model.date, model.month, model.year
            )
            val timeString = String.format(
                Locale.getDefault(),
                "At: %02d:%02d",
                model.hour, model.minute
            )

            name.text = "To: ${model.supplierName}"
            date.text = dateString
            time.text = timeString
            amount.text = Converters.numberToMoneyString(model.amount)

            root.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@ModelsListFragmentHelper.context,
                    R.style.materialAlertDialogStyle
                )
                    .setTitle("To: ${model.supplierName}")
                    .setMessage(model.note)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .setNeutralButton("Edit") { dialog, _ ->
                        val bundle = Bundle().apply {
                            putBoolean(KeysAndMessages.TO_EDIT_KEY, true)
                            putParcelable("supplierPayment", model)
                        }
                        this@ModelsListFragmentHelper.fragment.findNavController()
                            .navigate(
                                R.id.addEditSupplierPaymentFragment,
                                bundle,
                                Inputs.getFragmentAnimations()
                            )
                    }.show()
            }
        }
    }

    fun getWhatToDoOnFabClick(): () -> Unit {
        // Double brackets to make scope functions
        return when (this.modelName) {
            Model.SUPPLIER -> {
                {
                    val bundle = Bundle().apply {
                        putBoolean(KeysAndMessages.TO_ADD_KEY, true)
                    }
                    this.fragment.findNavController().navigate(
                        R.id.addEditSupplierFragment, bundle, Inputs.getFragmentAnimations()
                    )
                }
            }

            Model.SUPPLIERS_ITEM -> {
                {
                    AddEditSupplierItemBottomSheetFragment.getInstance(null, true)
                        .show(this.fragmentManager, "supplierItemAdd")
                }
            }

            Model.SUPPLIER_PAYMENT -> {
                {
                    val bundle = Bundle().apply {
                        putBoolean(KeysAndMessages.TO_ADD_KEY, true)
                    }
                    this.fragment.findNavController().navigate(
                        R.id.addEditSupplierPaymentFragment, bundle, Inputs.getFragmentAnimations()
                    )
                }
            }

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

}