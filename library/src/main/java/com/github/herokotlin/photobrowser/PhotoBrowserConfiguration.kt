package com.github.herokotlin.photobrowser

import android.graphics.drawable.Drawable
import android.widget.ImageView

interface PhotoBrowserConfiguration {

    fun load(imageView: ImageView, url: String, onLoadStart: (Boolean) -> Unit, onLoadProgress: (Float, Float) -> Unit, onLoadEnd: (Boolean) -> Unit)

    fun isLoaded(url: String): Boolean

    fun save(drawable: Drawable): Boolean

}