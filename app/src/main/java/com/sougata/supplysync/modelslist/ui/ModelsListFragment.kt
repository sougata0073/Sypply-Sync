package com.sougata.supplysync.modelslist.ui

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentModelsListBinding
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import com.sougata.supplysync.modelslist.viewmodels.ModelsListViewModel
import com.sougata.supplysync.modelslist.viewmodels.ModelsListViewModelFactory
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class ModelsListFragment : Fragment() {

    private var _binding: FragmentModelsListBinding? = null
    private val binding get() = _binding!!

    private val viewModelFactory by lazy { ModelsListViewModelFactory(this.helper) }

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            this.viewModelFactory
        )[ModelsListViewModel::class.java]
    }

    private val recyclerViewCallBack = { view: View, model: Model ->
        if (this.isSelectOnly) {
            view.setOnClickListener {
                val bundle = Bundle().apply {
                    putParcelable(KeysAndMessages.MODEL_KEY, model as Parcelable)
                }
                this.parentFragmentManager.setFragmentResult(
                    KeysAndMessages.ITEM_SELECTED_KEY,
                    bundle
                )
                this.findNavController().popBackStack()
            }
        }
    }

    private val recyclerViewAdapter by lazy {
        ModelsListRecyclerViewAdapter(
            mutableListOf(),
            this.helper,
            this.loadListAgain,
            this.recyclerViewCallBack
        )
    }

    private lateinit var modelName: String
    private var isSelectOnly = false

    private lateinit var helper: ModelsListHelper

    private lateinit var searchViewEditText: EditText

    private var searchField: String? = null
    private var currentQueryDataType: FirestoreFieldDataType? = null

    private val loadListAgain = MutableLiveData(false)

    private lateinit var fieldsListSearch: Array<Triple<String, String, FirestoreFieldDataType>>
    private lateinit var fieldsListFilter: Array<Pair<String, (Model) -> Boolean>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.modelName = requireArguments().getString(KeysAndMessages.MODEL_NAME_KEY)!!
        this.isSelectOnly = requireArguments().getBoolean(KeysAndMessages.IS_SELECT_ONLY_KEY)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_models_list, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.helper = ModelsListHelper(this.modelName, this)

        this.fieldsListSearch = this.helper.getSearchableFieldPairs()
        this.fieldsListFilter = this.helper.getFilterableFields()

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

        this._binding = null
    }


    private fun initializeUI() {

        if (this.viewModel.isSearchClicked) {
            this.loadChipsSearch()
        }

        this.loadChipsFilter()

        this.setUpToolBar()

        this.setUpRecyclerView()

        this.setUpFab()

        this.setUpSearchView()
    }

    private fun registerSubscribers() {

        this.registerListListener()
        this.registerFragmentResultListener()

        this.loadListAgain.observe(this.viewLifecycleOwner) {
            if (it) {
                this.loadItemsList()
            }
        }
    }

    private fun registerListListener() {
        this.viewModel.itemsList.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

                if (this.viewModel.isFirstTimeListLoad) {
                    this.binding.progressBar.visibility = View.VISIBLE
                    this.viewModel.isFirstTimeListLoad = false
                }

            } else if (it.second == Status.SUCCESS) {
                this.binding.progressBar.visibility = View.GONE

                Log.d("TAG3", this.viewModel.isModelRemoved.toString())

                var s = ""
                for (item in it.first) {
                    try {
                        item as Supplier
                        s += item.name + "  "
                    } catch (_: Exception) {
                        Log.d("TAG", item.toString())
                    }
                }
//                Log.d("list", s)

                this.recyclerViewAdapter.setItems(it.first)

                if (recyclerViewAdapter.itemCount == 0) {
                    this.binding.nothingHereLbl.visibility = View.VISIBLE
                } else {
                    this.binding.nothingHereLbl.visibility = View.GONE
                }

            } else if (it.second == Status.FAILED) {
                Snackbar.make(
                    requireView(),
                    KeysAndMessages.SOMETHING_WENT_WRONG,
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun setUpToolBar() {
        this.binding.toolBar.apply {
            title = helper.getHeading()
            setNavigationOnClickListener {
                this@ModelsListFragment.findNavController().popBackStack()
            }
        }
    }

    private fun setUpRecyclerView() {
        this.binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = recyclerViewAdapter
        }

        this.binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private val fabAnimator = AnimationProvider(binding.fab)
            private val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!isSelectOnly) {
                    if (dy > 0) { // When scroll up
                        this.fabAnimator.slideDownFadeForScroll()
                    } else if (dy < 0) { // When scroll down
                        this.fabAnimator.slideUpFadeForScroll()
                    }
                }

                val itemCount = layoutManager.itemCount
                val lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                if (lastItemPosition == itemCount - 1) {
                    loadItemsList()
                }
                if (lastItemPosition == itemCount - 5) {
                    if (!viewModel.noMoreItem) {
                        recyclerViewAdapter.addLoadingAnimation()
                    }
                }
            }

        })
    }

    private fun setUpFab() {
        if (this.isSelectOnly) {
            this.binding.fab.visibility = View.GONE
        } else {
            this.binding.fab.setOnClickListener {
                this.helper.getWhatToDoOnFabClick().invoke()
            }
        }
    }

    private fun setUpSearchView() {
        this.binding.searchView.apply {

            searchViewEditText = findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchViewEditText.setTextColor(requireContext().getColor(R.color.bw))

            setOnSearchClickListener {
                activateSearchClick()
            }

            setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        val field = searchField
                        val dataType = currentQueryDataType
                        if (field != null && dataType != null) {
                            viewModel.loadItemsList(Triple(field, query.orEmpty(), dataType))
                        } else {
                            Snackbar.make(
                                requireView(),
                                "Select a field to search",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (!newText.isNullOrEmpty()) {
                            activateSearch()
                        }
                        return false
                    }
                }
            )

            setOnCloseListener {
                searchField = null
                currentQueryDataType = null

                closeSearch()

                false
            }
        }
    }

    private fun registerFragmentResultListener() {
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
                this.onModelAdded()
            }

            if (updatedModel != null) {
                this.onModelUpdated(updatedModel)
            }

            if (removedModel != null) {
                this.onModelDeleted(removedModel)
            }

        }
    }

    private fun loadItemsList() {
        if (this.viewModel.isSearchActive) {
            if (!this.viewModel.noMoreItem) {
                this.viewModel.loadItemsList(
                    Triple(
                        this.viewModel.prevSearchField,
                        this.viewModel.prevSearchQuery,
                        this.viewModel.prevQueryDataType
                    )
                )
            }
        } else {
            if (!this.viewModel.noMoreItem) {
                this.viewModel.loadItemsList()
            }
        }
    }

    private fun onModelAdded() {
        this.viewModel.isModelAdded = true
        this.viewModel.loadLastAddedModel()
    }

    private fun onModelUpdated(newModel: Model) {
        this.viewModel.isModelUpdated = true
        this.viewModel.updateModel(newModel)
    }

    private fun onModelDeleted(model: Model) {
        this.viewModel.isModelRemoved = true
        viewModel.deleteModel(model)
    }

    private fun loadChipsSearch() {

        if (this.fieldsListSearch.isEmpty()) {
            return
        }

        this.binding.chipGroup.addView(this.getDecoratedMarkerChip("Search by"))

        for (field in this.helper.getSearchableFieldPairs()) {
            val chip = this.getDecoratedChip(field.second)

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    searchField = field.first
                    currentQueryDataType = field.third
                    searchViewEditText.text.clear()

                    if (currentQueryDataType == FirestoreFieldDataType.NUMBER) {
                        searchViewEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    } else {
                        searchViewEditText.inputType = InputType.TYPE_CLASS_TEXT
                    }
                } else {
                    searchField = null
                    currentQueryDataType = null
                }
            }
            this.binding.chipGroup.addView(chip)
        }
    }

    private fun loadChipsFilter() {

        if (this.fieldsListFilter.isEmpty()) {
            return
        }

        this.binding.chipGroup.addView(this.getDecoratedMarkerChip("Filters"))

        for (field in this.helper.getFilterableFields()) {
            val chip = this.getDecoratedChip(field.first)

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.activateFilter { field.second(it) }
                } else {
                    viewModel.closeFilter()
                }
            }
            this.binding.chipGroup.addView(chip)
        }
    }

    private fun removeAllChips() {
        this.binding.chipGroup.removeAllViews()
    }

    private fun activateSearchClick() {
        this.viewModel.activateSearchClick()
        this.removeAllChips()
        this.loadChipsSearch()
    }

    private fun activateSearch() {
        this.viewModel.activateSearch()
    }

    private fun closeSearch() {
        this.viewModel.closeSearch()
        this.removeAllChips()
        this.loadChipsFilter()
    }

    private fun getDecoratedMarkerChip(chipName: String): Chip {
        return Chip(requireContext()).apply {
            isCheckable = false
            isClickable = false
            isFocusable = false
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = chipName
            chipStrokeWidth = 0f
            chipBackgroundColor =
                requireContext().getColorStateList(R.color.night_mode_background)
            setTextColor(requireContext().getColor(R.color.bw))
        }
    }

    private fun getDecoratedChip(chipName: String): Chip {
        return Chip(
            requireContext(),
            null,
            com.google.android.material.R.style.Widget_Material3_Chip_Filter
        ).apply {
            isCheckable = true
            isClickable = true
            isFocusable = true
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = chipName
            chipStrokeWidth = 0f
            chipBackgroundColor =
                requireContext().getColorStateList(R.color.active_non_active_button_color)
            shapeAppearanceModel =
                shapeAppearanceModel.toBuilder().setAllCornerSizes(16f).build()
            checkedIcon = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_chip_check
            )
            setTextColor(requireContext().getColor(R.color.bw))
        }
    }
}