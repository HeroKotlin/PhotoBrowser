package com.github.herokotlin.photobrowser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.github.herokotlin.photoview.PhotoViewCallback
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_browser_page.view.*

class PhotoPage(context: Context, val photoViewPager: PhotoViewPager, val photoLoader: PhotoLoader, val page: Int, val photo: PhotoModel, val callback: PhotoBrowserCallback) : RelativeLayout(context) {

    private var hasRawUrl = false

    init {

        LayoutInflater.from(context).inflate(R.layout.photo_browser_page, this)

        val rawUrl = photo.rawUrl
        if (rawUrl != "") {
            hasRawUrl = true

            photoLoader.isLoaded(rawUrl) { isLoaded ->
                if (isLoaded) {
                    loadPhoto(rawUrl)
                }
                else {
                    loadPhoto(photo.highQualityUrl)
                    rawButton.setOnClickListener {
                        loadPhoto(rawUrl)
                    }
                }
            }
        }
        else {
            loadPhoto(photo.highQualityUrl)
        }

        // 图片可拖拽的方向
        var draggableDirection = PhotoView.DIRECTION_VERTICAL

        // 图片弹簧效果的方向
        var bounceDirection = PhotoView.DIRECTION_VERTICAL

        var pagingEnabled = true

        val firstPage = photoViewPager.getFirstPage()
        val lastPage = photoViewPager.getLastPage()

        // 只有一页可以随意拖拽
        if (firstPage != lastPage) {
            if (page == firstPage) {
                draggableDirection = draggableDirection or PhotoView.DIRECTION_RIGHT
                bounceDirection = bounceDirection or PhotoView.DIRECTION_LEFT
            }
            else if (page == lastPage) {
                draggableDirection = draggableDirection or PhotoView.DIRECTION_LEFT
                bounceDirection = bounceDirection or PhotoView.DIRECTION_RIGHT
            }
        }
        else {
            draggableDirection = PhotoView.DIRECTION_ALL
            bounceDirection = PhotoView.DIRECTION_ALL
            pagingEnabled = false
        }

        // 设置拖拽和弹簧方向
        photoView.draggableDirection = draggableDirection
        photoView.bounceDirection = bounceDirection

        photoViewPager.pagingEnabled = pagingEnabled

        // 图片和 ViewPager 的交互

        var rawVisible = false

        photoView.callback = object: PhotoViewCallback {

            override fun onReset() {
                photoViewPager.pagingEnabled = pagingEnabled
            }

            override fun onScale(scale: Float) {
                if (Math.abs(scale - 1) > 0.1) {
                    photoView.draggableDirection = PhotoView.DIRECTION_ALL
                    photoView.bounceDirection = PhotoView.DIRECTION_ALL
                    photoViewPager.pagingEnabled = false
                }
                else {
                    photoView.draggableDirection = draggableDirection
                    photoView.bounceDirection = bounceDirection
                    photoViewPager.pagingEnabled = pagingEnabled
                }
            }

            override fun onLongPress(x: Float, y: Float) {
                callback.onLongPress(x, y)
            }

            override fun onTap(x: Float, y: Float) {
                callback.onTap(x, y)
            }

            override fun onDragStart() {
                rawVisible = isVisible(rawButton)
                if (rawVisible) {
                    hideView(rawButton)
                }
            }

            override fun onDragEnd() {
                if (rawVisible) {
                    showView(rawButton)
                }
            }

        }

        downloadButton.setOnClickListener {
            downloadPhoto()
        }

    }

    private fun loadPhoto(url: String) {
        photoLoader.load(photoView, url, photo,
            { hasProgress: Boolean ->
                if (hasProgress) {
                    showView(spinnerView)
                }
                else {
                    showView(progressView)
                }
                if (url == photo.rawUrl) {
                    hideView(rawButton)
                }
                hideView(downloadButton)
            },
            { loaded: Float, total: Float ->
                spinnerView.value = loaded / total
            },
            { success: Boolean ->
                if (success) {
                    hideView(spinnerView)
                    hideView(progressView)
                    if (hasRawUrl) {
                        if (url == photo.rawUrl) {
                            hideView(rawButton)
                        }
                        else if (url == photo.highQualityUrl) {
                            showView(rawButton)
                        }
                    }
                    showView(downloadButton)
                }
                else {
                    hideView(spinnerView)
                    hideView(progressView)
                    if (hasRawUrl && url == photo.rawUrl) {
                        showView(rawButton)
                    }
                }
            }
        )
    }

    private fun downloadPhoto() {

        hideView(downloadButton)

        val drawable = photoView.drawable
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        val localUrl = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "", "")
        if (localUrl != null) {
            callback.onDownloadSuccess()
        }
        else {
            showView(downloadButton)
            callback.onDownloadFailure()
        }
    }

    private fun showView(view: View) {
        view.visibility = View.VISIBLE
    }

    private fun hideView(view: View) {
        view.visibility = View.GONE
    }

    private fun isVisible(view: View): Boolean {
        return view.visibility == View.VISIBLE
    }


}
