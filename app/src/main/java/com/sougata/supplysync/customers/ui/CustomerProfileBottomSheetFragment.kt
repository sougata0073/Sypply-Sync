package com.sougata.supplysync.customers.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.BottomSheetCustomerProfileBinding
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages

class CustomerProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomerProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var customer: Customer

    companion object {
        @JvmStatic
        fun getInstance(customer: Customer) =
            CustomerProfileBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Model.CUSTOMER, customer)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.customer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(Model.CUSTOMER, Customer::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(Model.CUSTOMER)
        }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(
            inflater, R.layout.bottom_sheet_customer_profile, container, false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.customer = this.customer

        this.initializeUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

    private fun initializeUI() {
        Glide.with(requireView())
            .load(this.customer.profileImageUrl)
            .placeholder(R.drawable.ic_user_profile)
            .error(R.drawable.ic_user_profile)
            .into(this.binding.profileImage)

        this.binding.receivableAmount.text =
            Converters.numberToMoneyString(this.customer.receivableAmount)
        this.binding.dueOrders.text = this.customer.dueOrders.toString()

        this.binding.editBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_EDIT_KEY, true)
                putParcelable(Model.CUSTOMER, customer)
            }

            this.findNavController().navigate(
                R.id.addEditCustomerFragment,
                bundle, AnimationProvider.slideRightLeftNavOptions()
            )

            this.dismiss()
        }
    }

}