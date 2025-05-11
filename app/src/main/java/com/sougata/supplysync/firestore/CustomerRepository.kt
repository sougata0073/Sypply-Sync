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
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.firestore.util.HelperRepository
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class CustomerRepository {

    private val currentUser = Firebase.auth.currentUser!!
    private val db = Firebase.firestore

    private val usersCol =
        this.db.collection(FieldNames.UsersCol.SELF_NAME)
    private val currentUserDoc = this.usersCol.document(this.currentUser.uid)

    private val valuesCol = this.currentUserDoc.collection(FieldNames.ValuesCol.SELF_NAME)

    private val customersCol = this.currentUserDoc
        .collection(FieldNames.CustomersCol.SELF_NAME)
    private val userItemsCol =
        this.currentUserDoc.collection(FieldNames.UserItemsCol.SELF_NAME)
    private val customerPaymentsCol =
        this.currentUserDoc
            .collection(FieldNames.CustomerPaymentsCol.SELF_NAME)
    private val ordersCol =
        this.currentUserDoc
            .collection(FieldNames.OrdersCol.SELF_NAME)


    private val helperRepository = HelperRepository()

    fun getCustomersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FieldNames.CustomersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.CustomersCol.TIMESTAMP to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FieldNames.CustomersCol.SELF_NAME,
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
            firebaseCollectionName = FieldNames.CustomerPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.CustomerPaymentsCol.PAYMENT_TIMESTAMP to Query.Direction.DESCENDING,
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
            firebaseCollectionName = FieldNames.CustomerPaymentsCol.SELF_NAME,
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
            firebaseCollectionName = FieldNames.OrdersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.OrdersCol.TIMESTAMP to Query.Direction.DESCENDING,
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
            firebaseCollectionName = FieldNames.OrdersCol.SELF_NAME,
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
            firebaseCollectionName = FieldNames.UserItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.UserItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FieldNames.UserItemsCol.SELF_NAME,
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
            it.set(customersCol.document(), customer)
            it.update(
                valuesCol.document(FieldNames.ValuesCol.CustomersCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.CustomersCountDoc.VALUE to FieldValue.increment(
                        1
                    )
                )
            )
            it.update(
                valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE to FieldValue.increment(
                        customer.receivableAmount
                    )
                )
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun updateCustomer(customer: Customer, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            var prevTotalReceivableAmount =
                it.get(valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME))
                    .getDouble(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE)
                    ?: 0.0
            var prevCustomerReceivableAmount =
                it.get(customersCol.document(customer.id))
                    .getDouble(FieldNames.CustomersCol.RECEIVABLE_AMOUNT) ?: 0.0
            val newTotalReceivableAmount: Double? =
                prevTotalReceivableAmount - prevCustomerReceivableAmount + customer.receivableAmount
            it.update(customersCol.document(customer.id), customer.toMap())
            it.update(
                valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME),
                mapOf(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE to newTotalReceivableAmount)
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteCustomer(customer: Customer, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.delete(this.customersCol.document(customer.id))
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.CustomersCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.CustomersCountDoc.VALUE to FieldValue.increment(
                        -1
                    )
                )
            )
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE to FieldValue.increment(
                        -customer.receivableAmount
                    )
                )
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addUserItem(userItem: UserItem, onComplete: (Status, String) -> Unit) {
        this.userItemsCol.document().set(userItem).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun updateUserItem(userItem: UserItem, onComplete: (Status, String) -> Unit) {
        this.userItemsCol.document(userItem.id).update(userItem.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteUserItem(userItem: UserItem, onComplete: (Status, String) -> Unit) {
        this.userItemsCol.document(userItem.id).delete().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addCustomerPayment(
        customerPayment: CustomerPayment, onComplete: (Status, String) -> Unit
    ) {
        this.customerPaymentsCol.document().set(customerPayment).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
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
                    onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
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
            .collection(FieldNames.CustomerPaymentsCol.SELF_NAME)
        this.usersCol.firestore.runTransaction {
            it.delete(customerPaymentsCol.document(customerPayment.id))

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addOrder(order: Order, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.set(ordersCol.document(), order)
            if (!order.isDelivered) {
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun updateOrder(order: Order, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            val prevIsDelivered = it.get(this.ordersCol.document(order.id))
                .getBoolean(FieldNames.OrdersCol.IS_DELIVERED) ?: false

            if (prevIsDelivered == true && order.isDelivered == false) {
                it.update(
                    this.valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            } else if (prevIsDelivered == false && order.isDelivered == true) {
                it.update(
                    this.valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
            it.update(this.ordersCol.document(order.id), order.toMap())
        }
    }

    fun deleteOrder(order: Order, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.delete(ordersCol.document(order.id))
            if (!order.isDelivered) {
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun getOrdersToDeliver(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME,
            onComplete
        )
    }

    fun getNumberOfCustomers(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.CustomersCountDoc.SELF_NAME,
            onComplete
        )
    }

    fun getReceivableAmountFromCustomers(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME,
            onComplete
        )
    }

    fun getSalesAmountByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, Number?, String) -> Unit
    ) {
        val query = this.ordersCol
            .whereGreaterThanOrEqualTo(
                FieldNames.OrdersCol.TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                FieldNames.OrdersCol.TIMESTAMP,
                endTimestamp
            ).whereEqualTo(
                FieldNames.OrdersCol.IS_DELIVERED,
                true
            ).aggregate(AggregateField.sum(Order::amount.name))

        query.get(AggregateSource.SERVER).addOnCompleteListener {
            if (it.isSuccessful) {
                val sum =
                    it.result.get(AggregateField.sum(Order::amount.name)) as? Number

                if (sum != null) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToDouble(sum),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, null, KeysAndMessages.TASK_FAILED_TO_COMPLETE)
                }
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

}