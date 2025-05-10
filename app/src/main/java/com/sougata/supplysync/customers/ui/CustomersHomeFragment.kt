package com.sougata.supplysync.customers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.customers.viewmodels.CustomersHomeViewModel
import com.sougata.supplysync.databinding.FragmentCustomersHomeBinding
import com.sougata.supplysync.sharedviewmodels.CommonDataViewModel
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class CustomersHomeFragment : Fragment() {

    private var _binding: FragmentCustomersHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomersHomeViewModel
    private lateinit var commonDataViewModel: CommonDataViewModel

    private var isDataAdded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_customers_home, container, false)

        this.binding.apply {
            scrollView.visibility = View.GONE
            mainProgressBar.visibility = View.VISIBLE
        }

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[CustomersHomeViewModel::class.java]
        this.commonDataViewModel =
            ViewModelProvider(requireActivity())[CommonDataViewModel::class.java]

        this.binding.sales.value.text = Converters.numberToMoneyString(0.00)

        this.binding.viewModel = this.viewModel

        this.initializeUI()

        this.registerSubscribers()
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
            ordersToDeliver.heading.text = "Orders to Deliver"
            numberOfCustomers.heading.text = "Number of customers"
            sales.heading.text = "Sales"
            receivableFromCustomers.heading.text = "Receivable amount from Customers"
        }
    }

    private fun registerSubscribers() {

        this.viewModel.ordersToDeliver.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {
                this.binding.ordersToDeliver.value.text = it.first.toString()
            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.viewModel.numberOfCustomers.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {
                this.binding.numberOfCustomers.value.text = it.first.toString()
            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.commonDataViewModel.salesAmountByRange.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {
                this.binding.sales.value.text = Converters.numberToMoneyString(it.first)
                this.binding.sales.dateRange.text = this.commonDataViewModel.salesAmountDateRange
            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }
        }

        this.viewModel.receivableAmountFromCustomers.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {
                this.binding.receivableFromCustomers.value.text =
                    Converters.numberToMoneyString(it.first)
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
            KeysAndMessages.RECENT_DATA_CHANGED_KEY, this.viewLifecycleOwner
        ) { requestKey, bundle ->

            this.isDataAdded = bundle.getBoolean(KeysAndMessages.DATA_ADDED_KEY)

            if (isDataAdded) {
                this.viewModel.loadOrdersToDeliver()
                this.viewModel.loadNumberOfCustomers()
                this.viewModel.loadReceivableAmountFromCustomers()
                this.commonDataViewModel.apply {
                    loadOrdersToReceive()
                    loadOrdersToDeliver()
                    loadPast30DaysSalesAmount()
                    loadPast30DaysPurchaseAmount()
                }
            }

        }
    }


}