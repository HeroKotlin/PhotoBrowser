package com.github.herokotlin.photobrowser.util

import android.view.View

internal object Util {

    fun showView(view: View) {
        view.visibility = View.VISIBLE
    }

    fun hideView(view: View) {
        view.visibility = View.GONE
    }

    fun isVisible(view: View): Boolean {
        return view.visibility == View.VISIBLE
    }

}