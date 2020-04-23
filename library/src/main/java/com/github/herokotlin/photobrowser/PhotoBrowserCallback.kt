package com.github.herokotlin.photobrowser

import com.github.herokotlin.photobrowser.model.Photo

interface PhotoBrowserCallback {

    fun onChange(photo: Photo, index: Int) {

    }

    fun onTap(photo: Photo, index: Int) {

    }

    fun onLongPress(photo: Photo, index: Int) {

    }

    fun onSavePress(photo: Photo, index: Int) {

    }

    fun onSave(photo: Photo, index: Int, success: Boolean) {

    }

}