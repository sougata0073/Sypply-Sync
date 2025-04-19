package com.sougata.supplysync.suppliers.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentSuppliersHomeBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.suppliers.viewmodels.SuppliersHomeViewModel
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class SuppliersHomeFragment : Fragment() {

    private lateinit var binding: FragmentSuppliersHomeBinding

    private lateinit var viewModel: SuppliersHomeViewModel

    private var isDataAdded = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_suppliers_home,
            container,
            false
        )

        this.binding.apply {
            childLayout.visibility = View.GONE
            mainProgressBar.visibility = View.VISIBLE
        }

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[SuppliersHomeViewModel::class.java]

        this.binding.dueToSuppliersAmount.text = Converters.numberToMoneyString(0.00)

        this.binding.viewModel = this.viewModel

        this.registerSubscribers()

//        Log.d("FragmentsLog", "onViewCreated() called")

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
    }


    private fun registerSubscribers() {

        this.viewModel.numberOfSuppliers.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.numberOfSuppliers.text = it.first.toString()

            } else if (it.second == Status.FAILED) {

                failedToLoadData(it.third)

            }

        }

        this.viewModel.dueAmountToSuppliers.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {
                this.binding.dueToSuppliersAmount.text = Converters.numberToMoneyString(it.first)

            } else if (it.second == Status.FAILED) {

                failedToLoadData(it.third)

            }
        }

        this.viewModel.numberOfOrdersToReceive.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

            } else if (it.second == Status.SUCCESS) {

                this.binding.ordersToReceiveNumber.text = it.first.toString()

            } else if (it.second == Status.FAILED) {

                failedToLoadData(it.third)

            }
        }

        this.viewModel.allApiCallFinishedIndicator.observe(this.viewLifecycleOwner) {
            if (it) {
                this.binding.apply {
                    mainProgressBar.visibility = View.GONE
                    childLayout.visibility = View.VISIBLE
                }
            }
        }

        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY, this.viewLifecycleOwner
        ) { requestKey, bundle ->

            this.isDataAdded = bundle.getBoolean(KeysAndMessages.DATA_ADDED_KEY)

            if (isDataAdded) {
                this.viewModel.loadDueAmountToSuppliers()
                this.viewModel.loadNumberOfSuppliers()
                this.viewModel.loadOrdersToReceive()
            }

        }
    }

    private fun failedToLoadData(message: String) {
        if (message == KeysAndMessages.USER_NOT_FOUND) {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } else {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        }
    }
}