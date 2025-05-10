package com.sougata.supplysync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem(
    var name: String,
    var inStock: Int,
    var price: Double,
    var details: String
): Model(), Parcelable
