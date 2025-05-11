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
import androidx.core.view.isVisible
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
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import com.sougata.supplysync.modelslist.viewmodels.ModelSearchViewModel
import com.sougata.supplysync.modelslist.viewmodels.ModelsListRegularViewModel
import com.sougata.supplysync.modelslist.viewmodels.ModelsListViewModelFactory
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class ModelsListFragment : Fragment() {

    private var _binding: FragmentModelsListBinding? = null
    private val binding get() = _binding!!

    private val viewModelFactory by lazy { ModelsListViewModelFactory(this.helper) }

    private val regularViewModel by lazy {
        ViewModelProvider(
            this,
            this.viewModelFactory
        )[ModelsListRegularViewModel::class.java]
    }
    private val searchViewModel by lazy {
        ViewModelProvider(
            this,
            this.viewModelFactory
        )[ModelSearchViewModel::class.java]
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
            { list ->
                if (list.isEmpty()) {
                    this.binding.nothingHereLbl.visibility = View.VISIBLE
                } else {
                    this.binding.nothingHereLbl.visibility = View.GONE
                }
            },
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

//        this.fieldsListSearch = this.helper.getSearchableFieldPairs()
//        this.fieldsListFilter = this.helper.getFilterableFields()

        this.initializeUI()

        this.registerSubscribers()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val bundle = Bundle().apply {
            putBoolean(
                KeysAndMessages.DATA_ADDED_KEY,
                regularViewModel.isModelAdded || regularViewModel.isModelUpdated || regularViewModel.isModelRemoved
            )
        }
        this.parentFragmentManager.setFragmentResult(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY,
            bundle
        )

        this._binding = null
    }


    private fun initializeUI() {

        this.loadChipsFilter()

        this.loadChipsSearch()

        this.binding.searchChipsLayout.visibility = if (searchViewModel.isSearchClicked) {
            View.VISIBLE
        } else {
            View.GONE
        }

        this.binding.filterChipsLayout.visibility = if (this.helper.getFilterableFields().isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        this.setUpToolBar()

        this.setUpRecyclerView()

        this.setUpFab()

        this.setUpSearchView()
    }

    private fun registerSubscribers() {

        this.registerAppBarOffSetListener()
        this.registerRegularViewModelListListener()
        this.registerSearchViewModelListListener()
        this.registerFragmentResultListener()

        this.loadListAgain.observe(this.viewLifecycleOwner) {
            if (it) {
                this.loadItemsList()
            }
        }

    }

    private fun registerAppBarOffSetListener() {
        this.binding.appBarLayout.addOnOffsetChangedListener { _, _ ->

            if (this.searchViewModel.isSearchClicked) {
                if (!this.binding.searchChipsLayout.isVisible) {
                    this.binding.searchChipsLayout.visibility = View.VISIBLE
                }
            } else {
                if (this.binding.searchChipsLayout.isVisible) {
                    this.binding.searchChipsLayout.visibility = View.GONE
                }
            }

            if (this.helper.getFilterableFields().isNotEmpty()) {
                if (!this.binding.filterChipsLayout.isVisible) {
                    this.binding.filterChipsLayout.visibility = View.VISIBLE
                }
            } else {
                if (this.binding.filterChipsLayout.isVisible) {
                    this.binding.filterChipsLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun registerRegularViewModelListListener() {
        this.regularViewModel.itemsList.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

                if (regularViewModel.isFirstTimeListLoaded) {
                    this.binding.progressBar.visibility = View.VISIBLE
                    regularViewModel.isFirstTimeListLoaded = false
                }

            } else if (it.second == Status.SUCCESS) {
                val list = it.first
                if (list != null) {
                    onSuccessfulListReceived(list)
                } else {
                    onEmptyOrNullListReceived()
                }

            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun registerSearchViewModelListListener() {
        this.searchViewModel.itemsList.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

                if (searchViewModel.isFirstTimeListLoaded) {
                    this.binding.progressBar.visibility = View.VISIBLE
                    searchViewModel.isFirstTimeListLoaded = false
                }

            } else if (it.second == Status.SUCCESS) {

                val list = it.first

                if (list != null) {
                    onSuccessfulListReceived(list)
                } else {
                    onEmptyOrNullListReceived()
                    this.recyclerViewAdapter.removeAllItems()
                }

            } else if (it.second == Status.FAILED) {
                Snackbar.make(requireView(), it.third, Snackbar.LENGTH_SHORT).show()
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
                    Log.d("TAG", "called")
                    loadItemsList()

                } else if (lastItemPosition == itemCount - 5) {
                    if (searchViewModel.isSearchActive) {
                        if (!searchViewModel.noMoreElementLeft) {
                            recyclerViewAdapter.addLoadingAnimation()
                        }
                    } else {
                        if (!regularViewModel.noMoreElementLeft) {
                            recyclerViewAdapter.addLoadingAnimation()
                        }
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
                searchViewModel.isSearchClicked = true
                binding.searchChipsLayout.visibility = View.VISIBLE
            }

            setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        val field = searchField
                        val dataType = currentQueryDataType
                        if (field != null && dataType != null) {
                            searchViewModel.loadItemsList(field, query.orEmpty(), dataType)
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
                            searchViewModel.isSearchActive = true
                        }
                        return false
                    }
                }
            )

            setOnCloseListener {
                searchViewModel.isSearchActive = false
                searchViewModel.isSearchClicked = false
                searchField = null
                currentQueryDataType = null

                binding.searchChipsLayout.visibility = View.GONE
                if (helper.getFilterableFields().isNotEmpty()) {
                    binding.filterChipsLayout.visibility = View.VISIBLE
                }

                val list = regularViewModel.itemsList.value?.first
                if (list != null) {
                    recyclerViewAdapter.setItems(list)
                }

                false
            }
        }
    }

    private fun onSuccessfulListReceived(list: MutableList<Model>) {
        this.binding.progressBar.visibility = View.GONE
        this.recyclerViewAdapter.setItems(list)

    }

    private fun onEmptyOrNullListReceived() {
        this.binding.progressBar.visibility = View.GONE
    }

    private fun registerFragmentResultListener() {
        this.parentFragmentManager.setFragmentResultListener(
            KeysAndMessages.RECENT_DATA_CHANGED_KEY_ADD_EDIT, this.viewLifecycleOwner
        ) { requestKey, bundle ->

            this.regularViewModel.isModelAdded = bundle.getBoolean(KeysAndMessages.DATA_ADDED_KEY)

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

            if (this.regularViewModel.isModelAdded) {
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
        if (this.searchViewModel.isSearchActive) {
            if (!this.searchViewModel.noMoreElementLeft) {
                this.searchViewModel.loadItemsList(
                    this.searchViewModel.prevSearchField,
                    this.searchViewModel.prevSearchQuery,
                    this.searchViewModel.prevQueryDataType
                )
            }
        } else {
            if (!this.regularViewModel.noMoreElementLeft) {
                this.regularViewModel.loadItemsList()
            }
        }
    }

    private fun onModelAdded() {
        this.regularViewModel.isModelAdded = true
        this.regularViewModel.loadLastAddedData()
        if (this.searchViewModel.isSearchActive) {
            this.searchViewModel.loadLastAddedData()
        }
    }

    private fun onModelUpdated(newModel: Model) {
        this.regularViewModel.isModelUpdated = true
        this.regularViewModel.updateModel(newModel)
        if (this.searchViewModel.isSearchActive) {
            this.searchViewModel.updateModel(newModel)
        }
    }

    private fun onModelDeleted(model: Model) {
        this.regularViewModel.isModelRemoved = true
        this.regularViewModel.deleteModel(model)
        if (this.searchViewModel.isSearchActive) {
            this.searchViewModel.deleteModel(model)
        }
    }

    private fun loadChipsSearch() {
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
            this.binding.chipGroupSearch.addView(chip)
        }
    }

    private fun loadChipsFilter() {

        this.binding.filterChipsLayout.visibility = if (this.helper.getFilterableFields().isEmpty()) {
            View.GONE
            return
        } else {
            View.VISIBLE
        }

        for (field in this.helper.getFilterableFields()) {
            val chip = this.getDecoratedChip(field.first)

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    recyclerViewAdapter.filterList { model -> field.second(model) }
                } else {
                    recyclerViewAdapter.clearFilter()
                }
            }
            this.binding.chipGroupFilter.addView(chip)
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