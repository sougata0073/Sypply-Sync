package com.sougata.supplysync.home.viewmodels

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sougata.supplysync.R

class HomeFragmentViewModel : ViewModel() {

    fun getBarChartData(context: Context): BarData {
        val list = listOf(
            BarEntry(0f, 10f), BarEntry(1f, 20f), BarEntry(2f, 30f), BarEntry(3f, 40f),
            BarEntry(4f, 50f), BarEntry(5f, 60f), BarEntry(6f, 70f)
        )

        val barDataSet = BarDataSet(list, "Amount").apply {
            color = context.getColor(R.color.primary_color)
            valueTextColor = context.getColor(R.color.bw)
            valueTextSize = 11f
        }

        return BarData().apply { addDataSet(barDataSet) }
    }

    fun getLineChartData(context: Context): LineData {

        val list = listOf(
            Entry(0f, 10f), Entry(1f, 20f), Entry(2f, 5f), Entry(3f, 15f),
            Entry(4f, 50f), Entry(5f, 30f), Entry(6f, 25f), Entry(7f, 40f),
            Entry(8f, 69f), Entry(9f, 75f), Entry(10f, 50f), Entry(11f, 60f),
            Entry(12f, 70f), Entry(13f, 80f), Entry(14f, 90f), Entry(15f, 100f)
        )

        val lineDataSet = LineDataSet(list, "Amount").apply {
            color = context.getColor(R.color.primary_color)
            setDrawFilled(true)
            fillColor = context.getColor(R.color.primary_color)
            setDrawCircles(false)
            setDrawValues(false)
            valueTextColor = context.getColor(R.color.bw)
            valueTextSize = 11f
            mode = LineDataSet.Mode.CUBIC_BEZIER

            fillDrawable = AppCompatResources.getDrawable(context, R.drawable.line_chart_gradient_bg)
        }

        return LineData().apply { addDataSet(lineDataSet) }

    }

}