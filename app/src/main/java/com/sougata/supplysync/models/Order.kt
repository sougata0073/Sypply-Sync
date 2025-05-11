package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    override var id: String,
    override var timestamp: Timestamp,
    var userItemId: String,
    var userItemName: String,
    var quantity: Int,
    var amount: Double,
    var customerId: String,
    var customerName: String,
    var deliveryTimestamp: Timestamp,
    var isDelivered: Boolean
): Model, Parcelable {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "userItemId" to userItemId,
            "userItemName" to userItemName,
            "quantity" to quantity,
            "amount" to amount,
            "customerId" to customerId,
            "customerName" to customerName,
            "deliveryTimestamp" to deliveryTimestamp,
            "isDelivered" to isDelivered
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return Order(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            userItemId = map["userItemId"] as String,
            userItemName = map["userItemName"] as String,
            quantity = map["quantity"] as Int,
            amount = map["amount"] as Double,
            customerId = map["customerId"] as String,
            customerName = map["customerName"] as String,
            deliveryTimestamp = map["deliveryTimestamp"] as Timestamp,
            isDelivered = map["isDelivered"] as Boolean
        )
    }
}
