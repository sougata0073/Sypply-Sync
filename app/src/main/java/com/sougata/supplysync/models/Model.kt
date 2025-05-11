package com.sougata.supplysync.models

import com.google.firebase.Timestamp

interface Model {

    var id: String
    var timestamp: Timestamp
    fun toMap(): Map<String, Any>
    fun toModel(map: Map<String, Any>): Model

    companion object {
        const val SUPPLIER = "Supplier"
        const val USER = "User"
        const val SUPPLIERS_ITEM = "Supplier item"
        const val SUPPLIER_PAYMENT = "Supplier payment"
        const val ORDERED_ITEM = "Ordered item"
        const val CUSTOMER = "Customer"
        const val CUSTOMER_PAYMENT = "Customer payment"
        const val ORDER = "Order"
        const val USER_ITEM = "User item"

    }
}