package com.sougata.supplysync.login.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.AuthRepository
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.Validations
import java.util.UUID

class CreateAccountViewModel : ViewModel() {

    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val phone = MutableLiveData("")
    val password = MutableLiveData("")
    val confirmPassword = MutableLiveData("")

    val accountCreationIndicator = MutableLiveData<Triple<User, Status, String>>()


    fun onNextClick(view: View) {

        var name = this.name.value.toString().trim()
        var email = this.email.value.toString().trim()
        var phone = this.phone.value.toString().trim()
        var password = this.password.value.toString().trim()
        var confirmPassword = this.confirmPassword.value.toString().trim()

        val validations = Validations()

        // Name validation
        if (name.isEmpty()) {
            Snackbar.make(view, "Name can't be empty", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Email validation
        if (!validations.validateEmail(email, view)) {
            return
        }

        // Phone number validation
        if (!validations.validatePhoneNumber(phone, view)) {
            return
        }

        // Password validation
        if (!validations.validatePassword(password, view)) {
            return
        } else if (password != confirmPassword) {
            Snackbar.make(view, "Passwords do not match", Snackbar.LENGTH_SHORT).show()
            return
        }

        val user = User(UUID.randomUUID().toString(), Timestamp.now(), name, email, phone)

        // When all validation is passed
        this.accountCreationIndicator.value = Triple(user, Status.STARTED, "")

        val authRepository = AuthRepository()

        authRepository.createAccount(user, password) { status, message ->
            this.accountCreationIndicator.value = Triple(user, status, message)
        }

    }

    fun onLoginClick(view: View) {
        view.findNavController().navigate(R.id.action_createAccountFragment_to_loginFragment)
    }

}