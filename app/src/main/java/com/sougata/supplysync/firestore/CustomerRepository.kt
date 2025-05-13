package com.sougata.supplysync.firestore

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.firestore.util.FirestoreNames
import com.sougata.supplysync.firestore.util.HelperRepository
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Messages
import com.sougata.supplysync.util.Status

class CustomerRepository {

    private val currentUser = Firebase.auth.currentUser!!
    private val db = Firebase.firestore

    private val usersCol = this.db.collection(FirestoreNames.Col.USERS)
    private val currentUserDoc = this.usersCol.document(this.currentUser.uid)

    private val valuesCol = this.currentUserDoc.collection(FirestoreNames.Col.VALUES)

    private val customersCol = this.currentUserDoc.collection(FirestoreNames.Col.CUSTOMERS)
    private val userItemsCol = this.currentUserDoc.collection(FirestoreNames.Col.USER_ITEMS)
    private val customerPaymentsCol =
        this.currentUserDoc.collection(FirestoreNames.Col.CUSTOMER_PAYMENTS)
    private val ordersCol = this.currentUserDoc.collection(FirestoreNames.Col.ORDERS)


    private val helperRepository = HelperRepository()

    fun getCustomersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreNames.Col.CUSTOMERS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = Customer::timestamp.name to Query.Direction.ASCENDING,
            limit = limit,
            clazz = Customer::class.java,
            onComplete = onComplete
        )
    }

    fun getCustomersListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreNames.Col.CUSTOMERS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = Customer::class.java,
            onComplete = onComplete
        )
    }

    fun getCustomerPaymentsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreNames.Col.CUSTOMER_PAYMENTS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = CustomerPayment::paymentTimestamp.name to Query.Direction.DESCENDING,
            limit = limit,
            clazz = CustomerPayment::class.java,
            onComplete = onComplete
        )
    }

    fun getCustomerPaymentsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreNames.Col.CUSTOMER_PAYMENTS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = CustomerPayment::class.java,
            onComplete = onComplete
        )
    }

    fun getOrdersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreNames.Col.ORDERS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = Order::timestamp.name to Query.Direction.DESCENDING,
            limit = limit,
            clazz = Order::class.java,
            onComplete = onComplete
        )
    }

    fun getOrdersListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreNames.Col.ORDERS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = Order::class.java,
            onComplete = onComplete
        )
    }

    fun getUserItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreNames.Col.USER_ITEMS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = UserItem::timestamp.name to Query.Direction.ASCENDING,
            limit = limit,
            clazz = UserItem::class.java,
            onComplete = onComplete
        )
    }

    fun getUserItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreNames.Col.USER_ITEMS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = UserItem::class.java,
            onComplete = onComplete
        )
    }

    fun addCustomer(
        customer: Customer, onComplete: (Status, String) -> Unit
    ) {
        this.usersCol.firestore.runTransaction {
            it.set(customersCol.document(customer.id), customer)
            it.update(
                valuesCol.document(FirestoreNames.ValuesDoc.CUSTOMERS_COUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                        1
                    )
                )
            )
            it.update(
                valuesCol.document(FirestoreNames.ValuesDoc.RECEIVABLE_AMOUNT_FROM_CUSTOMERS),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                        customer.receivableAmount
                    )
                )
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun updateCustomer(customer: Customer, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            var prevTotalReceivableAmount =
                it.get(valuesCol.document(FirestoreNames.ValuesDoc.RECEIVABLE_AMOUNT_FROM_CUSTOMERS))
                    .getDouble(FirestoreNames.ValuesDoc.Fields.VALUE)
                    ?: 0.0
            var prevCustomerReceivableAmount =
                it.get(customersCol.document(customer.id))
                    .getDouble(Customer::receivableAmount.name) ?: 0.0
            val newTotalReceivableAmount: Double? =
                prevTotalReceivableAmount - prevCustomerReceivableAmount + customer.receivableAmount
            it.update(customersCol.document(customer.id), customer.toMap())
            it.update(
                valuesCol.document(FirestoreNames.ValuesDoc.RECEIVABLE_AMOUNT_FROM_CUSTOMERS),
                mapOf(FirestoreNames.ValuesDoc.Fields.VALUE to newTotalReceivableAmount)
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteCustomer(customer: Customer, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.delete(this.customersCol.document(customer.id))
            it.update(
                this.valuesCol.document(FirestoreNames.ValuesDoc.CUSTOMERS_COUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                        -1
                    )
                )
            )
            it.update(
                this.valuesCol.document(FirestoreNames.ValuesDoc.RECEIVABLE_AMOUNT_FROM_CUSTOMERS),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                        -customer.receivableAmount
                    )
                )
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addUserItem(userItem: UserItem, onComplete: (Status, String) -> Unit) {
        this.userItemsCol.document(userItem.id).set(userItem).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun updateUserItem(userItem: UserItem, onComplete: (Status, String) -> Unit) {
        this.userItemsCol.document(userItem.id).update(userItem.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteUserItem(userItem: UserItem, onComplete: (Status, String) -> Unit) {
        this.userItemsCol.document(userItem.id).delete().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addCustomerPayment(
        customerPayment: CustomerPayment, onComplete: (Status, String) -> Unit
    ) {
        this.customerPaymentsCol.document(customerPayment.id).set(customerPayment)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
                } else {
                    onComplete(Status.FAILED, it.exception?.message.toString())
                }
            }
    }

    fun updateCustomerPayment(
        customerPayment: CustomerPayment, onComplete: (Status, String) -> Unit
    ) {
        this.customerPaymentsCol.document(customerPayment.id).update(customerPayment.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
                } else {
                    onComplete(Status.FAILED, it.exception?.message.toString())
                }
            }
    }

    fun deleteCustomerPayment(
        customerPayment: CustomerPayment,
        onComplete: (Status, String) -> Unit
    ) {
        val customerPaymentsCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreNames.Col.CUSTOMER_PAYMENTS)
        this.usersCol.firestore.runTransaction {
            it.delete(customerPaymentsCol.document(customerPayment.id))

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addOrder(order: Order, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.set(ordersCol.document(order.id), order)
            if (!order.delivered) {
                it.update(
                    valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_DELIVER),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun updateOrder(order: Order, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            val prevIsDelivered = it.get(this.ordersCol.document(order.id))
                .getBoolean(Order::delivered.name) ?: false

            if (prevIsDelivered == true && order.delivered == false) {
                it.update(
                    this.valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_DELIVER),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            } else if (prevIsDelivered == false && order.delivered == true) {
                it.update(
                    this.valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_DELIVER),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
            it.update(this.ordersCol.document(order.id), order.toMap())
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteOrder(order: Order, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.delete(ordersCol.document(order.id))
            if (!order.delivered) {
                it.update(
                    valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_DELIVER),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, Messages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun getOrdersToDeliver(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FirestoreNames.ValuesDoc.ORDERS_TO_DELIVER,
            onComplete
        )
    }

    fun getNumberOfCustomers(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FirestoreNames.ValuesDoc.CUSTOMERS_COUNT,
            onComplete
        )
    }

    fun getReceivableAmountFromCustomers(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FirestoreNames.ValuesDoc.RECEIVABLE_AMOUNT_FROM_CUSTOMERS,
            onComplete
        )
    }

//    fun getSalesAmountListByRange(
//        startTimestamp: Timestamp,
//        endTimestamp: Timestamp,
//        onComplete: (Status, List<Double>?, String) -> Unit
//    ) {
//        val query = this.ordersCol
//            .whereGreaterThanOrEqualTo(
//                Order::deliveryTimestamp.name,
//                startTimestamp
//            )
//            .whereLessThanOrEqualTo(
//                Order::deliveryTimestamp.name,
//                endTimestamp
//            )
//            .orderBy(Order::deliveryTimestamp.name, Query.Direction.ASCENDING)
//
//        query.get().addOnCompleteListener {
//            if (it.isSuccessful) {
//
//                val resultList = mutableListOf<Double>()
//
//                for (doc in it.result.documents) {
//                    if (doc.exists()) {
//                        val amount =
//                            Converters.numberToDouble(doc.get(Order::amount.name) as Number)
//                        resultList.add(amount)
//                    }
//                }
//                onComplete(
//                    Status.SUCCESS,
//                    resultList,
//                    Messages.TASK_COMPLETED_SUCCESSFULLY
//                )
//            } else {
//                onComplete(Status.FAILED, null, it.exception?.message.toString())
//            }
//        }
//    }

    fun getSalesAmountListByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, List<Pair<Double, Timestamp>>?, String) -> Unit
    ) {
        val query = this.ordersCol
            .whereGreaterThanOrEqualTo(
                Order::deliveryTimestamp.name,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                Order::deliveryTimestamp.name,
                endTimestamp
            )
            .orderBy(Order::deliveryTimestamp.name, Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {

                val resultList = mutableListOf<Pair<Double, Timestamp>>()

                for (doc in it.result.documents) {
                    if (doc.exists()) {
                        val amount =
                            Converters.numberToDouble(doc.get(Order::amount.name) as Number)
                        val timestamp = doc.getTimestamp(Order::deliveryTimestamp.name) as Timestamp
                        resultList.add(amount to timestamp)
                    }
                }
                onComplete(
                    Status.SUCCESS,
                    resultList,
                    Messages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

    fun getSalesAmountByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, Number?, String) -> Unit
    ) {
        val query = this.ordersCol
            .whereEqualTo(
                Order::delivered.name,
                false
            )
            .whereGreaterThanOrEqualTo(
                Order::timestamp.name,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                Order::timestamp.name,
                endTimestamp
            ).aggregate(AggregateField.sum(Order::amount.name))

        query.get(AggregateSource.SERVER).addOnCompleteListener {
            if (it.isSuccessful) {
                val sum =
                    it.result.get(AggregateField.sum(Order::amount.name)) as? Number

                if (sum != null) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToDouble(sum),
                        Messages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, null, Messages.TASK_FAILED_TO_COMPLETE)
                }
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

}