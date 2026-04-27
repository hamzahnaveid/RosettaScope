package com.example.rosettascope.epoxy

import com.airbnb.epoxy.Typed3EpoxyController

class WordEpoxyController: Typed3EpoxyController<List<String>, Map<String, Double>, String>() {
    override fun buildModels(
        data1: List<String>?,
        data2: Map<String, Double>?,
        data3: String
    ) {
        data1!!.forEachIndexed { index, word ->
            WordEpoxyModel(word, data2!!, data3)
                .id(index)
                .addTo(this)
        }
    }
}