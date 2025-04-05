package com.sougata.supplysync.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sougata.supplysync.MainActivity
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentHomeBinding
import com.sougata.supplysync.home.viewmodels.HomeFragmentViewModel
import com.sougata.supplysync.util.Setups

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var viewModel: HomeFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel = ViewModelProvider(this)[HomeFragmentViewModel::class.java]

        val setups = Setups()

        setups.setupLineChart(
            this.binding.yearsSalesLineChart,
            this.viewModel.getLineChartData(requireContext()), requireContext()
        )

        setups.setupLineChart(
            this.binding.yearsPurchaseLineChart,
            this.viewModel.getLineChartData(requireContext()), requireContext()
        )

        this.registerListeners()
    }

    private fun registerListeners() {
//        this.binding.scrollView.setOnScrollChangeListener(
//            object : View.OnScrollChangeListener {
//
//                private val mainActivity = activity as MainActivity?
//
//                override fun onScrollChange(
//                    v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int
//                ) {
//
////                    Log.d("scroll", "$oldScrollY old")
////                    Log.d("scroll", scrollY.toString())
//
//                    val dy = oldScrollY - scrollY // Positive = scroll up, Negative = scroll down
//
////                    Log.d("scroll", dy.toString())
//
//                    if (dy > 0) {
//                        this.mainActivity?.slideDownBottomNav()
//                    } else if (dy < 0) {
//                        this.mainActivity?.slideUpBottomNav()
//                    }
//                }
//            }
//
//        )
    }

}