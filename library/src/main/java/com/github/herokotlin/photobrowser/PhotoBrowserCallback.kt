package com.github.herokotlin.photobrowser

import com.github.herokotlin.photobrowser.model.Photo

interface PhotoBrowserCallback {

    fun onTap(photo: Photo) {

    }

    fun onLongPress(photo: Photo) {

    }

    fun onSave(photo: Photo, success: Boolean) {

    }

}