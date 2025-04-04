package com.sougata.supplysync.login.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sougata.supplysync.MainActivity
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentCreateAccountBinding
import com.sougata.supplysync.login.viewmodels.CreateAccountViewModel
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.Status

class CreateAccountFragment : Fragment() {

    private lateinit var binding: FragmentCreateAccountBinding

    private lateinit var viewModel: CreateAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_account, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[CreateAccountViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this

        this.registerSubscribers()

    }

    private fun registerSubscribers() {
        this.viewModel.accountCreationIndicator.observe(this.viewLifecycleOwner) {
            if (it.second == Status.STARTED) {

                this.binding.apply {

                    createAccountBtn.isClickable = false
                    progressBar.visibility = View.VISIBLE
                    parentLayout.alpha = 0.5F

                }
//                Log.d("MyTag", "Started")

            } else if (it.second == Status.SUCCESS) {

                findNavController().navigate(
                    R.id.action_createAccountFragment_to_emailVerificationFragment
                )


            } else if (it.second == Status.FAILED) {

                this.binding.apply {

                    createAccountBtn.isClickable = true
                    progressBar.visibility = View.GONE
                    parentLayout.alpha = 1F
                }

                Snackbar.make(requireView(), it.second, Snackbar.LENGTH_SHORT).show()

//                Log.d("MyTag", "Failed")

            }
        }
    }

}