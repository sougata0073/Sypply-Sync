package com.sougata.supplysync.util

import android.content.Context
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.sougata.supplysync.R

class Setups {

    fun setupBarChart(barChart: BarChart, barData: BarData, context: Context): BarChart {
        val bwColor = context.getColor(R.color.bw)
        return barChart.apply {

            data = barData

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(Inputs.getAllDaysNames())
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = bwColor
                textSize = 11f
                granularity = 1f
            }

            axisLeft.apply {
                textSize = 11f
                textColor = bwColor
                gridColor = bwColor
            }

            axisRight.isEnabled = false
            legend.isEnabled = false

            // Zooming
            isDragEnabled = true
            setScaleEnabled(true)

            description.isEnabled = false

            setFitBars(true)
            setVisibleXRangeMaximum(7f)

            animateY(2000)

        }
    }

    fun setupLineChart(lineChart: LineChart, lineData: LineData, context: Context): LineChart {
        val bwColor = context.getColor(R.color.bw)
        return lineChart.apply {
            data = lineData

            xAxis.apply {
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                textColor = bwColor
                textSize = 11f
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(false)
                textSize = 11f
                textColor = bwColor

            }

            axisRight.isEnabled = false
            legend.isEnabled = false

            // Zooming
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            description.isEnabled = false

//            setVisibleXRangeMaximum(5f) // only 5 entries will be visible on x-axis

            animateY(1000)

        }
    }

}