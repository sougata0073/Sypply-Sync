package com.sougata.supplysync.suppliers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentSuppliersHomeBinding
import com.sougata.supplysync.sharedviewmodels.CommonDataViewModel
import com.sougata.supplysync.suppliers.viewmodels.SuppliersHomeViewModel
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status

class SuppliersHomeFragment : Fragment() {

    private var _binding: FragmentSuppliersHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SuppliersHomeViewModel
    private lateinit var commonDataViewModel: CommonDataViewModel

    private var isDataAdded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_suppliers_home,
            container,
            false
        )

        this.binding.apply {
            scrollView.visibility = View.GONE
            mainProgressBar.visibility = View.VISIBLE
        }

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[SuppliersHomeViewModel::class.java]
        this.commonDataViewModel =
            ViewModelProvider(requireActivity())[CommonDataViewModel::class.java]

        this.binding.dueToSuppliers.value.text = Converters.numberToMoneyString(0.00)

        this.binding.viewModel = this.viewModel

        this.initializeUI()

        this.registerSubscribers()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bundle = Bundle().apply {
            putBoolean(
                Keys.DATA_ADDED, isDataAdded
            )
        }
        this.parentFragmentManager.setFragmentResult(
            Keys.RECENT_DATA_CHANGED,
            bundle
        )

        this._binding = null
    }

    private fun initializeUI() {
        this.binding.apply {
            ordersToReceive.heading.text = "Orders to Receive"
            numberOfSuppliers.heading.text = "Number of Suppliers"
            purchase.heading.text = "Purchase"
            dueToSuppliers.heading.text = "Due to Suppliers"
        }

        this.binding.purchase.calendarBtn.setOnClickListener {
            this.openDateRangePicker { startDateMillis, endDateMillis ->
                this.commonDataViewModel.loadPurchaseAmountByRange(startDateMillis, endDateMillis)
            }
        }
    }

    private fun registerSubscribers() {

        this.viewModel.numberOfSuppliers.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.numberOfSuppliers.value.text = it.first.toString()

            } else if (it.second == Status.FAILED) {

                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()

            }

        }

        this.commonDataViewModel.numberOfOrdersToReceive.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.ordersToReceive.value.text = it.first.toString()

            } else if (it.second == Status.FAILED) {

                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()

            }
        }

        this.commonDataViewModel.purchaseAmountByRange.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.purchase.value.text = Converters.numberToMoneyString(it.first)
                this.binding.purchase.dateRange.text =
                    this.commonDataViewModel.purchaseAmountDateRange

            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.viewModel.dueAmountToSuppliers.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {
                this.binding.dueToSuppliers.value.text = Converters.numberToMoneyString(it.first)

            } else if (it.second == Status.FAILED) {

                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()

            }
        }

        this.viewModel.allApiCallFinished.observe(this.viewLifecycleOwner) {
            if (it) {
                this.binding.apply {
                    mainProgressBar.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                }
            }
        }

        this.parentFragmentManager.setFragmentResultListener(
            Keys.RECENT_DATA_CHANGED, this.viewLifecycleOwner
        ) { requestKey, bundle ->

            this.isDataAdded = bundle.getBoolean(Keys.DATA_ADDED)

            if (isDataAdded) {
                this.viewModel.loadDueAmountToSuppliers()
                this.viewModel.loadNumberOfSuppliers()
                this.commonDataViewModel.apply {
                    loadOrdersToReceive()
                    loadOrdersToDeliver()
                    loadPast30DaysSalesAmount()
                    loadPast30DaysPurchaseAmount()
                }
            }

        }
    }

    private fun openDateRangePicker(onPositiveButtonClick: (Long, Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(
                Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            .setTitleText("Select dates")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            onPositiveButtonClick(it.first, it.second)
        }
        datePicker.show(this.parentFragmentManager, "dateRangePicker")
    }
}