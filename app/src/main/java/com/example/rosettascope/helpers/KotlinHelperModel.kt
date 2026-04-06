package com.example.rosettascope.helpers

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.airbnb.epoxy.EpoxyModel
import kotlin.properties.ReadOnlyProperty

abstract class KotlinHelperModel(
    @LayoutRes private val layoutRes: Int
) : EpoxyModel<View>() {
    private var view: View? = null

    abstract fun bind()

    override fun bind(view: View) {
        this.view = view
        bind()
    }

    override fun unbind(view: View) {
        this.view = null
    }

    override fun getDefaultLayout() = layoutRes

    protected fun <V : View> bind(@IdRes id: Int) =
        ReadOnlyProperty<KotlinHelperModel, V> { _, property ->
            view?.findViewById(id)
                ?: throw IllegalStateException("View ID $id for '${property.name}' not found.")
        }
}