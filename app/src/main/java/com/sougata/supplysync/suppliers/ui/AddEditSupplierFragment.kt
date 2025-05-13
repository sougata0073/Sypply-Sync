package com.sougata.supplysync.suppliers.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentAddEditSupplierBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.suppliers.viewmodels.AddEditSupplierViewModel
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class AddEditSupplierFragment : Fragment() {

    private var _binding: FragmentAddEditSupplierBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditSupplierViewModel

    private lateinit var prevSupplier: Supplier
    private var updatedSupplier: Supplier? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(Keys.TO_ADD)
        this.toEdit = requireArguments().getBoolean(Keys.TO_EDIT)

        if (this.toEdit) {
            this.prevSupplier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(Model.SUPPLIER, Supplier::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable(Model.SUPPLIER)
            }!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_edit_supplier, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[AddEditSupplierViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.viewModel.isSupplierAdded) {
            val bundle = Bundle().apply {
                putBoolean(Keys.DATA_ADDED, viewModel.isSupplierAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }
        if (this.viewModel.isSupplierUpdated) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_UPDATED, updatedSupplier)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }
        if (this.viewModel.isSupplierDeleted) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_REMOVED, prevSupplier)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT, bundle
            )
        }

        this._binding = null
    }

    private fun initializeUI() {

        if (this.toEdit) {
            this.viewModel.apply {
                name.value = prevSupplier.name.toString()
                email.value = prevSupplier.email.toString()
                phone.value = prevSupplier.phone.toString()
                dueAmount.value = prevSupplier.dueAmount.toString()
                paymentDetails.value = prevSupplier.paymentDetails.toString()
                note.value = prevSupplier.note.toString()
            }
        }
        this.setUpSaveButton()
        this.setUpDeleteButton()
    }

    private fun registerListeners() {

        this.viewModel.supplierAddedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.viewModel.isSupplierAdded = true
            }
            this.howToObserve(it, "Supplier added successfully")
        }
        this.viewModel.supplierEditedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.viewModel.isSupplierUpdated = true
            }
            this.howToObserve(it, "Supplier updated successfully")
        }
        this.viewModel.supplierDeletedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.viewModel.isSupplierDeleted = true
            }
            this.howToObserve(it, "Supplier deleted successfully")
        }

    }

    private fun setUpSaveButton() {
        this.binding.saveBtn.setOnClickListener {
            if (this.toAdd) {
                this.viewModel.addSupplier(this.binding.root)
            } else if (this.toEdit) {
                this.updatedSupplier = this.viewModel.updateSupplier(
                    this.prevSupplier.id, this.prevSupplier.timestamp,
                    this.binding.root
                )
            }
        }
    }

    private fun setUpDeleteButton() {
        if (this.toAdd) {
            binding.deleteBtn.visibility = View.INVISIBLE
        } else if (this.toEdit) {
            this.binding.deleteBtn.apply {
                visibility = View.VISIBLE
                setOnClickListener {

                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.materialAlertDialogStyle
                    ).setTitle("Warning")
                        .setMessage("Are you sure you want to delete this supplier?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            viewModel.deleteSupplier(prevSupplier)
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    private fun howToObserve(observedData: Pair<Status, String>, successMessage: String) {
        if (observedData.first == Status.STARTED) {

            this.binding.apply {

                saveBtn.isClickable = false
                progressBar.visibility = View.VISIBLE
                parentLayout.alpha = 0.5F
            }

        } else if (observedData.first == Status.SUCCESS) {

            Snackbar.make(
                requireParentFragment().requireView(),
                successMessage,
                Snackbar.LENGTH_SHORT
            ).show()

            this.findNavController().popBackStack()

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