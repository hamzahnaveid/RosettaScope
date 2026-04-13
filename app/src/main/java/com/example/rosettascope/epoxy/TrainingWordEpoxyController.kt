package com.example.rosettascope.epoxy

import com.airbnb.epoxy.Typed4EpoxyController

class TrainingWordEpoxyController: Typed4EpoxyController<List<String>, Map<String, Double>, Set<String>, (String, Boolean) -> Boolean>() {
    override fun buildModels(
        data1: List<String>?,
        data2: Map<String, Double>?,
        data3: Set<String>?,
        data4: (String, Boolean) -> Boolean
    ) {
        data1!!.forEach { word ->
            TrainingWordEpoxyModel(
                word,
                data2!!,
                data3!!.contains(word),
                data4
            )
                .id(word)
                .addTo(this)
        }
    }
}