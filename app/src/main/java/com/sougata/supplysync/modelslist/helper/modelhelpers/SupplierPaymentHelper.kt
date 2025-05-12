package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemSupplierPaymentBinding
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.firestore.util.FirestoreNames
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class SupplierPaymentHelper(
    private val fragment: Fragment,
    private val supplierRepository: SupplierRepository
) :
    ModelHelper  {

    private val context = this.fragment.requireContext()

    override val listHeading: String = "Payments sent"

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemSupplierPaymentBinding {
        return ItemSupplierPaymentBinding.inflate(
            inflater,
            parent,
            false
        )
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                SupplierPayment::amount.name,
                "Amount",
                FirestoreFieldDataType.NUMBER
            ),
            Triple(
                SupplierPayment::paymentTimestamp.name,
                "Payment date",
                FirestoreFieldDataType.TIMESTAMP
            ),
            Triple(
                SupplierPayment::supplierName.name,
                "Supplier name",
                FirestoreFieldDataType.STRING
            )
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
                R.id.addEditSupplierPaymentFragment,
                bundle,
                AnimationProvider.slideRightLeftNavOptions()
            )
        }
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemSupplierPaymentBinding
        model as SupplierPayment

        binding.apply {

            val dateString = DateTime.getDateStringFromTimestamp(model.paymentTimestamp)
            val timeString = DateTime.getTimeStringFromTimestamp(model.paymentTimestamp)

            name.text = "To: ${model.supplierName}"
            dateTime.text = "At: $dateString On: $timeString"
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
                            putParcelable(Model.SUPPLIER_PAYMENT, model)
                        }
                        this@SupplierPaymentHelper.fragment.findNavController()
                            .navigate(
                                R.id.addEditSupplierPaymentFragment,
                                bundle,
                                AnimationProvider.slideRightLeftNavOptions()
                            )
                    }.show()
            }
        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.supplierRepository.getSupplierPaymentsList(
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
        this.supplierRepository.getSupplierPaymentsListFiltered(
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
        newList as List<SupplierPayment>
        oldList as List<SupplierPayment>

        return when {
            newList[newPosition].timestamp != oldList[oldPosition].timestamp -> false
            newList[newPosition].amount != oldList[oldPosition].amount -> false
            newList[newPosition].paymentTimestamp != oldList[oldPosition].paymentTimestamp -> false
            newList[newPosition].note != oldList[oldPosition].note -> false
            newList[newPosition].supplierId != oldList[oldPosition].supplierId -> false
            newList[newPosition].supplierName != oldList[oldPosition].supplierName -> false
            else -> true
        }
    }
}