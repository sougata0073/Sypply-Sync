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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sougata.supplysync.R
import com.sougata.supplysync.cloud.SupplierFirestoreRepository
import com.sougata.supplysync.databinding.FragmentAddEditSupplierPaymentBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.suppliers.viewmodels.AddEditSupplierPaymentViewModel
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.modelslist.ModelsListBottomSheetFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditSupplierPaymentFragment : Fragment() {

    private var _binding: FragmentAddEditSupplierPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddEditSupplierPaymentViewModel

    private lateinit var prevSupplierPayment: SupplierPayment
    private var updatedSupplierPayment: SupplierPayment? = null

    // If any other model related to this model they will be here
    private var supplier: Supplier? = null

    // Flags
    private var toAdd: Boolean = false
    private var toEdit: Boolean = false
    private var isSupplierPaymentAdded = false
    private var isSupplierPaymentUpdated = false
    private var isSupplierPaymentDeleted = false

    private val supplierFirestoreRepository = SupplierFirestoreRepository()

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
            }!!

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding =
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
                KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT,
                bundle
            )
        }

        if (this.isSupplierPaymentUpdated) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_UPDATED_KEY, updatedSupplierPayment)
            }
            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT,
                bundle
            )
        }

        if (this.isSupplierPaymentDeleted) {
            val bundle = Bundle().apply {
                putParcelable(KeysAndMessages.DATA_REMOVED_KEY, prevSupplierPayment)
            }

            this.parentFragmentManager.setFragmentResult(
                KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT,
                bundle
            )
        }

        this._binding = null
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

                val supplier = this.supplier
                val supplierPaymentId = this.prevSupplierPayment.id
                this.updatedSupplierPayment = this.viewModel.updateSupplierPayment(
                    supplier?.id ?: this.prevSupplierPayment.supplierId,
                    supplier?.name ?: this.prevSupplierPayment.supplierName,
                    supplierPaymentId,
                    this.binding.root
                )

            }
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
                val formatedDate = dateFormat.format(Date(it))

                this.viewModel.date.value = formatedDate
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

        this.binding.deleteBtn.setOnClickListener {

            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.materialAlertDialogStyle
            ).setTitle("Warning")
                .setMessage("Are you sure you want to delete this payment?")
                .setPositiveButton("Yes") { dialog, _ ->

                    this.binding.parentLayout.alpha = 0.5f
                    this.binding.progressBar.visibility = View.VISIBLE

                    this.supplierFirestoreRepository.deleteSupplierPayment(this.prevSupplierPayment) { status, message ->

                        Snackbar.make(
                            requireParentFragment().requireView(),
                            message,
                            Snackbar.LENGTH_SHORT
                        ).show()

                        if (status == Status.SUCCESS) {

                            this.isSupplierPaymentDeleted = true

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
            binding.supplierName.visibility = View.GONE
            binding.deleteBtn.visibility = View.INVISIBLE
        }

        if (this.toEdit) {
            this.binding.deleteBtn.visibility = View.VISIBLE
            this.viewModel.apply {
                amount.value = prevSupplierPayment.amount.toString()

                var year = 0
                var month = 0
                var myDate = 0

                Converters.getDateFromTimestamp(prevSupplierPayment.timestamp).apply {
                    year = first
                    month = second
                    myDate = third
                }

                val dateString = String.format(
                    Locale.getDefault(),
                    "%02d-%02d-%04d", myDate, month, year
                )

                var hour = 0
                var minute = 0

                Converters.getTimeFromTimestamp(prevSupplierPayment.timestamp).apply {
                    hour = first
                    minute = second
                }

                val timeString = String.format(
                    Locale.getDefault(),
                    "%02d:%02d", hour, minute
                )

                date.value = dateString
                time.value = timeString
                note.value = prevSupplierPayment.note
                supplierName.value = "Supplier: ${prevSupplierPayment.supplierName}"
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

            this.binding.supplierName.visibility = View.VISIBLE
            this.viewModel.supplierName.value = "Supplier: ${supplier?.name}"
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