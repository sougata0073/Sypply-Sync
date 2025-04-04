package com.sougata.supplysync.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreRepository {

    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private val usersCol = this.db.collection("users")

    companion object {
        const val TO_ADD = "To add"
        const val TO_UPDATE = "To update"
    }

    fun insertUserToFirestore(user: User, onComplete: (Int, String) -> Unit) {

        val userDoc = mapOf(
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone
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

    fun getSupplierItemsList(
        coroutineScope: CoroutineScope, lastDocumentSnapshot: DocumentSnapshot?,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit
    ) {

        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            SupplierItem(
                name = map["name"] as String,
                price = Converters.numberToDouble(map["price"] as Number),
                details = map["details"] as String
            ).apply {
                id = document.id
                timestamp = map["timestamp"] as Timestamp
            }
        }

        return getAnyModelsList(
            "supplier_items", coroutineScope, lastDocumentSnapshot,
            howToConvert, onComplete, "timestamp" to Query.Direction.ASCENDING
        )
    }

    fun getSuppliersList(
        coroutineScope: CoroutineScope, lastDocumentSnapshot: DocumentSnapshot?,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit
    ) {


        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            Supplier(
                name = map["name"] as String,
                dueAmount = Converters.numberToDouble(map["due_amount"] as Number),
                phone = map["phone"] as String,
                email = map["email"] as String,
                note = map["note"] as String,
                paymentDetails = map["payment_details"] as String,
                profileImageUrl = map["profile_image_url"] as String
            ).apply {
                id = document.id
                timestamp = map["timestamp"] as Timestamp
            }
        }

        return getAnyModelsList(
            "suppliers", coroutineScope, lastDocumentSnapshot,
            howToConvert, onComplete, "timestamp" to Query.Direction.ASCENDING
        )

    }

    fun getSupplierPaymentsList(
        coroutineScope: CoroutineScope, lastDocumentSnapshot: DocumentSnapshot?,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            SupplierPayment(
                amount = Converters.numberToDouble(map["amount"] as Number),
                year = Converters.numberToInt(map["year"] as Number),
                month = Converters.numberToInt(map["month"] as Number),
                date = Converters.numberToInt(map["day"] as Number),
                hour = Converters.numberToInt(map["hour"] as Number),
                minute = Converters.numberToInt(map["minute"] as Number),
                note = map["note"] as String,
                supplierId = map["supplier_id"] as String,
                supplierName = map["supplier_name"] as String
            ).apply {
                id = document.id
                timestamp = map["timestamp"] as Timestamp
            }
        }

        return getAnyModelsList(
            "supplier_payments", coroutineScope, lastDocumentSnapshot,
            howToConvert, onComplete, "timestamp" to Query.Direction.ASCENDING
        )
    }

    fun addUpdateSupplier(
        supplier: Supplier, action: String, onComplete: (Int, String) -> Unit
    ) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val suppliersCol = this.usersCol.document(this.currentUser.uid).collection("suppliers")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        val supplierDoc = mutableMapOf<String, Any>(
            "name" to supplier.name,
            "due_amount" to supplier.dueAmount,
            "phone" to supplier.phone,
            "email" to supplier.email,
            "note" to supplier.note,
            "payment_details" to supplier.paymentDetails,
            "profile_image_url" to supplier.profileImageUrl
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierDoc.put("timestamp", FieldValue.serverTimestamp())
                it.set(suppliersCol.document(), supplierDoc)
                it.update(
                    valuesCol.document("suppliers_count"),
                    mapOf("value" to FieldValue.increment(1))
                )
                it.update(
                    valuesCol.document("suppliers_due_amount"),
                    mapOf("value" to FieldValue.increment(supplier.dueAmount))
                )
            } else if (action == TO_UPDATE) {
                var prevTotalDueAmount =
                    it.get(valuesCol.document("suppliers_due_amount")).getDouble("value") ?: 0.0
                var prevSupplierDueAmount =
                    it.get(suppliersCol.document(supplier.id)).getDouble("due_amount") ?: 0.0

                val newTotalDueAmount: Double? =
                    prevTotalDueAmount - prevSupplierDueAmount + supplier.dueAmount

                it.update(suppliersCol.document(supplier.id), supplierDoc)
                it.update(
                    valuesCol.document("suppliers_due_amount"),
                    mapOf("value" to newTotalDueAmount)
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                Log.d("list", it.exception?.message.toString())
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun getNumberOfSuppliers(onComplete: (Int, Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, 0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        valuesCol.document("suppliers_count").get().addOnCompleteListener {
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

    fun getDueAmountToSuppliers(onComplete: (Int, Double, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, 0.0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

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

    fun deleteSupplier(supplier: Supplier, onComplete: (Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val batch = usersCol.firestore.batch()

        val suppliersCol = this.usersCol.document(this.currentUser.uid).collection("suppliers")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        batch.delete(suppliersCol.document(supplier.id))
        batch.update(
            valuesCol.document("suppliers_count"), mapOf("value" to FieldValue.increment(-1))
        )

        batch.update(
            valuesCol.document("suppliers_due_amount"),
            mapOf("value" to FieldValue.increment(-supplier.dueAmount))
        )

        batch.commit().addOnCompleteListener {

            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "User deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }

        }
    }

    fun addSupplierItem(
        supplierItem: SupplierItem, onComplete: (Int, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val batch = this.usersCol.firestore.batch()

        val supplierItemsCol =
            this.usersCol.document(this.currentUser.uid).collection("supplier_items")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        val supplierItemDoc = mapOf(
            "name" to supplierItem.name,
            "price" to supplierItem.price,
            "details" to supplierItem.details,
            "timestamp" to FieldValue.serverTimestamp()
        )

        batch.set(supplierItemsCol.document(), supplierItemDoc)
        batch.update(
            valuesCol.document("supplier_items_count"), mapOf("value" to FieldValue.increment(1))
        )

        batch.commit().addOnCompleteListener {
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

        val batch = this.usersCol.firestore.batch()

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid).collection("supplier_payments")

        val supplierPaymentDoc = mutableMapOf<String, Any>(
            "amount" to supplierPayment.amount,
            "year" to supplierPayment.year,
            "month" to supplierPayment.month,
            "day" to supplierPayment.date,
            "hour" to supplierPayment.hour,
            "minute" to supplierPayment.minute,
            "note" to supplierPayment.note,
            "supplier_id" to supplierPayment.supplierId,
            "supplier_name" to supplierPayment.supplierName
        )

        if (action == TO_ADD) {
            supplierPaymentDoc.put("timestamp", FieldValue.serverTimestamp())
            batch.set(supplierPaymentsCol.document(), supplierPaymentDoc)
        } else if (action == TO_UPDATE) {
            batch.update(supplierPaymentsCol.document(supplierPayment.id), supplierPaymentDoc)
        }

        batch.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    private fun createRequiredThings(onComplete: (Int, String) -> Unit) {

        if (currentUser == null) {
            // Perform Login or create account again
            return
        }

        val batch = this.usersCol.firestore.batch()

        // Create 'values' sub collection
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        // Add value fields here
        val map = mapOf("value" to 0)
        batch.set(valuesCol.document("suppliers_count"), map)
        batch.set(valuesCol.document("suppliers_due_amount"), map)
        batch.set(valuesCol.document("supplier_items_count"), map)

        batch.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    private fun getAnyModelsList(
        firebaseCollectionName: String,
        coroutineScope: CoroutineScope,
        lastDocumentSnapshot: DocumentSnapshot?,
        howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit,
        customSorting: Pair<String, Query.Direction>
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), null, KeysAndMessages.USER_NOT_FOUND)
            return
        }

//        Log.d("list", lastDocumentSnapshot.toString())
//        Log.d("list", customSorting.toString())

        val col = this.usersCol.document(this.currentUser.uid).collection(firebaseCollectionName)

        val query = if (lastDocumentSnapshot == null) {
            col.orderBy(customSorting.first, customSorting.second)
        } else {
            col.orderBy(customSorting.first, customSorting.second)
                .startAfter(lastDocumentSnapshot)
        }.limit(10)


        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val modelsList = mutableListOf<Model>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (document in it.result.documents) {

                        val data = document.data

                        if (document.exists() && data != null) {
                            val model: Model = howToConvert(data, document)
                            modelsList.add(model)
                        }
                    }

                    if (it.result.documents.isEmpty()) {
                        // When no more data is there
                        onComplete(Status.SUCCESS, modelsList, null, KeysAndMessages.EMPTY_LIST)
//                        Log.d("doc", "empty")
                    } else {
                        onComplete(
                            Status.SUCCESS,
                            modelsList,
                            it.result.documents.last(),
                            KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                        )
//                        Log.d("doc", it.result.documents.last().toString())
                    }
                }

            } else {

                onComplete(Status.FAILED, mutableListOf(), null, it.exception?.message.toString())

            }

        }
    }

}