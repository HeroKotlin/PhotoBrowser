package com.github.herokotlin.photobrowser.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.github.herokotlin.photobrowser.PhotoBrowserConfiguration
import com.github.herokotlin.photobrowser.R
import com.github.herokotlin.photobrowser.model.Photo
import com.github.herokotlin.photobrowser.util.Util
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_browser_page.view.*
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import com.github.herokotlin.qrcode.QRCode

internal class PhotoPage(context: Context, private val photoViewPager: PhotoViewPager, private val configuration: PhotoBrowserConfiguration, val photo: Photo) : RelativeLayout(context) {

    var onTap: ((Photo) -> Unit)? = null

    var onLongPress: ((Photo) -> Unit)? = null

    var onScaleChange: ((Photo) -> Unit)? = null

    var onLoadStart: ((Photo) -> Unit)? = null

    var onLoadEnd: ((Photo) -> Unit)? = null

    var onDragStart: ((Photo) -> Unit)? = null

    var onDragEnd: ((Photo) -> Unit)? = null

    private val hasRawUrl: Boolean

        get() {
            return photo.rawUrl != "" && photo.rawUrl != photo.highQualityUrl
        }

    init {

        LayoutInflater.from(context).inflate(R.layout.photo_browser_page, this)

        // 图片可拖拽的方向
        val draggableDirection = PhotoView.DIRECTION_ALL

        // 图片弹簧效果的方向
        val bounceDirection = PhotoView.DIRECTION_ALL

        // 是否能翻页
        val pagingEnabled = photoViewPager.getCount() > 1

        // 设置拖拽和弹簧方向
        photoView.draggableDirection = draggableDirection
        photoView.bounceDirection = bounceDirection

        photoViewPager.pagingEnabled = pagingEnabled

        // 图片和 ViewPager 的交互
        photoView.onReset = {
            photoViewPager.pagingEnabled = pagingEnabled
        }

        photoView.onScaleChange = {
            val scale = photoView.scale / photoView.minScale
            photo.scale = if (scale - 1 > 0.1) scale else 1f
            if (photo.scale > 1) {
                photoViewPager.pagingEnabled = false
            }
            else {
                photoViewPager.pagingEnabled = pagingEnabled
            }
            onScaleChange?.invoke(photo)
        }

        photoView.onLongPress = {
            onLongPress?.invoke(photo)
        }

        photoView.onTap = {
            onTap?.invoke(photo)
        }

        photoView.onDragStart = {
            photo.isDragging = true
            onDragStart?.invoke(photo)
        }

        photoView.onDragEnd = {
            photo.isDragging = false
            onDragEnd?.invoke(photo)
        }

        if (photo.thumbnailUrl.isNotEmpty()
            && photo.thumbnailUrl != photo.highQualityUrl
            && !photo.isHighQualityPhotoLoaded
        ) {
            loadThumbnail()
        }
        else {
            loadHighQuality()
        }

    }

    fun decodeQRCode(callback: (String) -> Unit) {

        val drawable = photoView.drawable
        if (drawable !is BitmapDrawable) {
            return callback("")
        }

        val handler = Handler(Looper.getMainLooper())

        Thread {
            val text = QRCode.decodeQRCode(drawable.bitmap)
            handler.post {
                callback(text)
            }
        }.start()

    }

    fun loadRawPhoto() {
        loadPhoto(photo.rawUrl)
    }

    private fun loadThumbnail() {

        loadPhoto(photo.thumbnailUrl)

    }

    private fun loadHighQuality() {

        if (hasRawUrl) {
            if (photo.isRawPhotoLoaded) {
                loadPhoto(photo.rawUrl)
            }
            else {
                configuration.isLoaded(photo.rawUrl) {
                    if (it) {
                        loadPhoto(photo.rawUrl)
                    }
                    else {
                        loadPhoto(photo.highQualityUrl)
                    }
                }
            }
        }
        else {
            loadPhoto(photo.highQualityUrl)
        }

    }

    private fun loadPhoto(url: String) {
        configuration.load(photoView, url,
            { hasProgress ->
                onLoadStart(url, hasProgress)
            },
            { loaded, total ->
                onLoadProgress(loaded, total)
            },
            { success ->
                onLoadEnd(url, success)
            }
        )
    }

    private fun onLoadStart(url: String, hasProgress: Boolean) {

        if (hasProgress) {
            Util.showView(circleSpinner)
        }
        else {
            Util.showView(normalSpinner)
        }

        if (url == photo.rawUrl) {
            photo.isRawButtonVisible = false
        }
        photo.isSaveButtonVisible = false

        onLoadStart?.invoke(photo)

    }

    private fun onLoadProgress(loaded: Float, total: Float) {

        if (Util.isVisible(circleSpinner)) {
            circleSpinner.value = loaded / total
            circleSpinner.invalidate()
        }

    }

    private fun onLoadEnd(url: String, success: Boolean) {

        if (Util.isVisible(circleSpinner)) {
            Util.hideView(circleSpinner)
        }
        else {
            Util.hideView(normalSpinner)
        }

        photo.currentUrl = url

        if (success) {
            if (photo.thumbnailUrl != photo.highQualityUrl && url == photo.thumbnailUrl) {
                post { loadHighQuality() }
            }
            else {
                if (url == photo.highQualityUrl) {
                    photo.isHighQualityPhotoLoaded = true
                }
                else if (url == photo.rawUrl) {
                    photo.isRawPhotoLoaded = true
                }
                if (hasRawUrl) {
                    photo.isRawButtonVisible = url == photo.highQualityUrl
                }
                photo.isSaveButtonVisible = photo.canSave
            }
        }
        else if (hasRawUrl && url == photo.rawUrl) {
            photo.isRawButtonVisible = true
        }

        onLoadEnd?.invoke(photo)

    }

}
