package com.sougata.supplysync.modelslist

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.BottomSheetModelsListBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class ModelsListBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetModelsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ModelsListViewModel

    private lateinit var recyclerViewAdapter: ModelsListRecyclerViewAdapter

    private lateinit var modelName: String

    private lateinit var onBind: (ViewDataBinding, Model) -> Unit

    private lateinit var helper: ModelsListHelper

    companion object {

        const val MODEL_NAME_KEY = "modelName"

        @JvmStatic
        fun getInstance(modelName: String) =
            ModelsListBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(MODEL_NAME_KEY, modelName)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.modelName = requireArguments().getString(MODEL_NAME_KEY)!!

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(
            inflater, R.layout.bottom_sheet_models_list, container, false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(
            this,
            ModelsListViewModelFactory(this.modelName)
        )[ModelsListViewModel::class.java]

        this.helper = ModelsListHelper(this.modelName, this)

        this.onBind = this.helper.getWhatToDoOnBind()

        this.recyclerViewAdapter =
            ModelsListRecyclerViewAdapter(
                mutableListOf(),
                this.helper
            ) { view, model ->
                view.setOnClickListener {
                    val bundle = Bundle().apply {
                        putParcelable(KeysAndMessages.MODEL_KEY, model as Parcelable)
                    }
                    this.parentFragmentManager.setFragmentResult(
                        KeysAndMessages.ITEM_SELECTED_KEY,
                        bundle
                    )
                    this.dismiss()
                }
            }

        this.binding.recyclerView.apply {

            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = recyclerViewAdapter

        }

        this.registerSubscribers()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

    private fun registerSubscribers() {

        this.viewModel.itemsList.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

                if (viewModel.isFirstTimeListLoaded) {
                    this.binding.progressBar.visibility = View.VISIBLE
                    viewModel.isFirstTimeListLoaded = false
                }

            } else if (it.second == Status.SUCCESS) {

                val list = it.first

                if (list != null) {
                    if (list.isNotEmpty()) {
                        this.binding.progressBar.visibility = View.GONE
                        this.binding.nothingHereLbl.visibility = View.GONE
                        this.recyclerViewAdapter.removeLoadingAnimation()
                    } else {
                        this.binding.nothingHereLbl.visibility = View.VISIBLE
                        this.binding.progressBar.visibility = View.GONE
                        this.recyclerViewAdapter.removeLoadingAnimation()
                    }
                    this.recyclerViewAdapter.setItems(list)
                } else {
                    this.binding.nothingHereLbl.visibility = View.VISIBLE
                    this.binding.progressBar.visibility = View.GONE
                    this.recyclerViewAdapter.removeLoadingAnimation()
                }

            } else if (it.second == Status.FAILED) {

                this.onFailedToLoadData(it.third)

            }

        }

        this.binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!viewModel.noMoreElementLeft) {

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val itemCount = layoutManager.itemCount
                    val lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                    if (lastItemPosition == itemCount - 1) {
                        viewModel.loadItemsList()
                    } else if (lastItemPosition == itemCount - 5) {
                        recyclerViewAdapter.addLoadingAnimation()
                    }
                }

            }
        })
    }


    private fun onFailedToLoadData(message: String) {
        if (message == KeysAndMessages.USER_NOT_FOUND) {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } else {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        }
    }

}