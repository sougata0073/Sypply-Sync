package com.sougata.supplysync.modelslist.ui

import android.content.Intent
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.MainActivity
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentModelsListBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelsListHelper
import com.sougata.supplysync.modelslist.viewmodels.ModelSearchViewModel
import com.sougata.supplysync.modelslist.viewmodels.ModelsListViewModel
import com.sougata.supplysync.modelslist.viewmodels.ModelsListViewModelFactory
import com.sougata.supplysync.util.AnimationProvider
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class ModelsListFragment : Fragment() {

    private var _binding: FragmentModelsListBinding? = null
    private val binding get() = _binding!!

    private val viewModelFactory by lazy { ModelsListViewModelFactory(this.modelName) }

    private val regularViewModel by lazy {
        ViewModelProvider(
            this,
            this.viewModelFactory
        )[ModelsListViewModel::class.java]
    }
    private val searchViewModel by lazy {
        ViewModelProvider(
            this,
            this.viewModelFactory
        )[ModelSearchViewModel::class.java]
    }

    private val recyclerViewExtraCallBack = { view: View, model: Model ->
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
            this.recyclerViewExtraCallBack
        )
    }

    private lateinit var modelName: String
    private var isSelectOnly = false

    private lateinit var helper: ModelsListHelper

    private lateinit var searchViewEditText: EditText

    private var searchField: String? = null
    private var currentQueryDataType: FirestoreFieldDataType? = null

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

        this.binding.chipGroupHorizontalScrollView.visibility = View.GONE

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.helper = ModelsListHelper(this.modelName, this)

        if (this.searchViewModel.isSearchClicked) {
            this.loadChips()
            this.binding.chipGroupHorizontalScrollView.visibility = View.VISIBLE
        }

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

        this.setUpToolBar()

        this.setUpRecyclerView()

        this.setUpFab()

        this.setUpSearchView()
    }

    private fun registerSubscribers() {

        this.binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, _ ->
            if (this.binding.chipGroup.childCount < 2) {
                this.binding.chipGroupHorizontalScrollView.visibility = View.GONE
            }
        })

        this.regularViewModel.itemsList.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

                if (regularViewModel.isFirstTimeListLoaded) {
                    this.binding.progressBar.visibility = View.VISIBLE
                    regularViewModel.isFirstTimeListLoaded = false
                }

            } else if (it.second == Status.SUCCESS) {

                val list = it.first

                if (list != null && list.isNotEmpty()) {
                    onSuccessfulListReceived(list)
                } else {
                    onEmptyOrNullListReceived()
                }

            } else if (it.second == Status.FAILED) {
                this.onFailedToLoadData(it.third)
            }

        }

        this.searchViewModel.itemsList.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

                if (searchViewModel.isFirstTimeListLoaded) {
                    this.binding.progressBar.visibility = View.VISIBLE
                    searchViewModel.isFirstTimeListLoaded = false
                }

            } else if (it.second == Status.SUCCESS) {

                val list = it.first

                if (list != null && list.isNotEmpty()) {
                    onSuccessfulListReceived(list)
                } else {
                    onEmptyOrNullListReceived()
                    this.recyclerViewAdapter.removeAllItems()
                }

            } else if (it.second == Status.FAILED) {
                this.onFailedToLoadData(it.third)
            }
        }

        this.registerFragmentResultListener()
        this.registerScrollListener()

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
                loadChips()
                binding.chipGroupHorizontalScrollView.visibility = View.VISIBLE
                searchViewModel.isSearchClicked = true
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
                binding.chipGroupHorizontalScrollView.visibility = View.GONE

                val list = regularViewModel.itemsList.value?.first
                if (list != null) {
                    recyclerViewAdapter.setItems(list)
                    if (list.isNotEmpty()) {
                        binding.nothingHereLbl.visibility = View.GONE
                    }
                }

                binding.chipGroup.removeViews(1, helper.getSearchableModelFieldPair().size)

                false
            }
        }
    }

    private fun onSuccessfulListReceived(list: List<Model>) {
        this.binding.nothingHereLbl.visibility = View.GONE
        this.binding.progressBar.visibility = View.GONE
        this.recyclerViewAdapter.removeLoadingAnimation()
        this.recyclerViewAdapter.setItems(list)
    }

    private fun onEmptyOrNullListReceived() {
        this.binding.nothingHereLbl.visibility = View.VISIBLE
        this.binding.progressBar.visibility = View.GONE
        this.recyclerViewAdapter.removeLoadingAnimation()
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

    private fun registerScrollListener() {
        this.binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private val fabAnimator = AnimationProvider(binding.fab)

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!isSelectOnly) {
                    if (dy > 0) { // When scroll up

                        this.fabAnimator.slideDownFadeForScroll()

                    } else if (dy < 0) { // When scroll down

                        this.fabAnimator.slideUpFadeForScroll()

                    }
                }
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val itemCount = layoutManager.itemCount
                val lastItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                if (lastItemPosition == itemCount - 1) {

                    if (searchViewModel.isSearchActive) {

                        if (!searchViewModel.noMoreElementLeft) {
                            searchViewModel.loadItemsList(
                                searchViewModel.prevSearchField,
                                searchViewModel.prevSearchQuery,
                                searchViewModel.prevQueryDataType
                            )
                        }

                    } else {

                        if (!regularViewModel.noMoreElementLeft) {
                            regularViewModel.loadItemsList()
                        }

                    }
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

    private fun onFailedToLoadData(message: String) {
        if (message == KeysAndMessages.USER_NOT_FOUND) {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        } else {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
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

    private fun loadChips() {
        for (pair in this.helper.getSearchableModelFieldPair()) {
            val chip = Chip(
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
                text = pair.second.uppercase()
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

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        searchField = pair.first
                        currentQueryDataType = pair.third
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
            }
            this.binding.chipGroup.addView(chip)
        }
    }
}