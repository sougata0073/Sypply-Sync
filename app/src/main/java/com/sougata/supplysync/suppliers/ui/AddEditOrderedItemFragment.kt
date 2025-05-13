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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentAddEditOrderedItemBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.suppliers.viewmodels.AddEditOrderedItemViewModel
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditOrderedItemFragment : Fragment() {

    private var _binding: FragmentAddEditOrderedItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditOrderedItemViewModel

    private lateinit var prevOrderedItem: OrderedItem
    private var updatedOrderedItem: OrderedItem? = null

    private var supplierItem: SupplierItem? = null
    private var supplier: Supplier? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isOrderedItemAdded = false
    private var isOrderedItemUpdated = false
    private var isOrderedItemDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(Keys.TO_ADD)
        this.toEdit = requireArguments().getBoolean(Keys.TO_EDIT)

        if (this.toEdit) {
            this.prevOrderedItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(Model.ORDERED_ITEM, OrderedItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable(Model.ORDERED_ITEM)
            }!!

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_edit_ordered_item,
            container,
            false
        )
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[AddEditOrderedItemViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.isOrderedItemAdded) {
            val bundle = Bundle().apply {
                putBoolean(Keys.DATA_ADDED, isOrderedItemAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }

        if (this.isOrderedItemUpdated) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_UPDATED, updatedOrderedItem)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }

        if (this.isOrderedItemDeleted) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_REMOVED, prevOrderedItem)
            }

            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }

        this._binding = null
    }

    private fun initializeUI() {

        if (this.toEdit) {
            this.viewModel.apply {

                amount.value = prevOrderedItem.amount.toString()
                quantity.value = prevOrderedItem.quantity.toString()

                val dateString = DateTime.getDateStringFromTimestamp(prevOrderedItem.timestamp)

                date.value = dateString

                itemName.value = "Item: ${prevOrderedItem.supplierItemName}"
                supplierName.value = "Supplier: ${prevOrderedItem.supplierName}"
                isReceived.value = prevOrderedItem.received
            }
        }

        this.setUpCalendarButton()
        this.setUpDeleteButton()
        this.setUpSaveButton()
        this.setUpOpenItemsListButton()
        this.setUpOpenSuppliersListButton()
    }

    private fun registerListeners() {
        this.viewModel.orderedItemAddedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isOrderedItemAdded = true
            }
            observe(it, "Ordered item added successfully")
        }
        this.viewModel.orderedItemEditedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isOrderedItemUpdated = true
            }
            observe(it, "Ordered item updated successfully")
        }
        this.viewModel.orderedItemDeletedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isOrderedItemDeleted = true
            }
            observe(it, "Ordered item deleted successfully")
        }
        this.parentFragmentManager.setFragmentResultListener(
            Keys.ITEM_SELECTED,
            this
        )
        { requestKey, bundle ->
            val model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(
                    Keys.MODEL,
                    Model::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(Keys.MODEL)
            }
            if (model is SupplierItem) {
                this.supplierItem = model
                this.viewModel.itemName.value = "Item: ${supplierItem?.name}"
            }
            if (model is Supplier) {
                this.supplier = model
                this.viewModel.supplierName.value = "Supplier: ${supplier?.name}"
            }
        }
    }

    private fun setUpCalendarButton() {
        this.binding.calendarBtn.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val selectedDate = dateFormat.format(Date(it))

                this.viewModel.date.value = selectedDate
            }
            datePicker.show(this.parentFragmentManager, "datePicker")
        }
    }

    private fun setUpDeleteButton() {
        if (this.toAdd) {
            this.binding.deleteBtn.visibility = View.INVISIBLE
        } else if (this.toEdit) {
            this.binding.deleteBtn.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.materialAlertDialogStyle
                    ).setTitle("Warning")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            viewModel.deleteOrderedItem(prevOrderedItem)
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    private fun setUpSaveButton() {
        this.binding.saveBtn.setOnClickListener {
            if (this.toAdd) {
                val supplierItem = this.supplierItem
                val supplier = this.supplier

                if (supplierItem == null) {
                    Snackbar.make(
                        this.binding.root, "Select an item first", Snackbar.LENGTH_SHORT
                    ).show()
                } else if (supplier == null) {
                    Snackbar.make(
                        this.binding.root, "Select a supplier first", Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    this.viewModel.addOrderedItem(
                        supplierItem.id,
                        supplierItem.name,
                        supplier.id,
                        supplier.name,
                        this.binding.root
                    )
                }
            } else if (this.toEdit) {
                val supplierItem = this.supplierItem
                val supplier = this.supplier

                this.updatedOrderedItem = this.viewModel.updateOrderedItem(

                    this.prevOrderedItem.id, this.prevOrderedItem.timestamp,

                    supplierItem?.id ?: this.prevOrderedItem.supplierItemId,
                    supplierItem?.name ?: this.prevOrderedItem.supplierItemName,
                    supplier?.id ?: this.prevOrderedItem.supplierId,
                    supplier?.name ?: this.prevOrderedItem.supplierName,
                    this.binding.root
                )
            }
        }
    }

    private fun setUpOpenItemsListButton() {
        this.binding.openItemsListBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putString(Keys.MODEL_NAME, Model.SUPPLIER_ITEM)
                putBoolean(Keys.SELECT_ONLY, true)
            }

            findNavController().navigate(
                R.id.modelsListFragment,
                bundle,
                AnimationProvider.slideUpDownNavOptions()
            )

        }
    }

    private fun setUpOpenSuppliersListButton() {
        this.binding.openSuppliersListBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putString(Keys.MODEL_NAME, Model.SUPPLIER)
                putBoolean(Keys.SELECT_ONLY, true)
            }

            findNavController().navigate(
                R.id.modelsListFragment,
                bundle,
                AnimationProvider.slideUpDownNavOptions()
            )

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

            findNavController().popBackStack()

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


