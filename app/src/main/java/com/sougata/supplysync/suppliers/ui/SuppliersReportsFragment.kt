package com.sougata.supplysync.suppliers.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
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
import com.sougata.supplysync.databinding.FragmentSuppliersReportsBinding
import com.sougata.supplysync.suppliers.viewmodels.SupplierReportsViewModel
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import java.io.File

class SuppliersReportsFragment : Fragment() {

    private var _binding: FragmentSuppliersReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SupplierReportsViewModel

    private val fileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { saveFile(it) }

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

        this.setUpBarChart(this.binding.purchasedItemsCompChart.barChart)

        this.initializeUI()

        this.registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

    private fun initializeUI() {

        this.binding.supplierPayment.apply {

            this.mainImage.setImageResource(R.drawable.ic_money)
            this.heading.text = "Payments to suppliers"
            this.createBtn.setOnClickListener {
                this.actionButtonsLayout.visibility = View.GONE
                openDateRangePicker { startDateMillis, endDateMillis ->
                    viewModel.generateSupplierPaymentsPdf(startDateMillis, endDateMillis)
                }
            }
            this.open.setOnClickListener {
                val byteArray = viewModel.supplierPaymentsListPdf.value?.second

                onOpenClick(byteArray)
            }
            this.saveLocally.setOnClickListener {
                val byteArray =
                    viewModel.supplierPaymentsListPdf.value?.second ?: byteArrayOf()

                onSaveLocallyClick("Payments to suppliers.pdf", byteArray)
            }
            this.send.setOnClickListener {
                val byteArray =
                    viewModel.supplierPaymentsListPdf.value?.second ?: byteArrayOf()
                onSendClick("Payments to suppliers.pdf", byteArray)
            }
        }
        this.binding.purchasedItem.apply {

            this.mainImage.setImageResource(R.drawable.ic_item)
            this.heading.text = "Items purchased"
            this.createBtn.setOnClickListener {
                this.actionButtonsLayout.visibility = View.GONE
                openDateRangePicker { startDateMillis, endDateMillis ->
                    viewModel.generateOrderedItemsPdf(startDateMillis, endDateMillis)
                }
            }
            this.open.setOnClickListener {
                val byteArray = viewModel.orderedItemsListPdf.value?.second
                onOpenClick(byteArray)
            }
            this.saveLocally.setOnClickListener {
                val byteArray = viewModel.orderedItemsListPdf.value?.second ?: byteArrayOf()
                onSaveLocallyClick("Items purchased.pdf", byteArray)
            }
            this.send.setOnClickListener {
                val byteArray = viewModel.orderedItemsListPdf.value?.second ?: byteArrayOf()
                onSendClick("Items purchased.pdf", byteArray)
            }
        }

        this.binding.purchasedItemsCompChart.calendarBtn.setOnClickListener {
            this.openDateRangePicker { startDateMillis, endDateMillis ->
                this.viewModel.loadPurchasedItemsCompChartData(
                    startDateMillis, endDateMillis
                )
            }
        }
    }

    private fun registerListeners() {

        this.viewModel.supplierPaymentsListPdf.observe(this.viewLifecycleOwner) {
            pdfCreationListener(it, this.binding.supplierPayment.actionButtonsLayout)
        }

        this.viewModel.orderedItemsListPdf.observe(this.viewLifecycleOwner) {
            pdfCreationListener(it, this.binding.purchasedItem.actionButtonsLayout)
        }

        this.viewModel.purchasedItemsCompChartData.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

                this.binding.purchasedItemsCompChart.progressBar.visibility = View.VISIBLE

            } else if (it.second == Status.SUCCESS) {

                binding.purchasedItemsCompChart.barChart.apply {

                    this.data = it.first

                    this.xAxis.apply {
                        this.valueFormatter =
                            IndexAxisValueFormatter(viewModel.purchasedItemsCompChartXStrings)
                        this.labelRotationAngle = 270f
                        this.granularity = 1f
                        this.isGranularityEnabled = true
                    }

                    this.setVisibleXRangeMaximum(8f)

                    this.notifyDataSetChanged()

                    this.visibility = View.VISIBLE

                    if (viewModel.animatePurchasedItemsCompChart) {
                        animateY(1000)
                        viewModel.animatePurchasedItemsCompChart = false
                    }
                }

                this.binding.purchasedItemsCompChart.progressBar.visibility = View.GONE
                this.binding.purchasedItemsCompChart.dateRange.text =
                    this.viewModel.purchasedItemsCompChartDateRange


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
            putByteArray(KeysAndMessages.BYTE_ARRAY_KEY, byteArray)
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


