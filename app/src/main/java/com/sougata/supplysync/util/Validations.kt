package com.sougata.supplysync.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

class Validations {

    private val emailRegex =
        Regex("^[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}\$")

    private val phoneRegex =
        Regex("^(?:\\+?(\\d{1,3})?\\s?)?(?:\\(?(\\d{3})\\)?[-\\s]?)?(\\d{3})[-\\s]?(\\d{4})$")

    fun validateEmail(email: String, view: View): Boolean {

        if (email.isEmpty()) {
            showSnackBar(view, "Email can't be empty")
            return false
        }

        if (!this.emailRegex.matches(email)) {
            showSnackBar(view, "Enter a valid email")
            return false
        }

        return true
    }

    fun validatePhoneNumber(phone: String, view: View): Boolean {

        if (phone.isEmpty()) {
            showSnackBar(view, "Phone number can't be empty")
            return false
        }

        if (!this.phoneRegex.matches(phone)) {
            showSnackBar(view, "Enter a valid phone number")
            return false
        }

        return true
    }

    fun validatePassword(password: String, view: View): Boolean {
        if (password.isEmpty() || password.length < 6) {
            showSnackBar(view, "Password must be greater than or equals to 6 characters")
            return false
        }

        return true
    }

    private fun showSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

}