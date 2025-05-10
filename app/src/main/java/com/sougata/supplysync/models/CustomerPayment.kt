package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerPayment(
    var amount: Double,
    var paymentTimestamp: Timestamp,
    var note: String,
    var customerId: String,
    var customerName: String
): Model(), Parcelable
