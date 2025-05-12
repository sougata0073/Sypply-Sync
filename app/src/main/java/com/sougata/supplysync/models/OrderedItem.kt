package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderedItem(
    override var id: String,
    override var timestamp: Timestamp,
    var supplierItemId: String,
    var supplierItemName: String,
    var quantity: Int,
    var amount: Double,
    var supplierId: String,
    var supplierName: String,
    var orderTimestamp: Timestamp,
    var received: Boolean
): Model, Parcelable {

    constructor() : this("", Timestamp.now(), "", "", 0, 0.0, "", "", Timestamp.now(), false)

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "supplierItemId" to supplierItemId,
            "supplier" to supplierName,
            "quantity" to quantity,
            "amount" to amount,
            "supplierId" to supplierId,
            "supplierName" to supplierName,
            "orderTimestamp" to orderTimestamp,
            "received" to received
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return OrderedItem(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            supplierItemId = map["supplierItemId"] as String,
            supplierItemName = map["supplierItemName"] as String,
            quantity = map["quantity"] as Int,
            amount = map["amount"] as Double,
            supplierId = map["supplierId"] as String,
            supplierName = map["supplierName"] as String,
            orderTimestamp = map["orderTimestamp"] as Timestamp,
            received = map["received"] as Boolean
        )
    }
}
