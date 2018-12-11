package com.github.herokotlin.photobrowser

import android.widget.ImageView

interface PhotoLoader {

    fun load(imageView: ImageView, url: String, photo: PhotoModel, onLoadStart: (Boolean) -> Unit, onLoadProgress: (Float, Float) -> Unit, onLoadEnd: (Boolean) -> Unit)

    fun isLoaded(url: String, callback: (isLoaded: Boolean) -> Unit)

}