package com.sougata.supplysync.customers.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.customers.viewmodels.CustomerReportsViewModel
import com.sougata.supplysync.databinding.FragmentCustomersReportsBinding
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Status
import java.io.File

class CustomersReportsFragment : Fragment() {

    private var _binding: FragmentCustomersReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomerReportsViewModel

    private val fileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { saveFile(it) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_customers_reports, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[CustomerReportsViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.setUpBarChart(this.binding.receivedOrdersItemsCompChart.barChart)

        this.initializeUI()
        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

    private fun initializeUI() {
        this.setUpReceivedOrdersItemsCompBarChart()
        this.setUpPaymentsReceivedPdfSelection()
        this.setUpSalesPdfSelection()
    }

    private fun registerListeners() {

        this.viewModel.paymentsReceivedListPdf.observe(this.viewLifecycleOwner) {
            this.pdfCreationListener(it, this.binding.paymentsReceived.actionButtonsLayout)
        }

        this.viewModel.salesListPdf.observe(this.viewLifecycleOwner) {
            this.pdfCreationListener(it, this.binding.sales.actionButtonsLayout)
        }

        this.registerReceivedOrdersItemsCompChartListener()
    }

    private fun setUpReceivedOrdersItemsCompBarChart() {
        this.binding.receivedOrdersItemsCompChart.apply {
            heading.text = "Comparison of items ordered by tour customers"
            calendarBtn.setOnClickListener {
                openDateRangePicker { startDateMillis, endDateMillis ->
                    viewModel.loadReceivedOrdersItemsCompChartData(
                        startDateMillis, endDateMillis
                    )
                }
            }
        }
    }

    private fun setUpPaymentsReceivedPdfSelection() {
        this.binding.paymentsReceived.apply {
            this.mainImage.setImageResource(R.drawable.ic_money)
            this.heading.text = "Get a detailed PDF of payments received"
            this.createBtn.setOnClickListener {
                this.actionButtonsLayout.visibility = View.GONE
                openDateRangePicker { startDateMillis, endDateMillis ->
                    viewModel.generatePaymentsReceivedPdf(startDateMillis, endDateMillis)
                }
            }
            this.open.setOnClickListener {
                val byteArray = viewModel.paymentsReceivedListPdf.value?.second

                onOpenClick(byteArray)
            }
            this.saveLocally.setOnClickListener {
                val byteArray =
                    viewModel.paymentsReceivedListPdf.value?.second ?: byteArrayOf()

                onSaveLocallyClick("Payments received.pdf", byteArray)
            }
            this.send.setOnClickListener {
                val byteArray =
                    viewModel.paymentsReceivedListPdf.value?.second ?: byteArrayOf()
                onSendClick("Payments received.pdf", byteArray)
            }
        }
    }

    private fun setUpSalesPdfSelection() {
        this.binding.sales.apply {

            this.mainImage.setImageResource(R.drawable.ic_item)
            this.heading.text = "Get a detailed PDF of items purchased"
            this.createBtn.setOnClickListener {
                this.actionButtonsLayout.visibility = View.GONE
                openDateRangePicker { startDateMillis, endDateMillis ->
                    viewModel.generateSalesListPdf(startDateMillis, endDateMillis)
                }
            }
            this.open.setOnClickListener {
                val byteArray = viewModel.salesListPdf.value?.second
                onOpenClick(byteArray)
            }
            this.saveLocally.setOnClickListener {
                val byteArray = viewModel.salesListPdf.value?.second ?: byteArrayOf()
                onSaveLocallyClick("Items sold.pdf", byteArray)
            }
            this.send.setOnClickListener {
                val byteArray = viewModel.salesListPdf.value?.second ?: byteArrayOf()
                onSendClick("Items sold.pdf", byteArray)
            }
        }
    }

    private fun registerReceivedOrdersItemsCompChartListener() {
        this.viewModel.receivedOrdersItemsCompChartData.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

                this.binding.receivedOrdersItemsCompChart.progressBar.visibility = View.VISIBLE

            } else if (it.second == Status.SUCCESS) {

                this.binding.receivedOrdersItemsCompChart.barChart.apply {

                    this.data = it.first

                    this.xAxis.apply {
                        this.valueFormatter =
                            IndexAxisValueFormatter(viewModel.receivedOrdersItemsCompChartXStrings)

                        labelCount = 8
                        this.labelRotationAngle = 90f
                        this.granularity = 1f
                        this.isGranularityEnabled = true
                    }

                    this.setVisibleXRangeMaximum(8f)

                    this.notifyDataSetChanged()

                    this.visibility = View.VISIBLE

                    if (viewModel.animateReceivedOrdersItemsCompChart) {
                        animateY(1000)
                        viewModel.animateReceivedOrdersItemsCompChart = false
                    }
                }

                this.binding.receivedOrdersItemsCompChart.progressBar.visibility = View.GONE
                this.binding.receivedOrdersItemsCompChart.dateRange.text =
                    this.viewModel.receivedOrdersItemsCompChartDateRange


            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
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

    private fun onOpenClick(byteArray: ByteArray?) {
        val bundle = Bundle().apply {
            putByteArray(Keys.BYTE_ARRAY, byteArray)
        }

        this.findNavController()
            .navigate(
                R.id.pdfViewerFragment,
                bundle,
                AnimationProvider.popUpNavOptions()
            )
    }

    private fun onSaveLocallyClick(fileName: String, byteArray: ByteArray) {

        val intent = Intent().apply {
            action = Intent.ACTION_CREATE_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        this.viewModel.pdfByteArray = byteArray
        this.fileLauncher.launch(intent)
    }

    private fun onSendClick(fileName: String, byteArray: ByteArray) {
        val pdfFile = File(requireContext().cacheDir, fileName)

        // use{} block auto closes the resources
        pdfFile.outputStream().use {
            it.write(byteArray)
        }

        val pdfFileUri =
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                pdfFile
            )

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, pdfFileUri)
            type = "application/pdf"
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun saveFile(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val filePathUri = result.data?.data
            if (filePathUri != null) {
                try {
                    val outputStream =
                        this.requireContext().contentResolver.openOutputStream(filePathUri)

                    if (outputStream != null) {
                        outputStream.write(this.viewModel.pdfByteArray)
                        outputStream.close()

                        Snackbar.make(
                            requireView(),
                            "File saved successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        Snackbar.make(requireView(), "Failed to save file", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                } catch (_: Exception) {
                    Snackbar.make(requireView(), "Failed to save file", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun pdfCreationListener(value: Triple<Status, ByteArray?, String>, view: View) {
        if (value.first == Status.STARTED) {
            this.onPdfCreationStarted()
        } else if (value.first == Status.SUCCESS) {
            onPdfCreationFinished(view)
        } else if (value.first == Status.FAILED) {
            onPdfCreationFailed(value.third)
        }
    }

    private fun onPdfCreationStarted() {
        this.binding.apply {
            touchBlocker.visibility = View.VISIBLE
            root.alpha = 0.7f
            mainProgressBar.visibility = View.VISIBLE
        }
    }

    private fun onPdfCreationFinished(view: View) {
        this.binding.apply {
            root.alpha = 1f
            mainProgressBar.visibility = View.GONE
            view.visibility = View.VISIBLE
            touchBlocker.visibility = View.GONE
        }
    }

    private fun onPdfCreationFailed(message: String) {
        this.binding.apply {
            root.alpha = 1f
            mainProgressBar.visibility = View.GONE
            touchBlocker.visibility = View.GONE
        }
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }
}