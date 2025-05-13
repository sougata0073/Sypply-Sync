package com.sougata.supplysync.customers.ui

import android.icu.util.Calendar
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
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sougata.supplysync.R
import com.sougata.supplysync.customers.viewmodels.AddEditCustomerPaymentViewModel
import com.sougata.supplysync.databinding.FragmentAddEditCustomerPaymentBinding
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditCustomerPaymentFragment : Fragment() {

    private var _binding: FragmentAddEditCustomerPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditCustomerPaymentViewModel

    private lateinit var prevCustomerPayment: CustomerPayment
    private var updatedCustomerPayment: CustomerPayment? = null

    private var customer: Customer? = null

    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isCustomerPaymentAdded = false
    private var isCustomerPaymentUpdated = false
    private var isCustomerPaymentDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(Keys.TO_ADD)
        this.toEdit = requireArguments().getBoolean(Keys.TO_EDIT)

        if (this.toEdit) {
            this.prevCustomerPayment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(
                    Model.CUSTOMER_PAYMENT,
                    CustomerPayment::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable(Model.CUSTOMER_PAYMENT)
            }!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this._binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_edit_customer_payment,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[AddEditCustomerPaymentViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()

        this.registerListeners()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.isCustomerPaymentAdded) {
            val bundle = Bundle().apply {
                putBoolean(Keys.DATA_ADDED, isCustomerPaymentAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }

        if (this.isCustomerPaymentUpdated) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_UPDATED, updatedCustomerPayment)
            }
            this.parentFragmentManager.setFragmentResult(
                Keys.RECENT_DATA_CHANGED_ADD_EDIT,
                bundle
            )
        }

        if (this.isCustomerPaymentDeleted) {
            val bundle = Bundle().apply {
                putParcelable(Keys.DATA_REMOVED, prevCustomerPayment)
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
            this.binding.deleteBtn.visibility = View.VISIBLE
            this.viewModel.apply {
                amount.value = prevCustomerPayment.amount.toString()

                val dateString = DateTime.getDateStringFromTimestamp(prevCustomerPayment.timestamp)
                val timeString = DateTime.getTimeStringFromTimestamp(prevCustomerPayment.timestamp)

                date.value = dateString
                time.value = timeString
                note.value = prevCustomerPayment.note
                customerName.value = "Customer: ${prevCustomerPayment.customerName}"
            }
        }

        this.setUpDeleteButton()
        this.setUpDateTimeButton()
        this.setUpSaveButton()
        this.setUpOpenCustomersListButton()
    }

    private fun registerListeners() {
        this.viewModel.customerPaymentAddedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isCustomerPaymentAdded = true
            }
            observe(it, "Payment added successfully")
        }
        this.viewModel.customerPaymentEditedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isCustomerPaymentUpdated = true
            }
            observe(it, "Payment updated successfully")
        }
        this.viewModel.customerPaymentDeletedIndicator.observe(this.viewLifecycleOwner) {
            if (it.first == Status.SUCCESS) {
                this.isCustomerPaymentDeleted = true
            }
            observe(it, "Payment deleted successfully")
        }

        this.parentFragmentManager.setFragmentResultListener(
            Keys.ITEM_SELECTED,
            this
        )
        { requestKey, bundle ->
            this.customer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(
                    Keys.MODEL,
                    Customer::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(Keys.MODEL)
            }

            this.viewModel.customerName.value = "Customer: ${customer?.name}"
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

    private fun setUpSaveButton() {
        this.binding.saveBtn.setOnClickListener {
            if (this.toAdd) {
                val customer = this.customer

                if (customer == null) {
                    Snackbar.make(
                        this.binding.root,
                        "Select a customer first",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    this.viewModel.addCustomerPayment(customer.id, customer.name, this.binding.root)
                }

            } else if (this.toEdit) {

                val customer = this.customer

                this.updatedCustomerPayment = this.viewModel.updateCustomerPayment(
                    this.prevCustomerPayment.id, this.prevCustomerPayment.timestamp,

                    customer?.id ?: this.prevCustomerPayment.customerId,
                    customer?.name ?: this.prevCustomerPayment.customerName,
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
                        .setMessage("Are you sure you want to delete this payment?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            viewModel.deleteCustomerPayment(prevCustomerPayment)
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    private fun setUpDateTimeButton() {
        this.binding.calendarBtn.setOnClickListener {

            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formatedDate = dateFormat.format(Date(it))

                this.viewModel.date.value = formatedDate
            }
            datePicker.show(this.parentFragmentManager, "datePicker")

        }

        this.binding.clockBtn.setOnClickListener {

            val calendar = Calendar.getInstance()

            val timePicker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendar.get(Calendar.MINUTE))
                    .setTitleText("Select time")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .build()

            timePicker.addOnPositiveButtonClickListener {
                this.viewModel.time.value = "${timePicker.hour}:${timePicker.minute}"
            }
            timePicker.show(this.parentFragmentManager, "timePicker")
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