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
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.BottomSheetSupplierProfileBinding
import com.sougata.supplysync.firebase.FirestoreRepository
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.Inputs
import com.sougata.supplysync.util.Status

class SupplierProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetSupplierProfileBinding

    private lateinit var supplier: Supplier

    private lateinit var firestoreRepository: FirestoreRepository

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
        this.binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_supplier_profile,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.firestoreRepository = FirestoreRepository()

        this.initializeUI()

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

        this.binding.deleteBtn.setOnClickListener {

            this.binding.parentLayout.alpha = 0.5f
            this.binding.progressBar.visibility = View.VISIBLE

            this.firestoreRepository.deleteSupplier(this.supplier) { status, message ->

                Snackbar.make(
                    requireParentFragment().requireView(),
                    message,
                    Snackbar.LENGTH_SHORT
                ).show()

                if (status == Status.SUCCESS) {

                    val bundle = Bundle().apply {
                        putParcelable(KeysAndMessages.DATA_REMOVED_KEY, supplier)
                    }

                    this.parentFragmentManager.setFragmentResult(
                        KeysAndMessages.RECENT_DATA_CHANGED_KEY,
                        bundle
                    )

                    this.dismiss()
                } else if (status == Status.FAILED) {
                    this.binding.parentLayout.alpha = 1f
                    this.binding.progressBar.visibility = View.GONE
                }

            }

        }

        this.binding.supplier = this.supplier
    }

}