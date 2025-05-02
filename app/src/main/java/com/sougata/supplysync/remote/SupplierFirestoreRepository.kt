package com.sougata.supplysync.remote

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.DataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SupplierFirestoreRepository {

    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore

    private val usersCol =
        this.db.collection(FirestoreFieldNames.UsersCol.SELF_NAME)

    private val firestoreCommonRepository = FirestoreCommonRepository()
    private val modelsMapRepository = ModelsMapRepository()

    companion object {
        const val TO_ADD = "To add"
        const val TO_UPDATE = "To update"
    }

    fun insertUserToFirestore(user: User, onComplete: (Int, String) -> Unit) {

        val userDoc = mapOf(
            FirestoreFieldNames.UsersCol.NAME to user.name,
            FirestoreFieldNames.UsersCol.EMAIL to user.email,
            FirestoreFieldNames.UsersCol.PHONE to user.phone
        )

        usersCol.document(user.uid).set(userDoc)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    this.createRequiredThings { status, message ->
                        if (status == Status.SUCCESS) {
                            onComplete(Status.SUCCESS, "User successfully added to database")
                        } else if (status == Status.FAILED) {
                            onComplete(Status.FAILED, message)
                        }
                    }

                } else {
                    onComplete(Status.FAILED, it.exception?.message.toString())
                }
            }

    }

    fun getSuppliersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {

        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.SUPPLIER, map, document)
        }

        this.firestoreCommonRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreFieldNames.SuppliersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FirestoreFieldNames.SuppliersCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )

    }

    fun getSuppliersListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: DataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.SUPPLIER, map, document)
        }

        this.firestoreCommonRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreFieldNames.SuppliersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }


    fun getSupplierItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {

        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.SUPPLIERS_ITEM, map, document)
        }

        this.firestoreCommonRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreFieldNames.SupplierItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FirestoreFieldNames.SupplierItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getSupplierItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: DataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.SUPPLIERS_ITEM, map, document)
        }

        this.firestoreCommonRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreFieldNames.SupplierItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getOrderedItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.ORDERED_ITEM, map, document)
        }

        this.firestoreCommonRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreFieldNames.OrderedItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FirestoreFieldNames.OrderedItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getOrderedItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: DataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.ORDERED_ITEM, map, document)
        }

        this.firestoreCommonRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreFieldNames.OrderedItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getSupplierPaymentsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.SUPPLIER_PAYMENT, map, document)
        }

        this.firestoreCommonRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreFieldNames.SupplierPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FirestoreFieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP to Query.Direction.DESCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getSupplierPaymentsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: DataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelsMapRepository.getMappedModel(Model.SUPPLIER_PAYMENT, map, document)
        }

        this.firestoreCommonRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FirestoreFieldNames.SupplierPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun addUpdateSupplier(
        supplier: Supplier, action: String, onComplete: (Int, String) -> Unit
    ) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val suppliersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.SuppliersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        val supplierDoc = mutableMapOf<String, Any>(
            FirestoreFieldNames.SuppliersCol.NAME to supplier.name,
            FirestoreFieldNames.SuppliersCol.DUE_AMOUNT to supplier.dueAmount,
            FirestoreFieldNames.SuppliersCol.PHONE to supplier.phone,
            FirestoreFieldNames.SuppliersCol.EMAIL to supplier.email,
            FirestoreFieldNames.SuppliersCol.NOTE to supplier.note,
            FirestoreFieldNames.SuppliersCol.PAYMENT_DETAILS to supplier.paymentDetails,
            FirestoreFieldNames.SuppliersCol.PROFILE_IMAGE_URL to supplier.profileImageUrl
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierDoc.put(
                    FirestoreFieldNames.SuppliersCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(suppliersCol.document(), supplierDoc)
                it.update(
                    valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME),
                    mapOf(
                        FirestoreFieldNames.ValuesCol.SuppliersCountDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
                it.update(
                    valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                    mapOf(
                        FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to FieldValue.increment(
                            supplier.dueAmount
                        )
                    )
                )
            } else if (action == TO_UPDATE) {
                var prevTotalDueAmount =
                    it.get(valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME))
                        .getDouble(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE)
                        ?: 0.0
                var prevSupplierDueAmount =
                    it.get(suppliersCol.document(supplier.id))
                        .getDouble(FirestoreFieldNames.SuppliersCol.DUE_AMOUNT) ?: 0.0

                val newTotalDueAmount: Double? =
                    prevTotalDueAmount - prevSupplierDueAmount + supplier.dueAmount

                it.update(suppliersCol.document(supplier.id), supplierDoc)
                it.update(
                    valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                    mapOf(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to newTotalDueAmount)
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

    fun addUpdateSupplierItem(
        supplierItem: SupplierItem, action: String, onComplete: (Int, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.SupplierItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        val supplierItemDoc = mutableMapOf<String, Any>(
            FirestoreFieldNames.SupplierItemsCol.NAME to supplierItem.name,
            FirestoreFieldNames.SupplierItemsCol.PRICE to supplierItem.price,
            FirestoreFieldNames.SupplierItemsCol.DETAILS to supplierItem.details,
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierItemDoc.put(
                    FirestoreFieldNames.SupplierItemsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(supplierItemsCol.document(), supplierItemDoc)
                it.update(
                    valuesCol.document(FirestoreFieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME),
                    mapOf(
                        FirestoreFieldNames.ValuesCol.SupplierItemsCountDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            } else if (action == TO_UPDATE) {
                it.update(supplierItemsCol.document(supplierItem.id), supplierItemDoc)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addUpdateSupplierPayment(
        supplierPayment: SupplierPayment,
        action: String,
        onComplete: (Int, String) -> Unit
    ) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.SupplierPaymentsCol.SELF_NAME)

        val supplierPaymentDoc = mutableMapOf<String, Any>(
            FirestoreFieldNames.SupplierPaymentsCol.AMOUNT to supplierPayment.amount,
            FirestoreFieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP to supplierPayment.paymentTimestamp,
            FirestoreFieldNames.SupplierPaymentsCol.NOTE to supplierPayment.note,
            FirestoreFieldNames.SupplierPaymentsCol.SUPPLIER_ID to supplierPayment.supplierId,
            FirestoreFieldNames.SupplierPaymentsCol.SUPPLIER_NAME to supplierPayment.supplierName
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierPaymentDoc.put(
                    FirestoreFieldNames.SupplierPaymentsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(supplierPaymentsCol.document(), supplierPaymentDoc)
            } else if (action == TO_UPDATE) {
                it.update(supplierPaymentsCol.document(supplierPayment.id), supplierPaymentDoc)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun addUpdateOrderedItem(
        orderedItem: OrderedItem,
        action: String,
        onComplete: (Int, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.OrderedItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        val orderedItemDoc = mutableMapOf<String, Any>(
            FirestoreFieldNames.OrderedItemsCol.ITEM_ID to orderedItem.itemId,
            FirestoreFieldNames.OrderedItemsCol.ITEM_NAME to orderedItem.itemName,
            FirestoreFieldNames.OrderedItemsCol.QUANTITY to orderedItem.quantity,
            FirestoreFieldNames.OrderedItemsCol.AMOUNT to orderedItem.amount,
            FirestoreFieldNames.OrderedItemsCol.SUPPLIER_ID to orderedItem.supplierId,
            FirestoreFieldNames.OrderedItemsCol.SUPPLIER_NAME to orderedItem.supplierName,
            FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP to orderedItem.orderTimestamp,
            FirestoreFieldNames.OrderedItemsCol.IS_RECEIVED to orderedItem.isReceived
        )

        this.usersCol.firestore.runTransaction {

            if (action == TO_ADD) {
                orderedItemDoc.put(
                    FirestoreFieldNames.OrderedItemsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(orderedItemsCol.document(), orderedItemDoc)
                if (!orderedItem.isReceived) {
                    it.update(
                        valuesCol.document(FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                        mapOf(
                            FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(
                                1
                            )
                        )
                    )
                }

            } else if (action == TO_UPDATE) {

                val prevIsReceived =
                    it.get(orderedItemsCol.document(orderedItem.id))
                        .getBoolean(FirestoreFieldNames.OrderedItemsCol.IS_RECEIVED)
                        ?: false

                if (prevIsReceived == true && orderedItem.isReceived == false) {
                    it.update(
                        valuesCol.document(FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                        mapOf(
                            FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(
                                1
                            )
                        )
                    )
                } else if (prevIsReceived == false && orderedItem.isReceived == true) {
                    it.update(
                        valuesCol.document(FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                        mapOf(
                            FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(
                                -1
                            )
                        )
                    )
                }
                it.update(orderedItemsCol.document(orderedItem.id), orderedItemDoc)

            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun getNumberOfSuppliers(onComplete: (Int, Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, 0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToInt(it.result[FirestoreFieldNames.ValuesCol.SuppliersCountDoc.VALUE] as Number),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, 0, it.exception?.message.toString())
                }
            }
    }

    fun getDueAmountToSuppliers(onComplete: (Int, Double, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, 0.0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToDouble(it.result[FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE] as Number),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, 0.0, it.exception?.message.toString())
                }
            }
    }

    fun getOrdersToReceive(onComplete: (Int, Int, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, 0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        valuesCol.document(FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToInt(it.result[FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.VALUE] as Number),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, 0, it.exception?.message.toString())
                }
            }
    }

    fun deleteSupplier(supplier: Supplier, onComplete: (Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val suppliersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.SuppliersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(suppliersCol.document(supplier.id))
            it.update(
                valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME),
                mapOf(FirestoreFieldNames.ValuesCol.SuppliersCountDoc.VALUE to FieldValue.increment(-1))
            )
            it.update(
                valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                mapOf(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to FieldValue.increment(-supplier.dueAmount))
            )
        }.addOnCompleteListener {

            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Supplier deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }

        }
    }

    fun deleteSupplierItem(supplierItem: SupplierItem, onComplete: (Int, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.SupplierItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(supplierItemsCol.document(supplierItem.id))
            it.update(
                valuesCol.document(FirestoreFieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME),
                mapOf(FirestoreFieldNames.ValuesCol.SupplierItemsCountDoc.VALUE to FieldValue.increment(-1))
            )
        }.addOnCompleteListener {

            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Item deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }

        }

    }

    fun deleteSupplierPayment(supplierPayment: SupplierPayment, onComplete: (Int, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.SupplierPaymentsCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(supplierPaymentsCol.document(supplierPayment.id))
        }.addOnCompleteListener {

            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Payment deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }

        }

    }

    fun deleteOrderedItem(orderedItem: OrderedItem, onComplete: (Int, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.OrderedItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(orderedItemsCol.document(orderedItem.id))
            if (!orderedItem.isReceived) {
                it.update(
                    valuesCol.document(FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                    mapOf(FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(-1))
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Ordered item deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun getPurchaseAmountByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        coroutineScope: CoroutineScope,
        onComplete: (Int, MutableList<Double>, String) -> Unit
    ) {

        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.OrderedItemsCol.SELF_NAME)

        val query = orderedItemsCol
            .whereGreaterThanOrEqualTo(FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP, startTimestamp)
            .whereLessThanOrEqualTo(FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP, endTimestamp)
            .orderBy(FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP, Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val resultList = mutableListOf<Double>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data

                        if (doc.exists() && data != null) {
                            val amount =
                                Converters.numberToDouble(data[FirestoreFieldNames.OrderedItemsCol.AMOUNT] as Number)
                            resultList.add(amount)
                        }
                    }

                    onComplete(
                        Status.SUCCESS,
                        resultList,
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getFrequencyOfOrderedItemsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        coroutineScope: CoroutineScope,
        onComplete: (Int, MutableList<Pair<String, Int>>, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.OrderedItemsCol.SELF_NAME)

        val query = orderedItemsCol
            .whereGreaterThanOrEqualTo(FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP, startTimestamp)
            .whereLessThanOrEqualTo(FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP, endTimestamp)
            .orderBy(FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP, Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val map = hashMapOf<String, Int>()
                val resultList = mutableListOf<Pair<String, Int>>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data
                        if (doc.exists() && data != null) {
                            val itemName = data[FirestoreFieldNames.OrderedItemsCol.ITEM_NAME] as String
                            map[itemName] = map.getOrDefault(itemName, 0) + 1
                        }
                    }
                    for (item in map) {
                        resultList.add(Pair(item.key, item.value))
                    }

                    onComplete(
                        Status.SUCCESS,
                        resultList,
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getSupplierPaymentsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        coroutineScope: CoroutineScope,
        onComplete: (Int, MutableList<SupplierPayment>, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FirestoreFieldNames.SupplierPaymentsCol.SELF_NAME)

        val query = supplierPaymentsCol
            .whereGreaterThanOrEqualTo(
                FirestoreFieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(FirestoreFieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP, endTimestamp)
            .orderBy(FirestoreFieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP, Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val resultList = mutableListOf<SupplierPayment>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data

                        if (doc.exists() && data != null) {
                            val supplierPayment = SupplierPayment(
                                amount = Converters.numberToDouble(data[FirestoreFieldNames.SupplierPaymentsCol.AMOUNT] as Number),
                                paymentTimestamp = data[FirestoreFieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP] as Timestamp,
                                note = data[FirestoreFieldNames.SupplierPaymentsCol.NOTE] as String,
                                supplierId = data[FirestoreFieldNames.SupplierPaymentsCol.SUPPLIER_ID] as String,
                                supplierName = data[FirestoreFieldNames.SupplierPaymentsCol.SUPPLIER_NAME] as String
                            ).apply {
                                id = doc.id
                                timestamp =
                                    data[FirestoreFieldNames.SupplierPaymentsCol.TIMESTAMP] as Timestamp
                            }
                            resultList.add(supplierPayment)
                        }
                    }

                    onComplete(
                        Status.SUCCESS,
                        resultList,
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getCurrentUserDetails(onComplete: (Int, User?, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, null, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        this.usersCol.document(this.currentUser.uid).get().addOnCompleteListener {

            if (it.isSuccessful) {
                val doc = it.result
                val map = doc.data
                if (map != null) {
                    val user = User(
                        map[FirestoreFieldNames.UsersCol.NAME] as String,
                        map[FirestoreFieldNames.UsersCol.EMAIL] as String,
                        map[FirestoreFieldNames.UsersCol.PHONE] as String,
                        doc.id
                    )
                    onComplete(Status.SUCCESS, user, it.exception?.message.toString())
                }
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }

        }
    }

    private fun createRequiredThings(onComplete: (Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FirestoreFieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {

            // Add value fields here
            val map = mapOf("value" to 0)

            it.set(valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME), map)
            it.set(valuesCol.document(FirestoreFieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME), map)
            it.set(valuesCol.document(FirestoreFieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME), map)
            it.set(valuesCol.document(FirestoreFieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME), map)

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

}