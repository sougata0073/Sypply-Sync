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
import com.sougata.supplysync.remote.SupplierFirestoreRepository
import com.sougata.supplysync.databinding.FragmentAddEditOrderedItemBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.suppliers.viewmodels.AddEditOrderedItemViewModel
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
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

    private val supplierFirestoreRepository = SupplierFirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(KeysAndMessages.TO_ADD_KEY)
        this.toEdit = requireArguments().getBoolean(KeysAndMessages.TO_EDIT_KEY)

        if (this.toEdit) {
            this.prevOrderedItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable("orderedItem", OrderedItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable("orderedItem")
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
                putBoolean(KeysAndMessages.DATA_ADDED_KEY, isOrderedItemAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT,
                bundle
            )
        }

        if (this.isOrderedItemUpdated) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_UPDATED_KEY, updatedOrderedItem)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT,
                bundle
            )
        }

        if (this.isOrderedItemDeleted) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_REMOVED_KEY, prevOrderedItem)
            }

            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT,
                bundle
            )
        }

        this._binding = null
    }

    private fun initializeUI() {

        if (this.toAdd) {
            this.binding.deleteBtn.visibility = View.INVISIBLE
        }

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
                val orderedItemId = this.prevOrderedItem.id

                this.updatedOrderedItem = this.viewModel.updateOrderedItem(
                    supplierItem?.id ?: this.prevOrderedItem.itemId,
                    supplierItem?.name ?: this.prevOrderedItem.itemName,
                    supplier?.id ?: this.prevOrderedItem.supplierId,
                    supplier?.name ?: this.prevOrderedItem.supplierName,
                    orderedItemId,
                    this.binding.root
                )
            }
        }

        this.binding.openItemsListBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putString(KeysAndMessages.MODEL_NAME_KEY, Model.SUPPLIERS_ITEM)
                putBoolean(KeysAndMessages.IS_SELECT_ONLY_KEY, true)
            }

            findNavController().navigate(
                R.id.modelsListFragment,
                bundle,
                AnimationProvider.fragmentAnimationSlideUpDown()
            )

        }

        this.binding.openSuppliersListBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putString(KeysAndMessages.MODEL_NAME_KEY, Model.SUPPLIER)
                putBoolean(KeysAndMessages.IS_SELECT_ONLY_KEY, true)
            }

            findNavController().navigate(
                R.id.modelsListFragment,
                bundle,
                AnimationProvider.fragmentAnimationSlideUpDown()
            )

        }

        this.binding.calendarBtn.setOnClickListener {

            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
//                    .setTheme(R.style.materialDatePickerStyle)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val selectedDate = dateFormat.format(Date(it))

                this.viewModel.date.value = selectedDate
            }

            datePicker.show(this.parentFragmentManager, "datePicker")

        }

        this.binding.deleteBtn.setOnClickListener {

            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.materialAlertDialogStyle
            ).setTitle("Warning")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, _ ->

                    this.binding.parentLayout.alpha = 0.5f
                    this.binding.progressBar.visibility = View.VISIBLE

                    this.supplierFirestoreRepository.deleteOrderedItem(this.prevOrderedItem) { status, message ->

                        Snackbar.make(
                            requireParentFragment().requireView(),
                            message,
                            Snackbar.LENGTH_SHORT
                        ).show()

                        if (status == Status.SUCCESS) {

                            this.isOrderedItemDeleted = true

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

        if (this.toEdit) {
            this.binding.deleteBtn.visibility = View.VISIBLE
            this.viewModel.apply {

                amount.value = prevOrderedItem.amount.toString()
                quantity.value = prevOrderedItem.quantity.toString()

                var year = 0
                var month = 0
                var myDate = 0

                Converters.getDateFromTimestamp(prevOrderedItem.timestamp).apply {
                    year = first
                    month = second
                    myDate = third
                }

                val dateString = String.format(
                    Locale.getDefault(),
                    "%02d-%02d-%04d",
                    myDate, month, year
                )

                date.value = dateString

                itemName.value = "Item: ${prevOrderedItem.itemName}"
                supplierName.value = "Supplier: ${prevOrderedItem.supplierName}"
                isReceived.value = prevOrderedItem.isReceived
            }
        }
    }

    private fun registerListeners() {
        this.viewModel.orderedItemAddedIndicator.observe(this.viewLifecycleOwner) {
            howToObserve(it)
        }
        this.viewModel.orderedItemEditedIndicator.observe(this.viewLifecycleOwner) {
            howToObserve(it)
        }

        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.ITEM_SELECTED_KEY,
            this
        )
        { requestKey, bundle ->

            val model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(
                    KeysAndMessages.MODEL_KEY,
                    Model::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(KeysAndMessages.MODEL_KEY)
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
                "Ordered item added successfully",
                Snackbar.LENGTH_SHORT
            ).show()

            if (this.toAdd) {
                this.isOrderedItemAdded = true
            }
            if (this.toEdit) {
                this.isOrderedItemUpdated = true
            }

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


