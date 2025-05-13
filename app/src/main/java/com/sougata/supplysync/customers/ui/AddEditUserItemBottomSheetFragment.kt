package com.sougata.supplysync.customers.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.customers.viewmodels.AddEditUserItemViewModel
import com.sougata.supplysync.databinding.BottomSheetAddEditUserItemBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class AddEditUserItemBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddEditUserItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditUserItemViewModel

    private lateinit var prevUserItem: UserItem
    private var updatedUserItem: UserItem? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isUserItemAdded = false
    private var isUserItemUpdated = false
    private var isUserItemDeleted = false

    companion object {
        @JvmStatic
        fun getInstance(userItem: UserItem?, action: String) =
            AddEditUserItemBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Model.USER_ITEM, userItem)
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
            this.prevUserItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(Model.USER_ITEM, UserItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable(Model.USER_ITEM)
            }!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = BottomSheetAddEditUserItemBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.viewModel = ViewModelProvider(this)[AddEditUserItemViewModel::class.java]
        this.binding.viewModel = this.viewModel
        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()
        this.registerListeners()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.isUserItemAdded) {
            val bundle = Bundle().apply {
                putBoolean(Keys.DATA_ADDED, isUserItemAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }
        if (this.isUserItemUpdated) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_UPDATED, updatedUserItem)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }
        if (this.isUserItemDeleted) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_REMOVED, prevUserItem)
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
                this.viewModel.addUserItem(this.binding.root)
            } else if (this.toEdit) {
                this.updatedUserItem = this.viewModel.updateUserItem(
                    this.prevUserItem.id, this.prevUserItem.timestamp,
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
                    this.viewModel.deleteUserItem(this.prevUserItem)
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
                name.value = prevUserItem.name.toString()
                price.value = prevUserItem.price.toString()
                inStock.value = prevUserItem.inStock.toString()
                details.value = prevUserItem.details.toString()
            }
        }

    }

    private fun registerListeners() {

        this.viewModel.userItemAddedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isUserItemAdded = true
            }
            this.howToObserve(it, "Item added successfully")
        }
        this.viewModel.userItemEditedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isUserItemUpdated = true
            }
            this.howToObserve(it, "Item updated successfully")
        }
        this.viewModel.userItemDeletedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isUserItemDeleted = true
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