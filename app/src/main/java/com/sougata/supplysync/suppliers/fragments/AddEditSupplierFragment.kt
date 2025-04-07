package com.sougata.supplysync.suppliers.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.cloud.SupplierFirestoreRepository
import com.sougata.supplysync.databinding.FragmentAddEditSupplierBinding
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.suppliers.viewmodels.AddEditSupplierViewModel
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class AddEditSupplierFragment : Fragment() {

    private lateinit var binding: FragmentAddEditSupplierBinding

    private lateinit var viewModel: AddEditSupplierViewModel

    private lateinit var prevSupplier: Supplier
    private var updatedSupplier: Supplier? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isSupplierAdded = false
    private var isSupplierUpdated = false
    private var isSupplierDeleted = false

    private val supplierFirestoreRepository = SupplierFirestoreRepository()

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Log.d("img", it.toString())
        Log.d("tagy", "Hello")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(KeysAndMessages.TO_ADD_KEY)
        this.toEdit = requireArguments().getBoolean(KeysAndMessages.TO_EDIT_KEY)

        if (this.toEdit) {
            this.prevSupplier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable("supplier", Supplier::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable("supplier")
            }!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding =
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

        if (this.isSupplierAdded) {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.DATA_ADDED_KEY, isSupplierAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY,
                bundle
            )
        }
        if (this.isSupplierUpdated) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_UPDATED_KEY, updatedSupplier)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY,
                bundle
            )
        }
        if (this.isSupplierDeleted) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_REMOVED_KEY, prevSupplier)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY, bundle
            )
        }
    }

    private fun initializeUI() {

        this.binding.profileImage.setOnClickListener {
            this.imageLauncher.launch("image/*")
        }

        this.binding.saveBtn.setOnClickListener {

            if (this.toAdd) {

                this.viewModel.addSupplier(this.binding.root)

            } else if (this.toEdit) {

                val supplierId = this.prevSupplier.id

                this.updatedSupplier = this.viewModel.updateSupplier(
                    supplierId,
                    this.binding.root
                )

            }

        }

        this.binding.deleteBtn.setOnClickListener {

            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.materialAlertDialogStyle
            ).setTitle("Warning")
                .setMessage("Are you sure you want to delete this supplier?")
                .setPositiveButton("Yes") { dialog, _ ->

                    this.binding.parentLayout.alpha = 0.5f
                    this.binding.progressBar.visibility = View.VISIBLE

                    this.supplierFirestoreRepository.deleteSupplier(this.prevSupplier) { status, message ->

                        Snackbar.make(
                            requireParentFragment().requireView(),
                            message,
                            Snackbar.LENGTH_SHORT
                        ).show()

                        if (status == Status.SUCCESS) {

                            this.isSupplierDeleted = true

                            findNavController().popBackStack()

                        } else if (status == Status.FAILED) {
                            this.binding.parentLayout.alpha = 1f
                            this.binding.progressBar.visibility = View.GONE
                        }

                    }
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()

        }

        if (this.toAdd) {
            binding.deleteBtn.visibility = View.INVISIBLE
        }

        if (this.toEdit) {
            this.binding.deleteBtn.visibility = View.VISIBLE
            this.viewModel.apply {
                name.value = prevSupplier.name.toString()
                email.value = prevSupplier.email.toString()
                phone.value = prevSupplier.phone.toString()
                dueAmount.value = prevSupplier.dueAmount.toString()
                paymentDetails.value = prevSupplier.paymentDetails.toString()
                note.value = prevSupplier.note.toString()
                profileImageUrl.value = prevSupplier.profileImageUrl.toString()
            }

        }
    }

    private fun registerListeners() {

        this.viewModel.supplierAddedIndicator.observe(this.viewLifecycleOwner) {
            this.howToObserve(it)
        }
        this.viewModel.supplierEditedIndicator.observe(this.viewLifecycleOwner) {
            this.howToObserve(it)
        }

    }

    private fun howToObserve(observedData: Pair<Int, String>) {
        if (observedData.first == Status.STARTED) {

            this.binding.apply {

                saveBtn.isClickable = false
                progressBar.visibility = View.VISIBLE
                parentLayout.alpha = 0.5F
            }

        } else if (observedData.first == Status.SUCCESS) {

            Snackbar.make(
                requireParentFragment().requireView(),
                "Supplier added successfully",
                Snackbar.LENGTH_SHORT
            ).show()

            if (this.toAdd) {
                this.isSupplierAdded = true
            }
            if (this.toEdit) {
                this.isSupplierUpdated = true
            }

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