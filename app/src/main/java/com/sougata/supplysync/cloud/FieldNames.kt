package com.sougata.supplysync.cloud

object FieldNames {

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
    }
}