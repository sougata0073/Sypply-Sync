package com.sougata.supplysync.util.modelslist

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sougata.supplysync.R
import com.sougata.supplysync.cloud.FieldNamesRepository
import com.sougata.supplysync.databinding.ItemOrderedItemsListBinding
import com.sougata.supplysync.databinding.ItemSupplierItemsListBinding
import com.sougata.supplysync.databinding.ItemSupplierPaymentsListBinding
import com.sougata.supplysync.databinding.ItemSuppliersListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
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

    fun getWhatToOnBind(): (ViewDataBinding, Model) -> Unit {

        // The when block will return something whose
        // datatype is equal the function (e.g. this::bindSupplier)
        return when (this.modelName) {
            Model.SUPPLIER -> this::bindSupplier
            Model.SUPPLIERS_ITEM -> this::bindSupplierItem
            Model.SUPPLIER_PAYMENT -> this::bindSupplierPayment
            Model.ORDERED_ITEM -> this::bindOrderedItem
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

            root.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@ModelsListFragmentHelper.context,
                    R.style.materialAlertDialogStyle
                )
                    .setTitle(model.name)
                    .setMessage(model.details)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .setNeutralButton("Edit") { dialog, _ ->

                        AddEditSupplierItemBottomSheetFragment.getInstance(
                            model,
                            KeysAndMessages.TO_EDIT_KEY
                        )
                            .show(this@ModelsListFragmentHelper.fragmentManager, "supplierItemAdd")

                    }.show()
            }
        }
    }

    private fun bindSupplierPayment(binding: ViewDataBinding, model: Model) {
        binding as ItemSupplierPaymentsListBinding
        model as SupplierPayment

        binding.apply {
            var year = 0
            var month = 0
            var myDate = 0

            Converters.getDateFromTimestamp(model.paymentTimestamp).apply {
                year = first
                month = second
                myDate = third
            }

            var hour = 0
            var minute = 0

            Converters.getTimeFromTimestamp(model.paymentTimestamp).apply {
                hour = first
                minute = second
            }

            val dateString = String.format(
                Locale.getDefault(),
                "On: %02d-%02d-%04d",
                myDate, month, year
            )
            val timeString = String.format(
                Locale.getDefault(),
                "At: %02d:%02d",
                hour, minute
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

    private fun bindOrderedItem(binding: ViewDataBinding, model: Model) {
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

            val dateString = String.format(
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
                    this@ModelsListFragmentHelper.context,
                    R.style.materialAlertDialogStyle
                ).setTitle(model.itemName)
                    .setMessage(message)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .setNeutralButton("Edit") { dialog, _ ->
                        val bundle = Bundle().apply {
                            putBoolean(KeysAndMessages.TO_EDIT_KEY, true)
                            putParcelable("orderedItem", model)
                        }
                        this@ModelsListFragmentHelper.fragment.findNavController()
                            .navigate(
                                R.id.addEditOrderedItemFragment,
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
                    AddEditSupplierItemBottomSheetFragment.getInstance(
                        null,
                        KeysAndMessages.TO_ADD_KEY
                    )
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

            Model.ORDERED_ITEM -> {
                {
                    val bundle = Bundle().apply {
                        putBoolean(KeysAndMessages.TO_ADD_KEY, true)
                    }
                    this.fragment.findNavController().navigate(
                        R.id.addEditOrderedItemFragment, bundle, Inputs.getFragmentAnimations()
                    )
                }
            }

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getSortableModelFieldNames(): Array<String> {

        return when (this.modelName) {
            Model.SUPPLIER -> arrayOf(Supplier::name.name, Supplier::dueAmount.name)
            Model.SUPPLIERS_ITEM -> arrayOf(SupplierItem::name.name, SupplierItem::price.name)
            Model.SUPPLIER_PAYMENT -> arrayOf(
                SupplierPayment::amount.name, SupplierPayment::paymentTimestamp.name,
                SupplierPayment::supplierName.name
            )

            Model.ORDERED_ITEM -> arrayOf(
                OrderedItem::itemName.name,
                OrderedItem::quantity.name,
                OrderedItem::amount.name,
                OrderedItem::supplierName.name,
                OrderedItem::orderTimestamp.name
            )

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

    fun getSearchableModelFieldPair(): Array<Triple<String, String, DataType>> {
        return when (this.modelName) {
            Model.SUPPLIER -> arrayOf(
                Triple(
                    FieldNamesRepository.SuppliersCollection.NAME,
                    Supplier::name.name,
                    DataType.STRING
                ),
                Triple(
                    FieldNamesRepository.SuppliersCollection.DUE_AMOUNT,
                    Supplier::dueAmount.name,
                    DataType.NUMBER
                ),
                Triple(
                    FieldNamesRepository.SuppliersCollection.EMAIL,
                    Supplier::email.name,
                    DataType.STRING
                ),
            )

            Model.SUPPLIERS_ITEM -> arrayOf(
                Triple(
                    FieldNamesRepository.SupplierItemsCollection.NAME,
                    SupplierItem::name.name,
                    DataType.STRING
                ),
                Triple(
                    FieldNamesRepository.SupplierItemsCollection.PRICE,
                    SupplierItem::price.name,
                    DataType.NUMBER
                )
            )

            Model.SUPPLIER_PAYMENT -> arrayOf(
                Triple(
                    FieldNamesRepository.SupplierPaymentsCollection.AMOUNT,
                    SupplierPayment::amount.name,
                    DataType.NUMBER
                ),
                Triple(
                    FieldNamesRepository.SupplierPaymentsCollection.PAYMENT_TIMESTAMP,
                    SupplierPayment::paymentTimestamp.name,
                    DataType.TIMESTAMP
                ),
                Triple(
                    FieldNamesRepository.SupplierPaymentsCollection.SUPPLIER_NAME,
                    SupplierPayment::supplierName.name,
                    DataType.STRING
                )
            )

            Model.ORDERED_ITEM -> arrayOf(
                Triple(
                    FieldNamesRepository.OrderedItemsCollection.ITEM_NAME,
                    OrderedItem::itemName.name,
                    DataType.STRING
                ),
                Triple(
                    FieldNamesRepository.OrderedItemsCollection.QUANTITY,
                    OrderedItem::quantity.name,
                    DataType.NUMBER
                ),
                Triple(
                    FieldNamesRepository.OrderedItemsCollection.AMOUNT,
                    OrderedItem::amount.name,
                    DataType.NUMBER
                ),
                Triple(
                    FieldNamesRepository.OrderedItemsCollection.SUPPLIER_NAME,
                    OrderedItem::supplierName.name,
                    DataType.STRING
                ),
                Triple(
                    FieldNamesRepository.OrderedItemsCollection.ORDER_TIMESTAMP,
                    OrderedItem::orderTimestamp.name,
                    DataType.TIMESTAMP
                )
            )

            else -> throw IllegalArgumentException("Unknown model type")
        }
    }

}