package com.sougata.supplysync.cloud

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
import com.sougata.supplysync.util.modelslist.DataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SupplierFirestoreRepository {

    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore

    private val usersCol =
        this.db.collection(FieldNamesRepository.UsersCollection.THIS_COLLECTION_NAME)

    private val firestoreCommonRepository = FirestoreCommonRepository()
    private val modelsMapRepository = ModelsMapRepository()

    companion object {
        const val TO_ADD = "To add"
        const val TO_UPDATE = "To update"
    }

    fun insertUserToFirestore(user: User, onComplete: (Int, String) -> Unit) {

        val userDoc = mapOf(
            FieldNamesRepository.UsersCollection.NAME to user.name,
            FieldNamesRepository.UsersCollection.EMAIL to user.email,
            FieldNamesRepository.UsersCollection.PHONE to user.phone
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
            firebaseCollectionName = FieldNamesRepository.SuppliersCollection.THIS_COLLECTION_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNamesRepository.SuppliersCollection.TIMESTAMP to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FieldNamesRepository.SuppliersCollection.THIS_COLLECTION_NAME,
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
            firebaseCollectionName = FieldNamesRepository.SupplierItemsCollection.THIS_COLLECTION_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNamesRepository.SupplierItemsCollection.TIMESTAMP to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FieldNamesRepository.SupplierItemsCollection.THIS_COLLECTION_NAME,
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
            firebaseCollectionName = FieldNamesRepository.OrderedItemsCollection.THIS_COLLECTION_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNamesRepository.OrderedItemsCollection.TIMESTAMP to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FieldNamesRepository.OrderedItemsCollection.THIS_COLLECTION_NAME,
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
            firebaseCollectionName = FieldNamesRepository.SupplierPaymentsCollection.THIS_COLLECTION_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNamesRepository.SupplierPaymentsCollection.TIMESTAMP to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FieldNamesRepository.SupplierPaymentsCollection.THIS_COLLECTION_NAME,
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
            .collection(FieldNamesRepository.SuppliersCollection.THIS_COLLECTION_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        val supplierDoc = mutableMapOf<String, Any>(
            FieldNamesRepository.SuppliersCollection.NAME to supplier.name,
            FieldNamesRepository.SuppliersCollection.DUE_AMOUNT to supplier.dueAmount,
            FieldNamesRepository.SuppliersCollection.PHONE to supplier.phone,
            FieldNamesRepository.SuppliersCollection.EMAIL to supplier.email,
            FieldNamesRepository.SuppliersCollection.NOTE to supplier.note,
            FieldNamesRepository.SuppliersCollection.PAYMENT_DETAILS to supplier.paymentDetails,
            FieldNamesRepository.SuppliersCollection.PROFILE_IMAGE_URL to supplier.profileImageUrl
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierDoc.put(
                    FieldNamesRepository.SuppliersCollection.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(suppliersCol.document(), supplierDoc)
                it.update(
                    valuesCol.document(FieldNamesRepository.ValuesCollection.SuppliersCountDocument.THIS_DOCUMENT_NAME),
                    mapOf(
                        FieldNamesRepository.ValuesCollection.SuppliersCountDocument.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
                it.update(
                    valuesCol.document(FieldNamesRepository.ValuesCollection.SuppliersDueAmountDocument.THIS_DOCUMENT_NAME),
                    mapOf(
                        FieldNamesRepository.ValuesCollection.SuppliersDueAmountDocument.VALUE to FieldValue.increment(
                            supplier.dueAmount
                        )
                    )
                )
            } else if (action == TO_UPDATE) {
                var prevTotalDueAmount =
                    it.get(valuesCol.document(FieldNamesRepository.ValuesCollection.SuppliersDueAmountDocument.THIS_DOCUMENT_NAME))
                        .getDouble(FieldNamesRepository.ValuesCollection.SuppliersDueAmountDocument.VALUE)
                        ?: 0.0
                var prevSupplierDueAmount =
                    it.get(suppliersCol.document(supplier.id))
                        .getDouble(FieldNamesRepository.SuppliersCollection.DUE_AMOUNT) ?: 0.0

                val newTotalDueAmount: Double? =
                    prevTotalDueAmount - prevSupplierDueAmount + supplier.dueAmount

                it.update(suppliersCol.document(supplier.id), supplierDoc)
                it.update(
                    valuesCol.document(FieldNamesRepository.ValuesCollection.SuppliersDueAmountDocument.THIS_DOCUMENT_NAME),
                    mapOf(FieldNamesRepository.ValuesCollection.SuppliersDueAmountDocument.VALUE to newTotalDueAmount)
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
                .collection(FieldNamesRepository.SupplierItemsCollection.THIS_COLLECTION_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        val supplierItemDoc = mutableMapOf<String, Any>(
            FieldNamesRepository.SupplierItemsCollection.NAME to supplierItem.name,
            FieldNamesRepository.SupplierItemsCollection.PRICE to supplierItem.price,
            FieldNamesRepository.SupplierItemsCollection.DETAILS to supplierItem.details,
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierItemDoc.put(
                    FieldNamesRepository.SupplierItemsCollection.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(supplierItemsCol.document(), supplierItemDoc)
                it.update(
                    valuesCol.document(FieldNamesRepository.ValuesCollection.SupplierItemsCountDocument.THIS_DOCUMENT_NAME),
                    mapOf(
                        FieldNamesRepository.ValuesCollection.SupplierItemsCountDocument.VALUE to FieldValue.increment(
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
                .collection(FieldNamesRepository.SupplierPaymentsCollection.THIS_COLLECTION_NAME)

        val supplierPaymentDoc = mutableMapOf<String, Any>(
            FieldNamesRepository.SupplierPaymentsCollection.AMOUNT to supplierPayment.amount,
            FieldNamesRepository.SupplierPaymentsCollection.PAYMENT_TIMESTAMP to supplierPayment.paymentTimestamp,
            FieldNamesRepository.SupplierPaymentsCollection.NOTE to supplierPayment.note,
            FieldNamesRepository.SupplierPaymentsCollection.SUPPLIER_ID to supplierPayment.supplierId,
            FieldNamesRepository.SupplierPaymentsCollection.SUPPLIER_NAME to supplierPayment.supplierName
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierPaymentDoc.put(
                    FieldNamesRepository.SupplierPaymentsCollection.TIMESTAMP,
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
                .collection(FieldNamesRepository.OrderedItemsCollection.THIS_COLLECTION_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        val orderedItemDoc = mutableMapOf<String, Any>(
            FieldNamesRepository.OrderedItemsCollection.ITEM_ID to orderedItem.itemId,
            FieldNamesRepository.OrderedItemsCollection.ITEM_NAME to orderedItem.itemName,
            FieldNamesRepository.OrderedItemsCollection.QUANTITY to orderedItem.quantity,
            FieldNamesRepository.OrderedItemsCollection.AMOUNT to orderedItem.amount,
            FieldNamesRepository.OrderedItemsCollection.SUPPLIER_ID to orderedItem.supplierId,
            FieldNamesRepository.OrderedItemsCollection.SUPPLIER_NAME to orderedItem.supplierName,
            FieldNamesRepository.OrderedItemsCollection.ORDER_TIMESTAMP to orderedItem.orderTimestamp,
            FieldNamesRepository.OrderedItemsCollection.IS_RECEIVED to orderedItem.isReceived
        )

        this.usersCol.firestore.runTransaction {

            if (action == TO_ADD) {
                orderedItemDoc.put(
                    FieldNamesRepository.OrderedItemsCollection.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(orderedItemsCol.document(), orderedItemDoc)
                if (!orderedItem.isReceived) {
                    it.update(
                        valuesCol.document(FieldNamesRepository.ValuesCollection.OrdersToReceiveDocument.THIS_DOCUMENT_NAME),
                        mapOf(
                            FieldNamesRepository.ValuesCollection.OrdersToReceiveDocument.VALUE to FieldValue.increment(
                                1
                            )
                        )
                    )
                }

            } else if (action == TO_UPDATE) {

                val prevIsReceived =
                    it.get(orderedItemsCol.document(orderedItem.id))
                        .getBoolean(FieldNamesRepository.OrderedItemsCollection.IS_RECEIVED)
                        ?: false

                if (prevIsReceived == true && orderedItem.isReceived == false) {
                    it.update(
                        valuesCol.document(FieldNamesRepository.ValuesCollection.OrdersToReceiveDocument.THIS_DOCUMENT_NAME),
                        mapOf(
                            FieldNamesRepository.ValuesCollection.OrdersToReceiveDocument.VALUE to FieldValue.increment(
                                1
                            )
                        )
                    )
                } else if (prevIsReceived == false && orderedItem.isReceived == true) {
                    it.update(
                        valuesCol.document(FieldNamesRepository.ValuesCollection.OrdersToReceiveDocument.THIS_DOCUMENT_NAME),
                        mapOf(
                            FieldNamesRepository.ValuesCollection.OrdersToReceiveDocument.VALUE to FieldValue.increment(
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
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        valuesCol.document(FieldNamesRepository.ValuesCollection.SuppliersCountDocument.THIS_DOCUMENT_NAME)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToInt(it.result[FieldNamesRepository.ValuesCollection.SuppliersCountDocument.VALUE] as Number),
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
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        valuesCol.document("suppliers_due_amount").get().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(
                    Status.SUCCESS,
                    Converters.numberToDouble(it.result["value"] as Number),
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
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        valuesCol.document("orders_to_receive").get().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(
                    Status.SUCCESS,
                    Converters.numberToInt(it.result["value"] as Number),
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

        val suppliersCol = this.usersCol.document(this.currentUser.uid).collection("suppliers")
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(suppliersCol.document(supplier.id))
            it.update(
                valuesCol.document("suppliers_count"),
                mapOf("value" to FieldValue.increment(-1))
            )
            it.update(
                valuesCol.document("suppliers_due_amount"),
                mapOf("value" to FieldValue.increment(-supplier.dueAmount))
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
            this.usersCol.document(this.currentUser.uid).collection("supplier_items")
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(supplierItemsCol.document(supplierItem.id))
            it.update(
                valuesCol.document("supplier_items_count"),
                mapOf("value" to FieldValue.increment(-1))
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
            this.usersCol.document(this.currentUser.uid).collection("supplier_payments")

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
            this.usersCol.document(this.currentUser.uid).collection("ordered_items")
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(orderedItemsCol.document(orderedItem.id))
            if (!orderedItem.isReceived) {
                it.update(
                    valuesCol.document("orders_to_receive"),
                    mapOf("value" to FieldValue.increment(-1))
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
            this.usersCol.document(this.currentUser.uid).collection("ordered_items")

        val query = orderedItemsCol
            .whereGreaterThanOrEqualTo("order_timestamp", startTimestamp)
            .whereLessThanOrEqualTo("order_timestamp", endTimestamp)
            .orderBy("order_timestamp", Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val resultList = mutableListOf<Double>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data

                        if (doc.exists() && data != null) {
                            val amount = Converters.numberToDouble(data["amount"] as Number)
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
            this.usersCol.document(this.currentUser.uid).collection("ordered_items")

        val query = orderedItemsCol
            .whereGreaterThanOrEqualTo("order_timestamp", startTimestamp)
            .whereLessThanOrEqualTo("order_timestamp", endTimestamp)
            .orderBy("order_timestamp", Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val map = hashMapOf<String, Int>()
                val resultList = mutableListOf<Pair<String, Int>>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data
                        if (doc.exists() && data != null) {
                            val itemName = data["item_name"] as String
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
            this.usersCol.document(this.currentUser.uid).collection("supplier_payments")

        val query = supplierPaymentsCol
            .whereGreaterThanOrEqualTo("payment_timestamp", startTimestamp)
            .whereLessThanOrEqualTo("payment_timestamp", endTimestamp)
            .orderBy("payment_timestamp", Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val resultList = mutableListOf<SupplierPayment>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data

                        if (doc.exists() && data != null) {
                            val supplierPayment = SupplierPayment(
                                amount = Converters.numberToDouble(data["amount"] as Number),
                                paymentTimestamp = data["payment_timestamp"] as Timestamp,
                                note = data["note"] as String,
                                supplierId = data["supplier_id"] as String,
                                supplierName = data["supplier_name"] as String
                            ).apply {
                                id = doc.id
                                timestamp = data["timestamp"] as Timestamp
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
                        map["name"] as String,
                        map["email"] as String,
                        map["phone"] as String,
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
            .collection(FieldNamesRepository.ValuesCollection.THIS_COLLECTION_NAME)

        this.usersCol.firestore.runTransaction {

            // Add value fields here
            val map = mapOf("value" to 0)

            it.set(valuesCol.document("suppliers_count"), map)
            it.set(valuesCol.document("suppliers_due_amount"), map)
            it.set(valuesCol.document("supplier_items_count"), map)
            it.set(valuesCol.document("orders_to_receive"), map)

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

}