package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    var userItemId: String,
    var userItemName: String,
    var quantity: Int,
    var amount: Double,
    var customerId: String,
    var customerName: String,
    var deliveryTimestamp: Timestamp,
    var isDelivered: Boolean
): Model(), Parcelable
