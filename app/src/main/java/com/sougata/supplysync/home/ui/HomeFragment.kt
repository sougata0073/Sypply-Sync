package com.sougata.supplysync.home.ui

import android.content.Intent
import android.os.Bundle
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
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeFragmentViewModel

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

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.setUpLineCHart(this.binding.purchaseLineChart)

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

        this.binding.purchaseChartCalendarBtn.setOnClickListener {
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
                this.viewModel.loadPurchaseLineChartData(formatedStartDate, formatedEndDate)
                this.viewModel.purchaseChartRangeDate.postValue("From: $formatedStartDate To: $formatedEndDate")

            }

            dateRangePicker.show(this.parentFragmentManager, "dateRangePicker")
        }


    }

    private fun registerListeners() {

        this.viewModel.numberOfOrdersToReceive.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.ordersToReceiveNumber.text = it.first.toString()

            } else if (it.second == Status.FAILED) {

                this.onFailedToLoadData(it.third)

            }
        }

        this.viewModel.purchaseChartData.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

                this.binding.progressBarPurchaseChart.visibility = View.VISIBLE

            } else if (it.second == Status.SUCCESS) {

                binding.purchaseLineChart.apply {

                    data = it.first

                    axisLeft.valueFormatter = object : IndexAxisValueFormatter() {
                        // It is called everytime when the any axis needs to show a value
                        // The parameter is the actual value and I am formatting it with my own formatter
                        override fun getFormattedValue(value: Float): String? {
//                                Log.d("value", value.toString())
                            return Converters.getShortedNumberString(value.toDouble())
                        }
                    }

                    setVisibleXRangeMaximum(15f)
                }
//                    Log.d("val", it.first?.dataSets.toString())
                binding.apply {
                    purchaseLineChart.visibility = View.VISIBLE
                    purchaseLineChart.animateY(1000)
                    progressBarPurchaseChart.visibility = View.GONE
                    purchaseChartDateRange.visibility = View.VISIBLE
                }


            } else if (it.second == Status.FAILED) {

                this.onFailedToLoadData(it.third)

            }

        }

        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY, this.viewLifecycleOwner
        ) { requestKey, bundle ->

//            Log.d("FragmentsLog", "listened")

            this.isDataAdded = bundle.getBoolean(KeysAndMessages.DATA_ADDED_KEY)

            if (isDataAdded) {
                this.viewModel.loadOrdersToReceive()
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

//            setVisibleXRangeMaximum(5f) // only 5 entries will be visible on x-axis

//            animateY(1000)
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