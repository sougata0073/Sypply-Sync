package com.sougata.supplysync.modelslist.helper

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sougata.supplysync.R
import com.sougata.supplysync.cloud.FieldNames
import com.sougata.supplysync.databinding.ItemSupplierPaymentsListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.modelslist.DataType
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.KeysAndMessages
import java.util.Locale
import kotlin.reflect.KProperty1

class SupplierPaymentHelper(private val fragment: Fragment) :
    HelperStructure {

    private val context = this.fragment.requireContext()

    @Suppress("UNCHECKED_CAST")
    override fun getProperties(): Array<KProperty1<Model, *>> {
        return arrayOf(
            SupplierPayment::amount, SupplierPayment::paymentTimestamp,
            SupplierPayment::note, SupplierPayment::supplierId, SupplierPayment::timestamp
        ) as Array<KProperty1<Model, *>>
    }

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ViewDataBinding {
        return ItemSupplierPaymentsListBinding.inflate(
            inflater,
            parent,
            false
        )
    }

    override fun getFieldsPair(): Array<Triple<String, String, DataType>> {
        return arrayOf(
            Triple(
                FieldNames.SupplierPaymentsCol.AMOUNT,
                SupplierPayment::amount.name,
                DataType.NUMBER
            ),
            Triple(
                FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP,
                SupplierPayment::paymentTimestamp.name,
                DataType.TIMESTAMP
            ),
            Triple(
                FieldNames.SupplierPaymentsCol.SUPPLIER_NAME,
                SupplierPayment::supplierName.name,
                DataType.STRING
            )
        )
    }

    override fun getFabClickHandler(): () -> Unit {
        return {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_ADD_KEY, true)
            }
            this.fragment.findNavController().navigate(
                R.id.addEditSupplierPaymentFragment, bundle, Inputs.getFragmentAnimations()
            )
        }
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
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

            val dateString = String.Companion.format(
                Locale.getDefault(),
                "On: %02d-%02d-%04d",
                myDate, month, year
            )
            val timeString = String.Companion.format(
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
                    this@SupplierPaymentHelper.context,
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
                        this@SupplierPaymentHelper.fragment.findNavController()
                            .navigate(
                                R.id.addEditSupplierPaymentFragment,
                                bundle,
                                Inputs.getFragmentAnimations()
                            )
                    }.show()
            }
        }
    }
}