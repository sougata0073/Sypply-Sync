package com.sougata.supplysync.suppliers.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.BottomSheetAddEditSupplierItemBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.suppliers.viewmodels.AddEditSupplierItemViewModel
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class AddEditSupplierItemBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddEditSupplierItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditSupplierItemViewModel

    private lateinit var prevSupplierItem: SupplierItem
    private var updatedSupplierItem: SupplierItem? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isSupplierItemAdded = false
    private var isSupplierItemUpdated = false
    private var isSupplierItemDeleted = false

    companion object {
        @JvmStatic
        fun getInstance(supplierItem: SupplierItem?, action: String) =
            AddEditSupplierItemBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Model.SUPPLIER_ITEM, supplierItem)
                    if (action == Keys.TO_ADD) {
                        putBoolean(Keys.TO_ADD, true)
                    } else if (action == Keys.TO_EDIT) {
                        putBoolean(Keys.TO_EDIT, true)
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(Keys.TO_ADD)
        this.toEdit = requireArguments().getBoolean(Keys.TO_EDIT)

        if (this.toEdit) {
            this.prevSupplierItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(Model.SUPPLIER_ITEM, SupplierItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable(Model.SUPPLIER_ITEM)
            }!!

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_add_edit_supplier_item,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[AddEditSupplierItemViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.isSupplierItemAdded) {
            val bundle = Bundle().apply {
                putBoolean(Keys.DATA_ADDED, isSupplierItemAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }
        if (this.isSupplierItemUpdated) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_UPDATED, updatedSupplierItem)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }
        if (this.isSupplierItemDeleted) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_REMOVED, prevSupplierItem)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT, bundle
            )
        }

        this._binding = null
    }

    private fun initializeUI() {
        this.binding.saveBtn.setOnClickListener {
            if (this.toAdd) {
                this.viewModel.addSupplierItem(this.binding.root)
            } else if (this.toEdit) {
                this.updatedSupplierItem = this.viewModel.updateSupplierItem(
                    this.prevSupplierItem.id, this.prevSupplierItem.timestamp,
                    this.binding.root
                )

            }
        }

        this.binding.deleteBtn.setOnClickListener {

            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.materialAlertDialogStyle
            ).setTitle("Warning")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, _ ->
                    this.viewModel.deleteSupplierItem(this.prevSupplierItem)
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()

        }

        if (this.toAdd) {
            binding.deleteBtn.visibility = View.GONE
        }

        if (this.toEdit) {
            this.binding.deleteBtn.visibility = View.VISIBLE
            this.viewModel.apply {
                name.value = prevSupplierItem.name.toString()
                price.value = prevSupplierItem.price.toString()
                details.value = prevSupplierItem.details.toString()
            }
        }
    }

    private fun registerListeners() {

        this.viewModel.supplierItemAddedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isSupplierItemAdded = true
            }
            this.howToObserve(it, "Item added successfully")
        }
        this.viewModel.supplierItemEditedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isSupplierItemUpdated = true
            }
            this.howToObserve(it, "Item updated successfully")
        }
        this.viewModel.supplierItemDeletedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isSupplierItemDeleted = true
            }
            this.howToObserve(it, "Item deleted successfully")
        }
    }

    private fun howToObserve(observedData: Pair<Status, String>, message: String) {
        if (observedData.first == Status.STARTED) {

            this.binding.apply {

                saveBtn.isClickable = false
                progressBar.visibility = View.VISIBLE
                parentLayout.alpha = 0.5F
            }

        } else if (observedData.first == Status.SUCCESS) {

            Snackbar.make(
                requireParentFragment().requireView(),
                message,
                Snackbar.LENGTH_SHORT
            ).show()

            this.dismiss()

        } else if (observedData.first == Status.FAILED) {

            this.binding.apply {

                saveBtn.isClickable = true
                progressBar.visibility = View.GONE
                parentLayout.alpha = 1F

            }

            Snackbar.make(
                requireParentFragment().requireView(), observedData.second, Snackbar.LENGTH_SHORT
            ).show()

        }
    }
}