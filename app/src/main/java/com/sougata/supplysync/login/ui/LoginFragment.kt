package com.sougata.supplysync.login.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.MainActivity
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentLoginBinding
import com.sougata.supplysync.login.viewmodels.LoginViewModel
import com.sougata.supplysync.util.Status

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this._binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this

        this.registerSubscribers()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

    private fun registerSubscribers() {
        this.viewModel.accountLoginIndicator.observe(viewLifecycleOwner) {

            if (it.first == Status.STARTED) {

                this.binding.apply {

                    loginBtn.isClickable = false
                    progressBar.visibility = View.VISIBLE
                    parentLayout.alpha = 0.5F
                }

            } else if (it.first == Status.SUCCESS) {

                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finishAffinity()

//                Log.d("login", "Logged in")

            } else if (it.first == Status.FAILED) {

                this.binding.apply {

                    loginBtn.isClickable = true
                    progressBar.visibility = View.GONE
                    parentLayout.alpha = 1F
                }

                Snackbar.make(requireView(), it.second, Snackbar.LENGTH_SHORT).show()

            }

        }
    }

}