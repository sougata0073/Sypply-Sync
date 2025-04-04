package com.sougata.supplysync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Supplier(
    var name: String,
    var dueAmount: Double,
    var phone: String,
    var email: String,
    var note: String,
    var paymentDetails: String,
    var profileImageUrl: String
) : Model(), Parcelable
