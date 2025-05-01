package com.sougata.supplysync.cloud

object FieldNamesRepository {

    object UsersCollection {
        const val THIS_COLLECTION_NAME = "users"
        const val EMAIL = "email"
        const val NAME = "name"
        const val PHONE = "phone"
    }

    object OrderedItemsCollection {
        const val THIS_COLLECTION_NAME = "ordered_items"
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

    object SupplierItemsCollection {
        const val THIS_COLLECTION_NAME = "supplier_items"
        const val DETAILS = "details"
        const val NAME = "name"
        const val PRICE = "price"
        const val TIMESTAMP = "timestamp"
    }

    object SupplierPaymentsCollection {
        const val THIS_COLLECTION_NAME = "supplier_payments"
        const val AMOUNT = "amount"
        const val NOTE = "note"
        const val PAYMENT_TIMESTAMP = "payment_timestamp"
        const val SUPPLIER_ID = "supplier_id"
        const val SUPPLIER_NAME = "supplier_name"
        const val TIMESTAMP = "timestamp"
    }

    object SuppliersCollection {
        const val THIS_COLLECTION_NAME = "suppliers"
        const val DUE_AMOUNT = "due_amount"
        const val EMAIL = "email"
        const val NAME = "name"
        const val NOTE = "note"
        const val PAYMENT_DETAILS = "payment_details"
        const val PHONE = "phone"
        const val PROFILE_IMAGE_URL = "profile_image_url"
        const val TIMESTAMP = "timestamp"
    }

    object ValuesCollection {
        const val THIS_COLLECTION_NAME = "values"

        object OrdersToReceiveDocument {
            const val THIS_DOCUMENT_NAME = "orders_to_receive"
            const val VALUE = "value"
        }

        object SupplierItemsCountDocument {
            const val THIS_DOCUMENT_NAME = "supplier_items_count"
            const val VALUE = "value"
        }

        object SuppliersCountDocument {
            const val THIS_DOCUMENT_NAME = "suppliers_count"
            const val VALUE = "value"
        }

        object SuppliersDueAmountDocument {
            const val THIS_DOCUMENT_NAME = "suppliers_due_amount"
            const val VALUE = "value"
        }
    }
}