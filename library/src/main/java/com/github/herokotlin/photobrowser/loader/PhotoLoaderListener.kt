package com.github.herokotlin.photobrowser.loader

interface PhotoLoaderListener {

    fun onLoadStart(hasProgress: Boolean) {

    }

    fun onLoadProgress(loaded: Float, total: Float) {

    }

    fun onLoadError() {

    }

    fun onLoadSuccess() {

    }

}