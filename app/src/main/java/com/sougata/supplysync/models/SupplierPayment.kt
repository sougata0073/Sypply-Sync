package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize


@Parcelize
data class SupplierPayment(
    override var id: String,
    override var timestamp: Timestamp,
    var amount: Double,
    var paymentTimestamp: Timestamp,
    var note: String,
    var supplierId: String,
    var supplierName: String
) : Model, Parcelable {

    constructor() : this("", Timestamp.now(), 0.0, Timestamp.now(), "", "", "")

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "amount" to amount,
            "paymentTimestamp" to paymentTimestamp,
            "note" to note,
            "supplierId" to supplierId,
            "supplierName" to supplierName
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return SupplierPayment(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            amount = map["amount"] as Double,
            paymentTimestamp = map["paymentTimestamp"] as Timestamp,
            note = map["note"] as String,
            supplierId = map["supplierId"] as String,
            supplierName = map["supplierName"] as String
        )
    }
}
