package com.sougata.supplysync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderedItem(
    var itemId: String,
    var itemName: String,
    var quantity: Int,
    var amount: Double,
    var supplierId: String,
    var supplierName: String,
    var year: Int,
    var month: Int,
    var date: Int,
    var isReceived: Boolean
): Model(), Parcelable
