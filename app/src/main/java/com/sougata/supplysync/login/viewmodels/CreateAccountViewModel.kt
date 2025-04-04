package com.sougata.supplysync.login.viewmodels

import android.os.Bundle
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.firebase.AuthenticationRepository
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.Validations

class CreateAccountViewModel : ViewModel(), Observable {

    @Bindable
    val name = MutableLiveData("")

    @Bindable
    val email = MutableLiveData("")

    @Bindable
    val phone = MutableLiveData("")

    @Bindable
    val password = MutableLiveData("")

    @Bindable
    val confirmPassword = MutableLiveData("")

    val accountCreationIndicator = MutableLiveData<Triple<User, Int, String>>()


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

        val user = User(name, email, phone)

        // When all validation is passed
        this.accountCreationIndicator.postValue(Triple(user, Status.STARTED, ""))

        val authRepository = AuthenticationRepository()

        authRepository.createAccount(user, password) { status, message ->
            this.accountCreationIndicator.postValue(Triple(user, status, message))
        }

    }

    fun onLoginClick(view: View) {
        view.findNavController().navigate(R.id.action_createAccountFragment_to_loginFragment)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    }

}