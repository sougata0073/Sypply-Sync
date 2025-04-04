package com.sougata.supplysync.login.fragments

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.MainActivity
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentEmailVerificationBinding
import com.sougata.supplysync.login.viewmodels.EmailVerificationViewModel
import com.sougata.supplysync.util.Status
import java.util.Locale

class EmailVerificationFragment : Fragment() {

    private lateinit var binding: FragmentEmailVerificationBinding

    private lateinit var viewModel: EmailVerificationViewModel

    private var countDownTimer: CountDownTimer? = null

    private val startTime: Long = 60000 // 60 seconds
    private val interval: Long = 1000 // 1 second

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_email_verification,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[EmailVerificationViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this.viewLifecycleOwner

        this.registerSubscribers()
    }

    private fun registerSubscribers() {
        this.viewModel.emailVerifiedIndicator.observe(this.viewLifecycleOwner) {

            if (it.first == Status.STARTED) {

                this.binding.apply {

                    whenVerifiedBtn.isClickable = false
                    sendLinkAgainBtn.isClickable = false
                    progressBar.visibility = View.VISIBLE
                    parentLayout.alpha = 0.5F

                }

            } else if (it.first == Status.SUCCESS) {

                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()

            } else if (it.first == Status.FAILED) {

                this.binding.apply {

                    whenVerifiedBtn.isClickable = true
                    sendLinkAgainBtn.isClickable = true
                    progressBar.visibility = View.GONE
                    parentLayout.alpha = 1F
                }

                Snackbar.make(requireView(), it.second, Snackbar.LENGTH_SHORT).show()

            }

        }

        this.viewModel.emailAgainSendIndicator.observe(this.viewLifecycleOwner) {

            if (it.first == Status.STARTED) {

                this.binding.apply {
                    sendLinkAgainBtn.isClickable = false
                }

            } else if (it.first == Status.SUCCESS) {

                this.binding.apply {
                    sendLinkAgainBtn.alpha = 0.5f
                    timer.visibility = View.VISIBLE
                }

                this.startCountDownTimer()

                Snackbar.make(requireView(), "Email sent", Snackbar.LENGTH_SHORT).show()

            } else if (it.first == Status.FAILED) {

                this.countDownTimer?.cancel()

                this.binding.apply {
                    sendLinkAgainBtn.isClickable = true
                    sendLinkAgainBtn.alpha = 1f
                    timer.visibility = View.GONE
                }

                Snackbar.make(requireView(), it.second, Snackbar.LENGTH_SHORT).show()

            }

        }
    }

    private fun startCountDownTimer() {

        val timer = this.binding.timer

        this.countDownTimer = object : CountDownTimer(this.startTime, this.interval) {
            // 'millisUntilFinished' gives the remaining time in milliseconds
            override fun onTick(millisUntilFinished: Long) {
                val timeToShowInSeconds =
                    millisUntilFinished / 1000 // divided by 1000 to convert to seconds

                timer.text = String.format(
                    Locale.getDefault(),
                    "Wait $timeToShowInSeconds seconds to send link again"
                )

            }

            override fun onFinish() {

                binding.apply {
                    this.sendLinkAgainBtn.isClickable = true
                    this.sendLinkAgainBtn.alpha = 1f
                    this.timer.visibility = View.GONE
                }
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        this.countDownTimer?.cancel()
    }

}