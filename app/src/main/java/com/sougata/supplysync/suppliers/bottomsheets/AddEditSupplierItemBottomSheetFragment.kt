package com.sougata.supplysync.suppliers.bottomsheets

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.BottomSheetAddEditSupplierItemBinding
import com.sougata.supplysync.firebase.FirestoreRepository
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class AddEditSupplierItemBottomSheetFragment : BottomSheetDialogFragment(), Observable {

    private lateinit var binding: BottomSheetAddEditSupplierItemBinding

    private var supplierItem: SupplierItem? = null

    private lateinit var firestoreRepository: FirestoreRepository

    private var toAdd: Boolean = true

    private val supplierItemAddedIndicator = MutableLiveData<Pair<Int, String>>()

    private val supplierItemEditedIndicator = MutableLiveData<Pair<Int, String>>()

    private var isSupplierItemAdded = false

    @Bindable
    val name = MutableLiveData("")

    @Bindable
    val price = MutableLiveData("")

    @Bindable
    val details = MutableLiveData("")

    companion object {
        @JvmStatic
        fun getInstance(supplierItem: SupplierItem?, toAdd: Boolean) =
            AddEditSupplierItemBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    if (supplierItem != null) {
                        putParcelable("supplierItem", supplierItem)
                    }
                    putBoolean(KeysAndMessages.TO_ADD_KEY, toAdd)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.toAdd = requireArguments().getBoolean(KeysAndMessages.TO_ADD_KEY)

        if (!toAdd) {
            this.supplierItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable("supplierItem", SupplierItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                requireArguments().getParcelable("supplierItem")
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_add_edit_supplier_item,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.firestoreRepository = FirestoreRepository()

        this.binding.bottomSheet = this

        this.registerListeners()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val bundle = Bundle().apply {
            putBoolean(KeysAndMessages.DATA_ADDED_KEY, isSupplierItemAdded)
        }
        this.parentFragmentManager.setFragmentResult(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY,
            bundle
        )

    }

    private fun registerListeners() {

        this.supplierItemAddedIndicator.observe(this.viewLifecycleOwner) {

            if (it.first == Status.STARTED) {

                this.binding.apply {

                    saveBtn.isClickable = false
                    progressBar.visibility = View.VISIBLE
                    parentLayout.alpha = 0.5F
                }

            } else if (it.first == Status.SUCCESS) {

                Snackbar.make(
                    requireParentFragment().requireView(),
                    "Item added successfully",
                    Snackbar.LENGTH_SHORT
                ).show()

                this.isSupplierItemAdded = true

                this.dismiss()

            } else if (it.first == Status.FAILED) {

                this.binding.apply {

                    saveBtn.isClickable = true
                    progressBar.visibility = View.GONE
                    parentLayout.alpha = 1F

                }

                Snackbar.make(
                    requireParentFragment().requireView(), it.second, Snackbar.LENGTH_SHORT
                ).show()

            }

        }

    }

    fun onSaveBtnClick(view: View) {

        if (this.toAdd) {

            val name = this.name.value.orEmpty()
            val priceString = this.price.value.orEmpty()
            val details = this.details.value.orEmpty()

            if (name.isEmpty()) {
                Snackbar.make(requireView(), "Name cannot be empty", Snackbar.LENGTH_SHORT).show()
                return
            }

            var price = 0.0

            if (priceString.isNotEmpty()) {
                try {
                    price = priceString.toDouble()
                } catch (_: Exception) {
                    Snackbar.make(view, "Invalid due amount", Snackbar.LENGTH_SHORT).show()
                    return
                }
            }

            val newSupplierItem = SupplierItem(name, price.toDouble(), details)

            this.supplierItemAddedIndicator.postValue(Status.STARTED to "")

            this.firestoreRepository.addSupplierItem(newSupplierItem) { status, message ->

                this.supplierItemAddedIndicator.postValue(status to message)

            }

        } else {
            // To edit supplier item

        }

    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}

}