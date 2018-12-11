package com.github.herokotlin.photobrowser

interface PhotoBrowserCallback {

    fun onTap(x: Float, y: Float) {

    }

    fun onLongPress(x: Float, y: Float) {

    }

    fun onDownloadSuccess() {

    }

    fun onDownloadFailure() {

    }

}