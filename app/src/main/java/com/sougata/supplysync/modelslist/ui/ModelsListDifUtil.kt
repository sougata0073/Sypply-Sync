package com.sougata.supplysync.modelslist.ui

import androidx.recyclerview.widget.DiffUtil
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.modelslist.helper.ModelsListHelper

class ModelsListDifUtil(
    private val oldList: List<Model>,
    private val newList: List<Model>,
    private val helper: ModelsListHelper
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return this.oldList.size
    }

    override fun getNewListSize(): Int {
        return this.newList.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return this.oldList[oldItemPosition].id == this.newList[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {

        val isSame = this.helper.getContentComparator()(
            this.oldList,
            this.newList,
            oldItemPosition,
            newItemPosition
        )

        return isSame
    }
}