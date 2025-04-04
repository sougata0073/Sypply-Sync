package com.sougata.supplysync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SupplierItem(
    var name: String,
    var price: Double,
    var details: String
): Model(), Parcelable
