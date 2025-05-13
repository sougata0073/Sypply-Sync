package com.sougata.supplysync.customers.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.sougata.supplysync.customers.viewmodels.AddEditCustomerViewModel
import com.sougata.supplysync.databinding.FragmentAddEditCustomerBinding
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class AddEditCustomerFragment : Fragment() {

    private var _binding: FragmentAddEditCustomerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditCustomerViewModel

    private lateinit var prevCustomer: Customer
    private var updatedCustomer: Customer? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(Keys.TO_ADD)
        this.toEdit = requireArguments().getBoolean(Keys.TO_EDIT)

        if (this.toEdit) {
            this.prevCustomer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(Model.CUSTOMER, Customer::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable(Model.CUSTOMER)
            }!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_edit_customer, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[AddEditCustomerViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.viewModel.isCustomerAdded) {
            val bundle = Bundle().apply {
                putBoolean(Keys.DATA_ADDED, viewModel.isCustomerAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
            Log.d("TAG", "Added")
        }
        if (this.viewModel.isCustomerUpdated) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_UPDATED, updatedCustomer)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }
        if (this.viewModel.isCustomerDeleted) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_REMOVED, prevCustomer)
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
                name.value = prevCustomer.name.toString()
                email.value = prevCustomer.email.toString()
                phone.value = prevCustomer.phone.toString()
                receivableAmount.value = prevCustomer.receivableAmount.toString()
                dueOrders.value = prevCustomer.dueOrders.toString()
                note.value = prevCustomer.note.toString()
            }
        }

        this.setUpSaveButton()
        this.setUpDeleteButton()
    }

    private fun registerListeners() {
        this.viewModel.customerAddedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.viewModel.isCustomerAdded = true
            }
            this.observe(it, "Customer added successfully")
        }
        this.viewModel.customerEditedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.viewModel.isCustomerUpdated = true
            }
            this.observe(it, "Customer updated successfully")
        }
        this.viewModel.customerDeletedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.viewModel.isCustomerDeleted = true
            }
            this.observe(it, "Customer deleted successfully")
        }
    }

    private fun setUpSaveButton() {
        this.binding.saveBtn.setOnClickListener {
            if (this.toAdd) {
                this.viewModel.addCustomer(this.binding.root)
            } else if (this.toEdit) {
                val customerId = this.prevCustomer.id
                val timeStamp = this.prevCustomer.timestamp

                this.updatedCustomer = this.viewModel.updateCustomer(
                    customerId, timeStamp, this.binding.root
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
                        .setMessage("Are you sure you want to delete this customer?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            viewModel.deleteCustomer(prevCustomer)
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    private fun observe(observedData: Pair<Status, String>, successMessage: String) {
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