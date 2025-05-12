package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem(
    override var id: String,
    override var timestamp: Timestamp,
    var name: String,
    var inStock: Int,
    var price: Double,
    var details: String
) : Model, Parcelable {

    constructor() : this("", Timestamp.now(), "", 0, 0.0, "")

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "name" to name,
            "inStock" to inStock,
            "price" to price,
            "details" to details
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return UserItem(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            name = map["name"] as String,
            inStock = map["inStock"] as Int,
            price = map["price"] as Double,
            details = map["details"] as String,
        )
    }
}
