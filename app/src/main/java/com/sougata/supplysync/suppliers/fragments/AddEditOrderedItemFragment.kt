package com.sougata.supplysync.suppliers.fragments

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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentAddEditOrderedItemBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.suppliers.viewmodels.AddEditOrderedItemViewModel
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.modelslist.ModelsListBottomSheetFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditOrderedItemFragment : Fragment() {

    private lateinit var binding: FragmentAddEditOrderedItemBinding

    private lateinit var viewModel: AddEditOrderedItemViewModel

    private var prevOrderedItem: OrderedItem? = null
    private var updatedOrderedItem: OrderedItem? = null

    private var supplierItem: SupplierItem? = null
    private var supplier: Supplier? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isOrderedItemAdded = false
    private var isOrderedItemUpdated = false


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
            }
            Log.d("order", this.prevOrderedItem.toString())

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = DataBindingUtil.inflate(
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
                KeysAndMessages.RECENT_DATA_CHANGED_KEY,
                bundle
            )
        }

        if (this.isOrderedItemUpdated) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_UPDATED_KEY, updatedOrderedItem)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY,
                bundle
            )
        }
    }

    private fun initializeUI() {
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

                val orderedItem = this.prevOrderedItem

                if (orderedItem == null) {
                    Snackbar.make(
                        requireParentFragment().requireView(),
                        KeysAndMessages.SOMETHING_WENT_WRONG,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()

                } else {

                    val supplierItem = this.supplierItem
                    val supplier = this.supplier
                    val orderedItemId = orderedItem.id

                    this.updatedOrderedItem = this.viewModel.updateOrderedItem(
                        supplierItem?.id ?: orderedItem.itemId,
                        supplierItem?.name ?: orderedItem.itemName,
                        supplier?.id ?: orderedItem.supplierId,
                        supplier?.name ?: orderedItem.supplierName,
                        orderedItemId,
                        this.binding.root
                    )

                }

            }
        }

        this.binding.openItemsListBtn.setOnClickListener {

            ModelsListBottomSheetFragment.getInstance(Model.SUPPLIERS_ITEM)
                .show(this.parentFragmentManager, "itemsList")

        }

        this.binding.openSuppliersListBtn.setOnClickListener {

            ModelsListBottomSheetFragment.getInstance(Model.SUPPLIER)
                .show(this.parentFragmentManager, "suppliersList")

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

        if (this.toAdd) {
            this.binding.itemName.visibility = View.GONE
            this.binding.supplierName.visibility = View.GONE
        }

        if (this.toEdit) {

            this.viewModel.apply {

                val orderedItem = prevOrderedItem

                if(orderedItem == null) {
                    return@apply
                }
                amount.value = orderedItem.amount.toString()
                quantity.value = orderedItem.quantity.toString()

                var year = 0
                var month = 0
                var myDate = 0

                Converters.getYearMonthDateFromTimestamp(orderedItem.timestamp).apply {
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

                itemName.value = "Item: ${orderedItem.itemName}"
                supplierName.value = "Supplier: ${orderedItem.supplierName}"
                isReceived.value = orderedItem.isReceived
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
                this.binding.itemName.visibility = View.VISIBLE
                this.viewModel.itemName.value = "Item: ${supplierItem?.name}"
            }
            if (model is Supplier) {
                this.supplier = model
                this.binding.supplierName.visibility = View.VISIBLE
                this.viewModel.supplierName.value = "Supplier: ${supplier?.name}"
//                Log.d("listen", this.supplier.toString())
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


