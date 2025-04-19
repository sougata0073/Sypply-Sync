package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize


@Parcelize
data class SupplierPayment(
    var amount: Double,
    var paymentTimestamp: Timestamp,
    var note: String,
    var supplierId: String,
    var supplierName: String
): Model(), Parcelable
