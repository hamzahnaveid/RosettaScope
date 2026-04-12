package com.example.rosettascope.epoxy

import com.airbnb.epoxy.Typed2EpoxyController

class TrainingWordEpoxyController: Typed2EpoxyController<List<String>, Map<String, Double>>() {
    override fun buildModels(
        data1: List<String>?,
        data2: Map<String, Double>?
    ) {
        data1!!.forEachIndexed { index, word ->
            TrainingWordEpoxyModel(word, data2!!)
                .id(index)
                .addTo(this)
        }
    }
}