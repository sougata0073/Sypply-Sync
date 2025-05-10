package com.sougata.supplysync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    var name: String,
    var receivableAmount: Double,
    var dueOrders: Int,
    var phone: String,
    var email: String,
    var note: String,
    var profileImageUrl: String
): Model(), Parcelable
