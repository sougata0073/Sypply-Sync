package com.sougata.supplysync.firestore

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.firestore.util.Helper
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class AuthRepository {

    private val auth = Firebase.auth
    private val helper = Helper()

    fun createAccount(
        user: User, password: String, onComplete: (Status, String) -> Unit
    ) {

        this.auth.createUserWithEmailAndPassword(user.email, password)
            .addOnCompleteListener { createUserTask ->
                if (createUserTask.isSuccessful) {

                    val currentUser = this.auth.currentUser

                    if (currentUser != null) {

                        user.uid = currentUser.uid

                        this.helper.insertUserToFirestore(user) { status, message ->

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