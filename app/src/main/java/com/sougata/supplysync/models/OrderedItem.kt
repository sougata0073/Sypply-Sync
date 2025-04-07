package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderedItem(
    var itemId: String,
    var itemName: String,
    var quantity: Int,
    var amount: Double,
    var supplierId: String,
    var supplierName: String,
    var ordereTimestamp: Timestamp,
    var isReceived: Boolean
): Model(), Parcelable
