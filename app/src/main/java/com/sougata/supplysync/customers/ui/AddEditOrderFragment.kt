package com.sougata.supplysync.customers.ui

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
import com.sougata.supplysync.customers.viewmodels.AddEditOrderViewModel
import com.sougata.supplysync.databinding.FragmentAddEditOrderBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditOrderFragment : Fragment() {

    private var _binding: FragmentAddEditOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditOrderViewModel

    private lateinit var prevOrder: Order
    private var updatedOrder: Order? = null

    private var userItem: UserItem? = null
    private var customer: Customer? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isOrderAdded = false
    private var isOrderUpdated = false
    private var isOrderDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(Keys.TO_ADD)
        this.toEdit = requireArguments().getBoolean(Keys.TO_EDIT)

        if (this.toEdit) {
            this.prevOrder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(Model.ORDER, Order::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable(Model.ORDER)
            }!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_edit_order, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[AddEditOrderViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()

        this.registerListeners()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.isOrderAdded) {
            val bundle = Bundle().apply {
                putBoolean(Keys.DATA_ADDED, isOrderAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }

        if (this.isOrderUpdated) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_UPDATED, updatedOrder)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }

        if (this.isOrderDeleted) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_REMOVED, prevOrder)
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

                amount.value = prevOrder.amount.toString()
                quantity.value = prevOrder.quantity.toString()

                val dateString = DateTime.getDateStringFromTimestamp(prevOrder.timestamp)

                date.value = dateString

                itemName.value = "Item: ${prevOrder.userItemName}"
                customerName.value = "Customer: ${prevOrder.customerName}"
                isDelivered.value = prevOrder.delivered
            }
        }

        this.setUpCalendarButton()
        this.setUpDeleteButton()
        this.setUpSaveButton()
        this.setUpOpenItemsListButton()
        this.setUpOpenCustomersListButton()
    }

    private fun registerListeners() {
        this.viewModel.orderAddedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isOrderAdded = true
            }
            observe(it, "Order added successfully")
        }
        this.viewModel.orderEditedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isOrderUpdated = true
            }
            observe(it, "Order updated successfully")
        }
        this.viewModel.orderDeletedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isOrderDeleted = true
            }
            observe(it, "Order deleted successfully")
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
            if (model is UserItem) {
                this.userItem = model
                this.viewModel.itemName.value = "Item: ${userItem?.name}"
            }
            if (model is Customer) {
                this.customer = model
                this.viewModel.customerName.value = "Customer: ${customer?.name}"
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
                            viewModel.deleteOrder(prevOrder)
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
                val userItem = this.userItem
                val customer = this.customer

                if (userItem == null) {
                    Snackbar.make(
                        this.binding.root, "Select an item first", Snackbar.LENGTH_SHORT
                    ).show()
                } else if (customer == null) {
                    Snackbar.make(
                        this.binding.root, "Select a customer first", Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    this.viewModel.addOrder(
                        userItem.id,
                        userItem.name,
                        customer.id,
                        customer.name,
                        this.binding.root
                    )
                }
            } else if (this.toEdit) {
                val userItem = this.userItem
                val customer = this.customer

                this.updatedOrder = this.viewModel.updateOrder(
                    this.prevOrder.id, this.prevOrder.timestamp,

                    userItem?.id ?: this.prevOrder.userItemId,
                    userItem?.name ?: this.prevOrder.userItemName,
                    customer?.id ?: this.prevOrder.customerId,
                    customer?.name ?: this.prevOrder.customerName,
                    this.binding.root
                )
            }
        }
    }

    private fun setUpOpenItemsListButton() {
        this.binding.openItemsListBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putString(Keys.MODEL_NAME, Model.USER_ITEM)
                putBoolean(Keys.SELECT_ONLY, true)
            }

            findNavController().navigate(
                R.id.modelsListFragment,
                bundle,
                AnimationProvider.slideUpDownNavOptions()
            )

        }
    }

    private fun setUpOpenCustomersListButton() {
        this.binding.openCustomersListBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putString(Keys.MODEL_NAME, Model.CUSTOMER)
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