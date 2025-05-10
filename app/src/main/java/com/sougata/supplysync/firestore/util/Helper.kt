package com.sougata.supplysync.firestore.util

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlin.math.floor

class Helper {

    private val currentUser = Firebase.auth.currentUser!!
    private val db = Firebase.firestore
    private val usersCol = this.db.collection(FieldNames.UsersCol.SELF_NAME)

    fun insertUserToFirestore(user: User, onComplete: (Status, String) -> Unit) {

        val userDoc = mapOf(
            FieldNames.UsersCol.NAME to user.name,
            FieldNames.UsersCol.EMAIL to user.email,
            FieldNames.UsersCol.PHONE to user.phone
        )

        usersCol.document(user.uid).set(userDoc).addOnCompleteListener {
            if (it.isSuccessful) {

                this.createRequiredThings { status, message ->
                    if (status == Status.SUCCESS) {
                        onComplete(Status.SUCCESS, "User successfully added")
                    } else if (status == Status.FAILED) {
                        onComplete(Status.FAILED, message)
                    }
                }

            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun getAnyModelsList(
        firebaseCollectionName: String,
        lastDocumentSnapshot: DocumentSnapshot?,
        customSorting: Pair<String, Query.Direction>,
        limit: Long,
        howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {

        val col = this.usersCol.document(this.currentUser.uid).collection(firebaseCollectionName)

        var query = if (lastDocumentSnapshot == null) {
            col.orderBy(customSorting.first, customSorting.second)
        } else {
            col.orderBy(customSorting.first, customSorting.second).startAfter(lastDocumentSnapshot)
        }

        val querySnapshot = if (limit == -1L) {
            query.get()
        } else {
            query.limit(limit).get()
        }

        querySnapshot.addOnCompleteListener {

            if (it.isSuccessful) {

                val modelsList = mutableListOf<Model>()

                for (document in it.result.documents) {

                    val data = document.data

                    if (data != null && document.exists()) {
                        val model: Model = howToConvert(data, document)
                        modelsList.add(model)
                    }
                }

                if (it.result.documents.isEmpty()) {
                    // When no more data is there
                    onComplete(Status.SUCCESS, modelsList, null, KeysAndMessages.EMPTY_LIST)
                } else {
                    onComplete(
                        Status.SUCCESS,
                        modelsList,
                        it.result.documents.last(),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }
            } else {
                onComplete(
                    Status.FAILED, null, lastDocumentSnapshot, it.exception?.message.toString()
                )
            }
        }
    }

    fun searchInAnyModelsList(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        firebaseCollectionName: String,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit,
    ) {

        val col = this.usersCol.document(this.currentUser.uid).collection(firebaseCollectionName)

        var query = if (queryDataType == FirestoreFieldDataType.NUMBER) {
            val lowerBound = floor(searchQuery.toDouble())
            val upperBound = lowerBound + 1

            col.whereGreaterThanOrEqualTo(searchField, lowerBound)
                .whereLessThanOrEqualTo(searchField, upperBound)

        } else {
            col.orderBy(searchField).startAt(searchQuery).endAt(searchQuery + "\uf8ff")
        }.limit(limit)

        if (lastDocumentSnapshot != null) {
            query = query.startAfter(lastDocumentSnapshot)
        }

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val modelsList = mutableListOf<Model>()

                for (document in it.result.documents) {

                    val data = document.data

                    if (data != null && document.exists()) {
                        val model: Model = howToConvert(data, document)
                        modelsList.add(model)
                    }
                }

                if (it.result.documents.isEmpty()) {
                    // When no more data is there
                    onComplete(Status.SUCCESS, modelsList, null, KeysAndMessages.EMPTY_LIST)
                } else {
                    onComplete(
                        Status.SUCCESS,
                        modelsList,
                        it.result.documents.last(),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(
                    Status.FAILED, null, lastDocumentSnapshot, it.exception?.message.toString()
                )
            }

        }
    }

    fun getAnyValueFromValuesCol(
        documentName: String,
        fieldDataType: FirestoreFieldDataType,
        onComplete: (Status, Any, String) -> Unit
    ) {
        val valuesCol =
            this.usersCol.document(this.currentUser.uid).collection(FieldNames.ValuesCol.SELF_NAME)

        valuesCol.document(documentName).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val result = when (fieldDataType) {
                    FirestoreFieldDataType.STRING -> {
                        it.result[FieldNames.Commons.VALUE] as? String
                    }

                    FirestoreFieldDataType.NUMBER -> {
                        it.result[FieldNames.Commons.VALUE] as? Number
                    }

                    FirestoreFieldDataType.TIMESTAMP -> {
                        it.result[FieldNames.Commons.VALUE] as? Timestamp
                    }

                    FirestoreFieldDataType.BOOLEAN -> {
                        it.result[FieldNames.Commons.VALUE] as? Boolean
                    }
                }
                if (result != null) {
                    onComplete(
                        Status.SUCCESS, result, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, "-1", KeysAndMessages.EMPTY_LIST)
                }
            } else {
                onComplete(Status.FAILED, "-1", it.exception?.message.toString())
            }
        }
    }

    fun createRequiredThings(onComplete: (Status, String) -> Unit) {

        val valuesCol =
            this.usersCol.document(this.currentUser.uid).collection(FieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {

            // Add value fields here
            val map = mapOf(FieldNames.Commons.VALUE to 0)

            it.set(valuesCol.document(FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME), map)
            it.set(valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME), map)
            it.set(valuesCol.document(FieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME), map)
            it.set(valuesCol.document(FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME), map)
            it.set(valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME), map)
            it.set(valuesCol.document(FieldNames.ValuesCol.CustomersCountDoc.SELF_NAME), map)
            it.set(valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME), map)

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }
}