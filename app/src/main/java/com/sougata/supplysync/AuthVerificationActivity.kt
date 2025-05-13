package com.sougata.supplysync

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.util.Keys
import com.sougata.supplysync.util.Messages

class AuthVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        installSplashScreen()

        val currentUser = Firebase.auth.currentUser

        if (currentUser == null) {

            val bundle = Bundle().apply {
                putString(Keys.REASON, Messages.USER_NOT_FOUND)
            }

            startActivity(Intent(this, LoginActivity::class.java), bundle)
            finishAffinity()

        } else if (!currentUser.isEmailVerified) {

            val bundle = Bundle().apply {
                putString(Keys.REASON, Messages.EMAIL_IS_NOT_VERIFIED)
            }

            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtras(bundle)
            }

            startActivity(intent)
            finishAffinity()

        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

    }
}