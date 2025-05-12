package com.sougata.supplysync.firestore

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.firestore.util.FirestoreNames
import com.sougata.supplysync.firestore.util.HelperRepository
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class AuthRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCol = this.db.collection(FirestoreNames.Col.USERS)

    fun insertUserToFirestore(user: User, onComplete: (Status, String) -> Unit) {

        val userDoc = mapOf(
            User::name.name to user.name,
            User::email.name to user.email,
            User::phone.name to user.phone
        )

        usersCol.document(user.uid).set(userDoc).addOnCompleteListener {
            if (it.isSuccessful) {

                this.createRequiredThings(user) { status, message ->
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

    fun createRequiredThings(user: User, onComplete: (Status, String) -> Unit) {
        val currentUserDoc = this.usersCol.document(user.uid)
        val valuesCol = currentUserDoc.collection(FirestoreNames.Col.VALUES)

        this.usersCol.firestore.runTransaction {

            // Add value fields here
            val map = mapOf(FirestoreNames.ValuesDoc.Fields.VALUE to 0)

            it.set(valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_COUNT), map)
            it.set(valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_DUE_AMOUNT), map)
            it.set(valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIER_ITEMS_COUNT), map)
            it.set(valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_RECEIVE), map)
            it.set(valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_DELIVER), map)
            it.set(valuesCol.document(FirestoreNames.ValuesDoc.CUSTOMERS_COUNT), map)
            it.set(
                valuesCol.document(FirestoreNames.ValuesDoc.RECEIVABLE_AMOUNT_FROM_CUSTOMERS),
                map
            )

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun createAccount(
        user: User, password: String, onComplete: (Status, String) -> Unit
    ) {

        this.auth.createUserWithEmailAndPassword(user.email, password)
            .addOnCompleteListener { createUserTask ->
                if (createUserTask.isSuccessful) {

                    val currentUser = this.auth.currentUser

                    if (currentUser != null) {

                        user.uid = currentUser.uid

                        this.insertUserToFirestore(user) { status, message ->

                            if (status == Status.SUCCESS) {
                                this.sendEmailVerificationLink { status, message ->
                                    onComplete(status, message)
                                }
                            } else if (status == Status.FAILED) {
                                onComplete(status, message)
                            }

                        }

                    } else {
                        onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
                    }

                } else {
                    onComplete(Status.FAILED, createUserTask.exception?.message.toString())
                }
            }

    }

    fun sendEmailVerificationLink(onComplete: (Status, String) -> Unit) {
        val currentUser = this.auth.currentUser

        currentUser?.sendEmailVerification()?.addOnCompleteListener { emailSendTask ->
            if (emailSendTask.isSuccessful) {

                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)

            } else {

                onComplete(Status.FAILED, emailSendTask.exception?.message.toString())

            }
        }
    }

    fun verifyUsersEmail(onComplete: (Status, String) -> Unit) {

        val currentUser = this.auth.currentUser

        currentUser?.reload()?.addOnCompleteListener { userReloadTask ->

            if (userReloadTask.isSuccessful) {

                val newCurrentUser = this.auth.currentUser

                if (newCurrentUser == null) {
                    onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
                    return@addOnCompleteListener
                }

                if (newCurrentUser.isEmailVerified) {

                    onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)

                } else {
                    onComplete(Status.FAILED, KeysAndMessages.EMAIL_IS_NOT_VERIFIED)
                }

            } else {
                onComplete(Status.FAILED, userReloadTask.exception?.message.toString())
            }
        }

    }


    fun loginAccount(
        email: String, password: String, onComplete: (Status, String) -> Unit
    ) {

        this.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)

                } else {

                    onComplete(Status.FAILED, it.exception?.message.toString())

                }

            }
    }

    fun deleteAccount(email: String, password: String, onComplete: (Status, String) -> Unit) {
        val currentUser = this.auth.currentUser

        val authCredential = EmailAuthProvider.getCredential(email, password)

        currentUser?.reauthenticate(authCredential)?.addOnCompleteListener { reAuthTask ->
            if (reAuthTask.isSuccessful) {

                currentUser.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {

                        onComplete(Status.SUCCESS, "Account deleted successfully")

                    } else {
                        onComplete(Status.FAILED, deleteTask.exception?.message.toString())
                    }
                }

            } else {
                onComplete(Status.FAILED, reAuthTask.exception?.message.toString())
            }
        }

    }

}