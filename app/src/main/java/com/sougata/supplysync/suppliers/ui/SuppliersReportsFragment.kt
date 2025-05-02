package com.sougata.supplysync.suppliers.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentSuppliersReportsBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.suppliers.viewmodels.SupplierReportsViewModel
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SuppliersReportsFragment : Fragment() {

    private var _binding: FragmentSuppliersReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SupplierReportsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_suppliers_reports, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[SupplierReportsViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.setUpBarChart(this.binding.purchasedItemsCompChart)

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

    private fun initializeUI() {


        this.binding.supplierPaymentsBtn.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setSelection(
                        Pair(
                            MaterialDatePicker.thisMonthInUtcMilliseconds(),
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    )
                    .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                    .setTitleText("Select dates")
                    .build()

            dateRangePicker.addOnPositiveButtonClickListener {
                val startDateMillis = it.first
                val endDateMillis = it.second

                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formatedStartDate = dateFormat.format(Date(startDateMillis))
                val formatedEndDate = dateFormat.format(Date(endDateMillis))
                this.viewModel.generateSupplierPaymentsPdf(formatedStartDate, formatedEndDate)
                this.viewModel.purchasedItemsCompChartRangeDate.postValue("From: $formatedStartDate To: $formatedEndDate")
            }

            dateRangePicker.show(this.parentFragmentManager, "dateRangePicker")
        }

        this.binding.purchasedItemsChartCompCalendarBtn.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setSelection(
                        Pair(
                            MaterialDatePicker.thisMonthInUtcMilliseconds(),
                            MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    )
                    .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                    .setTitleText("Select dates")
                    .build()

            dateRangePicker.addOnPositiveButtonClickListener {

                val startDateMillis = it.first
                val endDateMillis = it.second

                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formatedStartDate = dateFormat.format(Date(startDateMillis))
                val formatedEndDate = dateFormat.format(Date(endDateMillis))
//                Log.d("date", "$formatedStartDate\t$formatedEndDate")
                this.viewModel.loadPurchasedItemsCompBarChartData(
                    formatedStartDate,
                    formatedEndDate
                )
                this.viewModel.purchasedItemsCompChartRangeDate.postValue("From: $formatedStartDate To: $formatedEndDate")

            }

            dateRangePicker.show(this.parentFragmentManager, "dateRangePicker")
        }
    }

    private fun registerListeners() {

        this.viewModel.supplierPaymentsListPdfIndicator.observe(this.viewLifecycleOwner) {

            if(it.first == Status.STARTED) {
                this.binding.supplierPaymentsBtn.text = "Generating..."
            } else if (it.first == Status.SUCCESS) {

                this.binding.supplierPaymentsBtn.text = "Payments To Suppliers"

            }else if (it.first == Status.FAILED) {
                this.binding.supplierPaymentsBtn.text = "Payments To Suppliers"
                this.onFailedToLoadData(it.second)
            }

        }

        this.viewModel.purchasedItemsCompChartData.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

                this.binding.progressBarPurchasedItemsCompChart.visibility = View.VISIBLE

            } else if (it.second == Status.SUCCESS) {

                binding.purchasedItemsCompChart.apply {

                    data = it.first

                    xAxis.valueFormatter =
                        IndexAxisValueFormatter(viewModel.purchasedItemsCompChartXStrings)

                    setVisibleXRangeMaximum(10f)

                }

                binding.apply {
                    purchasedItemsCompChart.visibility = View.VISIBLE
                    purchasedItemsCompChart.animateY(1000)
                    progressBarPurchasedItemsCompChart.visibility = View.GONE
                    purchasedItemsCompDateRange.visibility = View.VISIBLE
                }

            } else if (it.second == Status.FAILED) {

                this.onFailedToLoadData(it.third)

            }
        }

    }

    private fun setUpBarChart(myBarChart: BarChart) {
        val bwColor = requireContext().getColor(R.color.bw)

        myBarChart.apply {

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = bwColor
                textSize = 11f
                granularity = 1f
            }

            axisLeft.apply {
                textSize = 11f
                textColor = bwColor
                gridColor = bwColor
            }

            axisRight.isEnabled = false
            legend.isEnabled = false

            isDragEnabled = true
            setScaleEnabled(true)

            description.isEnabled = false

            setFitBars(true)

        }
    }

    private fun onFailedToLoadData(message: String) {
        if (message == KeysAndMessages.USER_NOT_FOUND) {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } else {
//            Log.d("err", message)
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        }
    }
}


