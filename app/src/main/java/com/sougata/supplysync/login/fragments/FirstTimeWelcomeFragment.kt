package com.sougata.supplysync.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentFirstTimeWelcomeBinding
import com.sougata.supplysync.login.viewmodels.FirstTimeWelcomeViewModel

class FirstTimeWelcomeFragment : Fragment() {

    private var _binding: FragmentFirstTimeWelcomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FirstTimeWelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this._binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_first_time_welcome,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[FirstTimeWelcomeViewModel::class.java]

        this.binding.viewModel = this.viewModel

        this.binding.lifecycleOwner = this

    }

    override fun onDestroyView() {
        super.onDestroyView()

        this._binding = null
    }

}