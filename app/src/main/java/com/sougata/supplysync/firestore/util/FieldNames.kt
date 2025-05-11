package com.sougata.supplysync.firestore.util

import com.google.firebase.Timestamp

object FieldNames {

    object Commons {
        const val ID = "id"
        const val TIMESTAMP = "timestamp"
        const val VALUE = "value"
    }

    object UsersCol {
        const val SELF_NAME = "users"
        const val EMAIL = "email"
        const val NAME = "name"
        const val PHONE = "phone"
    }

    object OrderedItemsCol {
        const val SELF_NAME = "ordered_items"
        const val AMOUNT = "amount"
        const val IS_RECEIVED = "is_received"
        const val ITEM_ID = "item_id"
        const val ITEM_NAME = "item_name"
        const val ORDER_TIMESTAMP = "order_timestamp"
        const val QUANTITY = "quantity"
        const val SUPPLIER_ID = "supplier_id"
        const val SUPPLIER_NAME = "supplier_name"
        const val TIMESTAMP = "timestamp"
    }

    object SupplierItemsCol {
        const val SELF_NAME = "supplier_items"
        const val DETAILS = "details"
        const val NAME = "name"
        const val PRICE = "price"
        const val TIMESTAMP = "timestamp"
    }

    object SupplierPaymentsCol {
        const val SELF_NAME = "supplier_payments"
        const val AMOUNT = "amount"
        const val NOTE = "note"
        const val PAYMENT_TIMESTAMP = "payment_timestamp"
        const val SUPPLIER_ID = "supplier_id"
        const val SUPPLIER_NAME = "supplier_name"
        const val TIMESTAMP = "timestamp"
    }

    object SuppliersCol {
        const val SELF_NAME = "suppliers"
        const val DUE_AMOUNT = "due_amount"
        const val EMAIL = "email"
        const val NAME = "name"
        const val NOTE = "note"
        const val PAYMENT_DETAILS = "payment_details"
        const val PHONE = "phone"
        const val PROFILE_IMAGE_URL = "profile_image_url"
        const val TIMESTAMP = "timestamp"
    }

    object CustomersCol {
        const val SELF_NAME = "customers"
        const val NAME = "name"
        const val RECEIVABLE_AMOUNT = "receivable_amount"
        const val DUE_ORDERS = "due_orders"
        const val PHONE = "phone"
        const val EMAIL = "email"
        const val NOTE = "note"
        const val PROFILE_IMAGE_URL = "profile_image_url"
        const val TIMESTAMP = "timestamp"
    }

    object CustomerPaymentsCol {
        const val SELF_NAME = "customer_payments"
        const val AMOUNT = "amount"
        const val PAYMENT_TIMESTAMP = "payment_timestamp"
        const val NOTE = "note"
        const val CUSTOMER_ID = "customer_id"
        const val CUSTOMER_NAME = "customer_name"
        const val TIMESTAMP = "timestamp"
    }

    object OrdersCol {
        const val SELF_NAME = "orders"
        const val USER_ITEM_ID = "user_item_id"
        const val USER_ITEM_NAME = "user_item_name"
        const val QUANTITY = "quantity"
        const val AMOUNT = "amount"
        const val CUSTOMER_ID = "customer_id"
        const val CUSTOMER_NAME = "customer_name"
        const val DELIVERY_TIMESTAMP = "delivery_timestamp"
        const val IS_DELIVERED = "is_delivered"
        const val TIMESTAMP = "timestamp"
    }

    object UserItemsCol {
        const val SELF_NAME = "users"
        const val NAME = "name"
        const val IN_STOCK = "in_stock"
        const val PRICE = "price"
        const val DETAILS = "details"
        const val TIMESTAMP = "timestamp"
    }

    object ValuesCol {
        const val SELF_NAME = "values"

        object OrdersToReceiveDoc {
            const val SELF_NAME = "orders_to_receive"
            const val VALUE = "value"
        }

        object SupplierItemsCountDoc {
            const val SELF_NAME = "supplier_items_count"
            const val VALUE = "value"
        }

        object SuppliersCountDoc {
            const val SELF_NAME = "suppliers_count"
            const val VALUE = "value"
        }

        object SuppliersDueAmountDoc {
            const val SELF_NAME = "suppliers_due_amount"
            const val VALUE = "value"
        }

        object OrdersToDeliverDoc {
            const val SELF_NAME = "orders_to_deliver"
            const val VALUE = "value"
        }

        object CustomersCountDoc {
            const val SELF_NAME = "customers_count"
            const val VALUE = "value"
        }

        object ReceivableAmountFromCustomersDoc {
            const val SELF_NAME = "receivable_amount_from_customers"
            const val VALUE = "value"
        }
    }
}