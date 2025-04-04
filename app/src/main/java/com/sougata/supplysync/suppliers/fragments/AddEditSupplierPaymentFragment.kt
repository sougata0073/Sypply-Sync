package com.sougata.supplysync.suppliers.fragments

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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentAddEditSupplierPaymentBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.suppliers.viewmodels.AddEditSupplierPaymentViewModel
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.modelslist.ModelsListBottomSheetFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditSupplierPaymentFragment : Fragment() {

    private lateinit var binding: FragmentAddEditSupplierPaymentBinding

    private lateinit var viewModel: AddEditSupplierPaymentViewModel

    private var prevSupplierPayment: SupplierPayment? = null
    private var updatedSupplierPayment: SupplierPayment? = null

    // If any other model related to this model they will be here
    private var supplier: Supplier? = null

    // Flags
    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isSupplierPaymentAdded = false
    private var isSupplierPaymentUpdated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(KeysAndMessages.TO_ADD_KEY)
        this.toEdit = requireArguments().getBoolean(KeysAndMessages.TO_EDIT_KEY)

        if (this.toEdit) {
            this.prevSupplierPayment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable("supplierPayment", SupplierPayment::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable("supplierPayment")
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_edit_supplier_payment,
                container,
                false
            )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[AddEditSupplierPaymentViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this.isSupplierPaymentAdded) {
            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.DATA_ADDED_KEY, isSupplierPaymentAdded)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY,
                bundle
            )
        }

        if (this.isSupplierPaymentUpdated) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_UPDATED_KEY, updatedSupplierPayment)
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

                val supplier = this.supplier

                if (supplier == null) {
                    Snackbar.make(
                        this.binding.root,
                        "Select a supplier first",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    this.viewModel.addSupplierPayment(supplier.id, supplier.name, this.binding.root)
                }

            } else if (this.toEdit) {

                val supplierPayment = this.prevSupplierPayment

                if (supplierPayment == null) {
                    Snackbar.make(
                        requireParentFragment().requireView(),
                        KeysAndMessages.SOMETHING_WENT_WRONG,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()

                } else {

                    val supplier = this.supplier
                    val supplierPaymentId = supplierPayment.id
                    if (supplier == null) {
                        this.updatedSupplierPayment = this.viewModel.updateSupplierPayment(
                            supplierPayment.supplierId,
                            supplierPayment.supplierName,
                            supplierPaymentId,
                            this.binding.root
                        )
                    } else {
                        this.updatedSupplierPayment = this.viewModel.updateSupplierPayment(
                            supplier.id,
                            supplier.name,
                            supplierPaymentId,
                            this.binding.root
                        )
                    }
                }

            }
        }

        this.binding.openSupplierListBtn.setOnClickListener {

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

        this.binding.clockBtn.setOnClickListener {

            val calendar = Calendar.getInstance()

            val timePicker =
                MaterialTimePicker.Builder()
//                    .setTheme(R.style.materialTimePickerStyle)
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

        if (this.toEdit) {
            this.viewModel.apply {
                amount.value = prevSupplierPayment?.amount.toString()

                val dateString = String.format(
                    Locale.getDefault(),
                    "%02d-%02d-%04d",
                    prevSupplierPayment?.date,
                    prevSupplierPayment?.month,
                    prevSupplierPayment?.year
                )
                val timeString = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    prevSupplierPayment?.hour,
                    prevSupplierPayment?.minute
                )

                date.value = dateString
                time.value = timeString
                note.value = prevSupplierPayment?.note
                supplierName.value = "Supplier: ${prevSupplierPayment?.supplierName}"
            }

        }

    }

    private fun registerListeners() {
        this.viewModel.supplierPaymentAddedIndicator.observe(this.viewLifecycleOwner) {
            howToObserve(it)
        }
        this.viewModel.supplierPaymentEditedIndicator.observe(this.viewLifecycleOwner) {
            howToObserve(it)
        }

        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.ITEM_SELECTED_KEY,
            this
        )
        { requestKey, bundle ->
            this.supplier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(
                    KeysAndMessages.MODEL_KEY,
                    Supplier::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(KeysAndMessages.MODEL_KEY)
            }

            this.binding.supplierName.text = "Supplier: ${this.supplier?.name}"
//            Log.d("listen", this.supplier.toString())
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
                "Payment added successfully",
                Snackbar.LENGTH_SHORT
            ).show()

            if (this.toAdd) {
                this.isSupplierPaymentAdded = true
            }
            if (this.toEdit) {
                this.isSupplierPaymentUpdated = true
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