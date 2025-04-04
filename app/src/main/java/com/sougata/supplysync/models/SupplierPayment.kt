package com.sougata.supplysync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class SupplierPayment(
    var amount: Double,
    var year: Int,
    var month: Int,
    var date: Int,
    var hour: Int,
    var minute: Int,
    var note: String,
    var supplierId: String,
    var supplierName: String
): Model(), Parcelable
