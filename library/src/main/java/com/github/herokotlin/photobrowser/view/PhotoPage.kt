package com.github.herokotlin.photobrowser.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.github.herokotlin.photobrowser.PhotoBrowserConfiguration
import com.github.herokotlin.photobrowser.R
import com.github.herokotlin.photobrowser.model.Photo
import com.github.herokotlin.photobrowser.util.Util
import com.github.herokotlin.photoview.PhotoViewCallback
import com.github.herokotlin.photoview.PhotoView
import kotlinx.android.synthetic.main.photo_browser_page.view.*

internal class PhotoPage(context: Context, val photoViewPager: PhotoViewPager, val configuration: PhotoBrowserConfiguration, val index: Int, val photo: Photo) : RelativeLayout(context) {

    private var loadedUrl = ""

    private var hasRawUrl = false

        get() {
            return photo.rawUrl != "" && photo.rawUrl != photo.highQualityUrl
        }

    var onTap: ((Photo) -> Unit)? = null

    var onLongPress: ((Photo) -> Unit)? = null

    var onScaleChange: ((Photo) -> Unit)? = null

    var onLoadStart: ((Photo) -> Unit)? = null

    var onLoadEnd: ((Photo) -> Unit)? = null

    var onSave: ((Photo, Boolean) -> Unit)? = null

    var onDragStart: ((Photo) -> Unit)? = null

    var onDragEnd: ((Photo) -> Unit)? = null

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
        photoView.callback = object: PhotoViewCallback {

            override fun onReset() {
                photoViewPager.pagingEnabled = pagingEnabled
            }

            override fun onScaleChange(scale: Float) {
                photo.scale = if (scale - 1 > 0.1) scale else 1f
                if (photo.scale > 1) {
                    photoViewPager.pagingEnabled = false
                }
                else {
                    photoViewPager.pagingEnabled = pagingEnabled
                }
                onScaleChange?.invoke(photo)
            }

            override fun onLongPress(x: Float, y: Float) {
                onLongPress?.invoke(photo)
            }

            override fun onTap(x: Float, y: Float) {
                onTap?.invoke(photo)
            }

            override fun onDragStart() {
                photo.isDragging = true
                onDragStart?.invoke(photo)
            }

            override fun onDragEnd() {
                photo.isDragging = false
                onDragEnd?.invoke(photo)
            }

        }

        var url = photo.highQualityUrl

        if (hasRawUrl && (configuration.isLoaded(photo.rawUrl) || photo.isRawPhotoLoaded)) {
            url = photo.rawUrl
        }

        if (url != loadedUrl) {
            loadPhoto(url)
        }

    }

    fun savePhoto() {

        // 让外部决定图片保存在哪
        val success = configuration.save(loadedUrl, photoView.drawable)

        onSave?.invoke(photo, success)

    }

    fun loadRawPhoto() {
        loadPhoto(photo.rawUrl)
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

        if (success) {
            if (hasRawUrl) {
                if (url == photo.highQualityUrl) {
                    photo.isRawButtonVisible = true
                }
                else {
                    photo.isRawPhotoLoaded = url == photo.rawUrl
                    photo.isRawButtonVisible = false
                }
            }
            photo.isSaveButtonVisible = true
            loadedUrl = url
        }
        else if (hasRawUrl && url == photo.rawUrl) {
            photo.isRawButtonVisible = true
        }

        onLoadEnd?.invoke(photo)

    }

}
