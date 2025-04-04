package com.sougata.supplysync.login

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.ActivityLogInBinding
import com.sougata.supplysync.firebase.AuthenticationRepository
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding

    private var reasonToBeHere: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_log_in)

        this.reasonToBeHere = intent.getStringExtra("reason").toString()

        this.setupIfOnlyEmailIsNotVerified()
    }

    private fun setupIfOnlyEmailIsNotVerified() {
        if (this.reasonToBeHere == KeysAndMessages.EMAIL_IS_NOT_VERIFIED) {

            this.binding.main.alpha = 0.5f
            this.binding.progressBar.visibility = View.VISIBLE

            // Verify email
            val authRepository = AuthenticationRepository()

            authRepository.sendEmailVerificationLink { status, message ->

                if (status == Status.SUCCESS) {

                    this.binding.main.alpha = 1f
                    this.binding.progressBar.visibility = View.GONE

                    val navController =
                        (this.supportFragmentManager.findFragmentById(R.id.navHostLogin) as NavHostFragment).navController

                    navController.navigate(
                        R.id.action_firstTimeWelcomeFragment_to_emailVerificationFragment
                    )

                } else if (status == Status.FAILED) {
                    this.binding.main.alpha = 1f
                    this.binding.progressBar.visibility = View.GONE

                    Snackbar.make(this.binding.root, message, Snackbar.LENGTH_LONG).show()
                }

            }
        }
    }
}