package com.example.rosettascope.helpers

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.rosettascope.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet

class SparkLineStyle(val context: Context) {

    fun styleChart(lineChart: LineChart) = lineChart.apply {
        axisRight.isEnabled = false
        xAxis.isEnabled = false

        axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 110f
            setDrawGridLines(false)
            setDrawAxisLine(true)
            axisLineColor = ContextCompat.getColor(context, R.color.white)
            textColor = ContextCompat.getColor(context, R.color.white)
        }

        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(false)
        setPinchZoom(false)
        description = null
        legend.isEnabled = false

    }

    fun styleLineDataSet(lineDataSet: LineDataSet) = lineDataSet.apply {
        color = ContextCompat.getColor(context, R.color.rosetta_yellow)
        valueTextColor = ContextCompat.getColor(context, R.color.white)
        setDrawValues(false)
        lineWidth = 3f
        isHighlightEnabled = true
        mode = LineDataSet.Mode.CUBIC_BEZIER

        setDrawFilled(true)
        fillDrawable = ContextCompat.getDrawable(context, R.drawable.bg_spark_line)
    }

}