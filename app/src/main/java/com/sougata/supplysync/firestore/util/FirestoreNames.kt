package com.sougata.supplysync.firestore.util

object FirestoreNames {

    object Col {
        const val USERS = "users"
        const val SUPPLIERS = "suppliers"
        const val SUPPLIER_ITEMS = "supplier_items"
        const val SUPPLIER_PAYMENTS = "supplier_payments"
        const val ORDERED_ITEMS = "ordered_items"
        const val CUSTOMERS = "customers"
        const val CUSTOMER_PAYMENTS = "customer_payments"
        const val ORDERS = "orders"
        const val USER_ITEMS = "user_items"
        const val VALUES = "values"
    }

    object ValuesDoc {
        const val ORDERS_TO_RECEIVE = "orders_to_receive"
        const val SUPPLIER_ITEMS_COUNT = "supplier_items_count"
        const val SUPPLIERS_COUNT = "suppliers_count"
        const val SUPPLIERS_DUE_AMOUNT = "suppliers_due_amount"
        const val ORDERS_TO_DELIVER = "orders_to_deliver"
        const val CUSTOMERS_COUNT = "customers_count"
        const val RECEIVABLE_AMOUNT_FROM_CUSTOMERS = "receivable_amount_from_customers"

        object Fields {
            const val VALUE = "value"
        }
    }

}