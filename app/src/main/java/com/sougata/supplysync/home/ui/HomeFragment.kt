package com.sougata.supplysync.home.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentHomeBinding
import com.sougata.supplysync.home.viewmodels.HomeFragmentViewModel
import com.sougata.supplysync.sharedviewmodels.CommonDataViewModel
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var commonDataViewModel: CommonDataViewModel

    private var isDataAdded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[HomeFragmentViewModel::class.java]
        this.commonDataViewModel =
            ViewModelProvider(requireActivity())[CommonDataViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.setUpLineCHart(this.binding.purchaseChart.lineChart)
        this.setUpLineCHart(this.binding.salesChart.lineChart)

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bundle = Bundle().apply {
            putBoolean(
                KeysAndMessages.DATA_ADDED_KEY, isDataAdded
            )
        }
        this.parentFragmentManager.setFragmentResult(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY,
            bundle
        )

        this._binding = null
    }

    private fun initializeUI() {

        this.binding.apply {
            ordersToReceive.heading.text = "Orders to receive"
            ordersToDeliver.heading.text = "Orders to deliver"

            salesByRange.heading.text = "Sales"
            purchaseByRange.heading.text = "Purchase"

            salesChart.heading.text = "Sales chart"
            purchaseChart.heading.text = "Purchase chart"
        }

        this.binding.salesByRange.calendarBtn.setOnClickListener {
            this.openDateRangePicker { startDateMillis, endDateMillis ->
                this.commonDataViewModel.loadSalesAmountByRange(startDateMillis, endDateMillis)
            }
        }

        this.binding.purchaseByRange.calendarBtn.setOnClickListener {
            this.openDateRangePicker { startDateMillis, endDateMillis ->
                this.commonDataViewModel.loadPurchaseAmountByRange(startDateMillis, endDateMillis)
            }
        }

        this.binding.purchaseChart.calendarBtn.setOnClickListener {
            this.openDateRangePicker { startDateMillis, endDateMillis ->
                this.viewModel.loadPurchaseLineChartData(startDateMillis, endDateMillis)
            }
        }

        this.binding.salesChart.calendarBtn.setOnClickListener {
            this.openDateRangePicker { startDateMillis, endDateMillis ->
                this.viewModel.loadSalesLineChartData(startDateMillis, endDateMillis)
            }
        }
    }

    private fun registerListeners() {

        this.commonDataViewModel.numberOfOrdersToReceive.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.ordersToReceive.value.text = it.first.toString()

            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.commonDataViewModel.salesAmountByRange.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.salesByRange.value.text = Converters.numberToMoneyString(it.first)
                this.binding.salesByRange.dateRange.text =
                    this.commonDataViewModel.salesAmountDateRange

            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.commonDataViewModel.purchaseAmountByRange.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.purchaseByRange.value.text = Converters.numberToMoneyString(it.first)
                this.binding.purchaseByRange.dateRange.text =
                    this.commonDataViewModel.purchaseAmountDateRange

            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.commonDataViewModel.numberOfOrdersToDeliver.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.ordersToDeliver.value.text = it.first.toString()

            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY, this.viewLifecycleOwner
        ) { requestKey, bundle ->

            this.isDataAdded = bundle.getBoolean(KeysAndMessages.DATA_ADDED_KEY)

            if (isDataAdded) {
                this.commonDataViewModel.apply {
                    loadOrdersToReceive()
                    loadOrdersToDeliver()
                    loadPast30DaysSalesAmount()
                    loadPast30DaysPurchaseAmount()
                }
            }

        }

        this.registerPurchaseChartListener()
        this.registerSalesChartListener()

    }

    private fun registerPurchaseChartListener() {
        this.viewModel.purchaseChartData.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {
                this.binding.purchaseChart.progressBar.visibility = View.VISIBLE
            } else if (it.second == Status.SUCCESS) {

                this.binding.purchaseChart.lineChart.apply {

                    this.data = it.first

                    this.axisLeft.valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String? {
                            return Converters.getShortedNumberString(value.toDouble())
                        }
                    }

                    this.setVisibleXRangeMaximum(15f)

                    this.notifyDataSetChanged()

                    this.visibility = View.VISIBLE

                    if (viewModel.animatePurchaseChart) {
                        animateY(1000)
                        viewModel.animatePurchaseChart = false
                    }
                }

                this.binding.purchaseChart.progressBar.visibility = View.GONE
                this.binding.purchaseChart.dateRange.text = this.viewModel.purchaseChartDateRange

            } else if (it.second == Status.FAILED) {
                this.binding.purchaseChart.progressBar.visibility = View.GONE
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun registerSalesChartListener() {
        this.viewModel.salesChartData.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {
                this.binding.salesChart.progressBar.visibility = View.VISIBLE
            } else if (it.second == Status.SUCCESS) {
                this.binding.salesChart.lineChart.apply {
                    this.data = it.first

                    this.axisLeft.valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String? {
                            return Converters.getShortedNumberString(value.toDouble())
                        }
                    }

                    this.setVisibleXRangeMaximum(15f)

                    this.notifyDataSetChanged()

                    this.visibility = View.VISIBLE

                    if (viewModel.animateSalesChart) {
                        animateY(1000)
                        viewModel.animateSalesChart = false
                    }
                }

                this.binding.salesChart.progressBar.visibility = View.GONE
                this.binding.salesChart.dateRange.text = this.viewModel.salesChartDateRange

            } else if (it.second == Status.FAILED) {
                this.binding.salesChart.progressBar.visibility = View.GONE
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpLineCHart(myLineChart: LineChart) {
        val bwColor = requireContext().getColor(R.color.bw)

        myLineChart.apply {

            xAxis.apply {
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                textColor = bwColor
                textSize = 11f
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(false)
                textSize = 11f
                textColor = bwColor

            }

            axisRight.isEnabled = false
            legend.isEnabled = false

            // Zooming
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            description.isEnabled = false

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