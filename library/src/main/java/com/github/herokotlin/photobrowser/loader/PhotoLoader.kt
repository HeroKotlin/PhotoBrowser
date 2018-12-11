package com.github.herokotlin.photobrowser.loader

import android.widget.ImageView
import com.github.herokotlin.photobrowser.model.Photo

interface PhotoLoader {

    fun load(imageView: ImageView, url: String, photo: Photo, listener: PhotoLoaderListener)

    fun isLoaded(url: String, callback: (isLoaded: Boolean) -> Unit)

}