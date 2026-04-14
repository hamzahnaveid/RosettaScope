package com.example.rosettascope.helpers

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class LockableLinearLayoutManager(
    context: Context
) : LinearLayoutManager(context, HORIZONTAL, false) {

    var isScrollEnabled = false

    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled
    }
}