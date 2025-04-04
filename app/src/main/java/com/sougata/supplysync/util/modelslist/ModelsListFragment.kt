package com.sougata.supplysync.util.modelslist

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentModelsListBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModelsListFragment : Fragment() {

    private lateinit var binding: FragmentModelsListBinding

    private lateinit var viewModel: ModelsListViewModel

    private lateinit var recyclerViewAdapter: ModelsListRecyclerViewAdapter

    private lateinit var modelName: String

    private lateinit var onBind: (ViewDataBinding, Model) -> Unit

    private var isModelAdded = false
    private var isModelUpdated = false
    private var isModelRemoved = false

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

        this.onBind = this.helper.getWhatToOnBind()

        this.recyclerViewAdapter =
            ModelsListRecyclerViewAdapter(
                mutableListOf(),
                this.onBind,
                this.modelName
            )

        this.binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = ScaleInAnimationAdapter(recyclerViewAdapter).apply {
                setFirstOnly(false)
                setDuration(80)
            }
        }

        this.binding.fab.setOnClickListener {
            this.helper.getWhatToDoOnFabClick().invoke()
        }

        this.registerSubscribers()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bundle = Bundle().apply {
            putBoolean(
                KeysAndMessages.DATA_ADDED_KEY,
                isModelAdded || isModelUpdated || isModelRemoved
            )
        }
        this.parentFragmentManager.setFragmentResult(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY,
            bundle
        )

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
                    // Formula to convert dp to px
                    // dp = 200 here
                    val px = (200 * Resources.getSystem().displayMetrics.density).toInt()
                    this.binding.recyclerView.smoothScrollBy(0, px)
                }

            } else if (it.second == Status.FAILED) {

                this.onFailedToLoadData(it.third)

            }

        }

        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY, this.viewLifecycleOwner
        ) { requestKey, bundle ->

            this.isModelAdded = bundle.getBoolean(KeysAndMessages.DATA_ADDED_KEY)

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

            if (this.isModelAdded) {
                this.onModelAdded()
            }

            if (updatedModel != null) {
                this.onModelUpdated(updatedModel)
            }

            if (removedModel != null) {
                this.onModelDeleted(removedModel)
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

    private fun onModelAdded() {
        this.isModelAdded = true
        this.viewModel.loadLastAddedData()
    }

    private fun onModelUpdated(model: Model) {
        this.isModelUpdated = true
        lifecycleScope.launch(Dispatchers.IO) {

            val list = viewModel.itemsList.value?.first ?: mutableListOf()

            for (i in 0 until list.size) {
                if (list[i].id == model.id) {
                    list[i] = model
                    withContext(Dispatchers.Main) {
                        recyclerViewAdapter.updateItem(model, i)
                    }
                    break
                }
            }

        }
    }

    private fun onModelDeleted(model: Model) {
        this.isModelRemoved = true
        this.viewModel.itemsList.value?.first?.remove(model)
        this.recyclerViewAdapter.deleteItem(model)

        if (recyclerViewAdapter.itemCount == 0) {
            this.binding.nothingHereLbl.visibility = View.VISIBLE
        }
    }
}