package com.sougata.supplysync.util.modelslist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.BottomSheetModelsListBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class ModelsListBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetModelsListBinding

    private lateinit var viewModel: ModelsListViewModel

    private lateinit var recyclerViewAdapter: ModelsListRecyclerViewAdapter

    private lateinit var modelName: String

    private lateinit var onBind: (ViewDataBinding, Model) -> Unit

    private lateinit var helper: ModelsListFragmentHelper

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
        this.binding = DataBindingUtil.inflate(
            inflater, R.layout.bottom_sheet_models_list, container, false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.helper = ModelsListFragmentHelper(this.modelName, this)

        this.viewModel =
            this.helper.getWhichViewModelToCreate()

        this.onBind = this.helper.getWhatToOnBind()

        this.recyclerViewAdapter =
            ModelsListRecyclerViewAdapter(
                mutableListOf(),
                this.onBind,
                this.modelName,
                this
            )

        this.binding.recyclerView.apply {

            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = recyclerViewAdapter

        }

        this.registerSubscribers()

    }

    private fun registerSubscribers() {

        this.viewModel.itemsList.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

                this.binding.apply {
                    nothingHereLbl.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                }

            } else if (it.second == Status.SUCCESS) {

                if (it.third == KeysAndMessages.EMPTY_LIST) {

                    this.binding.apply {
                        progressBar.visibility = View.GONE
                        nothingHereLbl.visibility = View.VISIBLE
                    }

                } else {

                    this.binding.apply {
                        nothingHereLbl.visibility = View.GONE
                        progressBar.visibility = View.GONE
                    }
                    this.recyclerViewAdapter.setData(it.first)
                    this.binding.recyclerView.smoothScrollBy(0, 300)

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
                        viewModel.loadListItem()
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