package com.sougata.supplysync.modelslist.helper.modelhelpers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ItemUserItemBinding
import com.sougata.supplysync.firestore.CustomerRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.modelslist.helper.ModelHelper
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Status

class UserItemHelper(
    private val fragment: Fragment,
    private val customerRepository: CustomerRepository
) : ModelHelper {

    private val context = this.fragment.requireContext()
    private val fragmentManager = this.fragment.parentFragmentManager

    override val listHeading = "Your items"

    override fun getViewToInflate(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemUserItemBinding {
        return ItemUserItemBinding.inflate(inflater, parent, false)
    }

    override fun getSearchableFieldPairs(): Array<Triple<String, String, FirestoreFieldDataType>> {
        return arrayOf(
            Triple(
                UserItem::name.name, "Name", FirestoreFieldDataType.STRING
            ),
            Triple(
                UserItem::inStock.name, "In stock", FirestoreFieldDataType.BOOLEAN
            ),
            Triple(
                UserItem::price.name, "Price", FirestoreFieldDataType.NUMBER
            )
        )
    }

    override fun getFilterableFields(): Array<Pair<String, (Model) -> Boolean>> {
        return emptyArray()
    }

    override fun getFabClickHandler(): () -> Unit {
        TODO("Not yet implemented")
    }

    override fun bind(
        binding: ViewDataBinding,
        model: Model
    ) {
        binding as ItemUserItemBinding
        model as UserItem

        binding.apply {

            name.text = model.name

            if (model.inStock > 0) {
                inStock.text = "In stock: ${model.inStock}"
                inStock.setTextColor(this@UserItemHelper.context.getColor(R.color.green))
            } else {
                inStock.text = "Out of stock"
                inStock.setTextColor(this@UserItemHelper.context.getColor(R.color.red))
            }

            details.text = model.details
            price.text = Converters.numberToMoneyString(model.price)

            root.setOnClickListener {

            }

        }
    }

    override fun fetchList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.customerRepository.getUserItemsList(
            lastDocumentSnapshot,
            limit,
            onComplete
        )
    }

    override fun fetchListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.customerRepository.getUserItemsListFiltered(
            searchField,
            searchQuery,
            queryDataType,
            lastDocumentSnapshot,
            limit,
            onComplete
        )
    }

    override fun loadFullListOnNewModelAdded(): Boolean {
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun getContentComparator(
        newList: List<Model>,
        oldList: List<Model>,
        newPosition: Int,
        oldPosition: Int
    ): Boolean {
        newList as List<UserItem>
        oldList as List<UserItem>

        return when {
            newList[newPosition].timestamp != oldList[oldPosition].timestamp -> false
            newList[newPosition].name != oldList[oldPosition].name -> false
            newList[newPosition].inStock != oldList[oldPosition].inStock -> false
            newList[newPosition].price != oldList[oldPosition].price -> false
            newList[newPosition].details != oldList[oldPosition].details -> false
            else -> true
        }
    }
}