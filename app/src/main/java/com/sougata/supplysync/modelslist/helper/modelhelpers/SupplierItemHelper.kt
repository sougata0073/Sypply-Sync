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
import com.sougata.supplysync.databinding.ItemSupplierItemBinding
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.firestore.util.FirestoreNames
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.suppliers.ui.AddEditSupplierItemBottomSheetFragment
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class SupplierItemHelper(
    private val fragment: Fragment,
    private val supplierRepository: SupplierRepository
) :
    ModelHelper {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading: String = "Suppliers items"

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemSupplierItemBinding {
        return ItemSupplierItemBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                SupplierItem::name.name,
                "Name",
                FirestoreFieldDataType.STRING
            ),
            Triple(
                SupplierItem::price.name,
                "Price",
                FirestoreFieldDataType.NUMBER
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
        binding as ItemSupplierItemBinding
        model as SupplierItem

        binding.apply {
            name.text = model.name
            details.text = model.details
            price.text = Converters.numberToMoneyString(model.price)

            root.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@SupplierItemHelper.context,
                    R.style.materialAlertDialogStyle
                )
                    .setTitle(model.name)
                    .setMessage(model.details)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .setNeutralButton("Edit") { dialog, _ ->

                        AddEditSupplierItemBottomSheetFragment.Companion.getInstance(
                            model,
                            KeysAndMessages.TO_EDIT_KEY
                        )
                            .show(this@SupplierItemHelper.fragmentManager, "supplierItemAdd")

                    }.show()
            }
        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.supplierRepository.getSupplierItemsList(
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
        this.supplierRepository.getSupplierItemsListFiltered(
            searchField,
            searchQuery,
            queryDataType,
            lastDocumentSnapshot,
            limit,
            onComplete
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

        newList as List<SupplierItem>
        oldList as List<SupplierItem>

        return when {
            newList[newPosition].timestamp != oldList[oldPosition].timestamp -> false
            newList[newPosition].name != oldList[oldPosition].name -> false
            newList[newPosition].price != oldList[oldPosition].price -> false
            newList[newPosition].details != oldList[oldPosition].details -> false
            else -> true
        }
    }
}