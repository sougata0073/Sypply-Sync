package com.sougata.supplysync.util.modelslist

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentModelsListBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.ViewAnimator

class ModelsListFragment : Fragment() {

    private lateinit var binding: FragmentModelsListBinding

    private lateinit var viewModel: ModelsListViewModel

    private lateinit var recyclerViewAdapter: ModelsListRecyclerViewAdapter

    private lateinit var modelName: String

    private lateinit var onBind: (ViewDataBinding, Model) -> Unit

    private lateinit var helper: ModelsListFragmentHelper

    companion object {
        const val MODEL_NAME_KEY = "modelName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.modelName = requireArguments().getString(MODEL_NAME_KEY)!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_models_list, container, false)

        return this.binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.helper = ModelsListFragmentHelper(this.modelName, this)

        this.viewModel =
            this.helper.getWhichViewModelToCreate()


        this.initializeUI()

        this.registerSubscribers()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bundle = Bundle().apply {
            putBoolean(
                KeysAndMessages.DATA_ADDED_KEY,
                viewModel.isModelAdded || viewModel.isModelUpdated || viewModel.isModelRemoved
            )
        }
        this.parentFragmentManager.setFragmentResult(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY,
            bundle
        )

    }

    private fun initializeUI() {

        this.onBind = this.helper.getWhatToOnBind()

        this.recyclerViewAdapter =
            ModelsListRecyclerViewAdapter(
                mutableListOf(),
                this.onBind,
                this.modelName
            )

        this.binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = recyclerViewAdapter
        }

        this.binding.fab.setOnClickListener {
            this.helper.getWhatToDoOnFabClick().invoke()
        }

    }

    private fun registerSubscribers() {

        this.viewModel.itemsList.observe(this.viewLifecycleOwner) {

            if (it.second == Status.STARTED) {

                this.binding.apply {
                    nothingHereLbl.visibility = View.GONE

                    if (viewModel.isFirstTimeListLoaded) {
                        progressBar.visibility = View.VISIBLE
                        viewModel.isFirstTimeListLoaded = false
                    }

                }

            } else if (it.second == Status.SUCCESS) {

                if (it.third == KeysAndMessages.EMPTY_LIST) {

                    this.binding.apply {
                        if (progressBar.isVisible) {
                            progressBar.visibility = View.GONE
                        }
                        nothingHereLbl.visibility = View.VISIBLE
                    }

                } else {
//                    Log.d("list", "registerSubscribers: ${it.first.toString()}")
                    this.binding.apply {
                        if (progressBar.isVisible) {
                            progressBar.visibility = View.GONE
                        }
                        nothingHereLbl.visibility = View.GONE
                    }
                    this.recyclerViewAdapter.setItems(it.first!!)

//                    Log.d("loader", it.first.toString())
                }

            } else if (it.second == Status.FAILED) {

                this.onFailedToLoadData(it.third)

            }

        }

        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT, this.viewLifecycleOwner
        ) { requestKey, bundle ->

            this.viewModel.isModelAdded = bundle.getBoolean(KeysAndMessages.DATA_ADDED_KEY)

            val removedModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(KeysAndMessages.DATA_REMOVED_KEY, Model::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(KeysAndMessages.DATA_REMOVED_KEY)
            }

            val updatedModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(KeysAndMessages.DATA_UPDATED_KEY, Model::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(KeysAndMessages.DATA_UPDATED_KEY)
            }

            if (this.viewModel.isModelAdded) {
//                Log.d("fragment", "model added")
                this.onModelAdded()
            }

            if (updatedModel != null) {
//                Log.d("fragment", "model updated")
                this.onModelUpdated(updatedModel)
            }

            if (removedModel != null) {
//                Log.d("fragment", "model removed")
//                Log.d("loader", "registerSubscribers: ${removedModel.id}")
                this.onModelDeleted(removedModel)
            }

        }

        this.binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private val fabAnimator = ViewAnimator(binding.fab)

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // When scroll up

                    this.fabAnimator.slideDownFadeForScroll()

                } else if (dy < 0) { // When scroll down

                    this.fabAnimator.slideUpFadeForScroll()

                }

                if (!viewModel.noMoreElementLeft) {

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val itemCount = layoutManager.itemCount
                    val lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                    if (lastItemPosition == itemCount - 1) {
                        viewModel.loadListItem()
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

    private fun onModelAdded() {
        this.viewModel.isModelAdded = true
        this.viewModel.loadLastAddedData()
    }

    private fun onModelUpdated(newModel: Model) {
        this.viewModel.isModelUpdated = true
        this.viewModel.updateModel(newModel)
    }

    private fun onModelDeleted(model: Model) {
        this.viewModel.isModelRemoved = true
        this.viewModel.deleteModel(model)
    }
}