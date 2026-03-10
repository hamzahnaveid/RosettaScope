package com.example.rosettascope.helpers

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class LineChartXAxisValueFormatter : IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val epochTime = value.toLong()

        val timeMilliseconds = Date(epochTime)
        val dateTimeFormat: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

        return dateTimeFormat.format(timeMilliseconds)
    }

}