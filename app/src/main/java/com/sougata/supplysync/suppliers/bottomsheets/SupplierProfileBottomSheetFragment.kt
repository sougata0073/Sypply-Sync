package com.sougata.supplysync.suppliers.bottomsheets

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
import com.sougata.supplysync.databinding.BottomSheetSupplierProfileBinding
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Inputs

class SupplierProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSupplierProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var supplier: Supplier

    companion object {
        @JvmStatic
        fun getInstance(supplier: Supplier) =
            SupplierProfileBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("supplier", supplier)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supplier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("supplier", Supplier::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("supplier")
        }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_supplier_profile,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.supplier = this.supplier

        this.initializeUI()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

    private fun initializeUI() {

        Glide.with(requireView())
            .load(this.supplier.profileImageUrl)
            .placeholder(R.drawable.ic_user_profile)
            .error(R.drawable.ic_user_profile)
            .into(this.binding.profileImage)

        this.binding.dueAmount.text = Converters.numberToMoneyString(this.supplier.dueAmount)

        this.binding.editBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putBoolean(KeysAndMessages.TO_EDIT_KEY, true)
                putParcelable("supplier", supplier)
            }

            findNavController().navigate(
                R.id.addEditSupplierFragment,
                bundle, Inputs.getFragmentAnimations()
            )

            this.dismiss()
        }

    }

}