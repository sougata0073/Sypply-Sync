package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerPayment(
    override var id: String,
    override var timestamp: Timestamp,
    var amount: Double,
    var paymentTimestamp: Timestamp,
    var note: String,
    var customerId: String,
    var customerName: String
): Model, Parcelable {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "amount" to amount,
            "paymentTimestamp" to paymentTimestamp,
            "note" to note,
            "customerId" to customerId,
            "customerName" to customerName
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return CustomerPayment(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            amount = map["amount"] as Double,
            paymentTimestamp = map["paymentTimestamp"] as Timestamp,
            note = map["note"] as String,
            customerId = map["customerId"] as String,
            customerName = map["customerName"] as String
        )
    }
}
