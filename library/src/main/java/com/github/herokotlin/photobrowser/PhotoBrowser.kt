package com.github.herokotlin.photobrowser

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.github.herokotlin.photobrowser.model.Photo
import com.github.herokotlin.photobrowser.util.Util
import com.github.herokotlin.photobrowser.view.PhotoPage
import kotlinx.android.synthetic.main.photo_browser.view.*
import kotlinx.android.synthetic.main.photo_browser_page.view.*

open class PhotoBrowser: RelativeLayout {

    companion object {

        var DEFAULT_PAGE_MARGIN = 30

        var DEFAULT_OFFSCREEN_PAGE_LIMIT = 2

    }

    lateinit var configuration: PhotoBrowserConfiguration

    lateinit var callback: PhotoBrowserCallback

    var indicator = IndicatorType.NONE

        set(value) {
            field = value
            when (value) {
                IndicatorType.DOT -> {
                    Util.showView(dotIndicator)
                    Util.hideView(numberIndicator)
                }
                IndicatorType.NUMBER -> {
                    Util.showView(numberIndicator)
                    Util.hideView(dotIndicator)
                }
                else -> {
                    Util.hideView(dotIndicator)
                    Util.hideView(numberIndicator)
                }
            }
        }

    var pageMargin = 0

        set(value) {
            if (field == value) {
                return
            }
            field = value
            pager.pageMargin = value
        }

    var offscreenPageLimit = 0

        set(value) {
            if (field == value) {
                return
            }
            field = value
            pager.offscreenPageLimit = value
        }

    var photos = listOf<Photo>()

        set(value) {

            field = value

            val count = value.size

            dotIndicator.count = count
            numberIndicator.count = count

            isDataDirty = true
            refresh()

        }

    var index = -1

        set(value) {

            if (field == value) {
                return
            }
            field = value

            dotIndicator.index = value
            numberIndicator.index = value

            if (isPageScrolling) {
                return
            }
            refresh()

        }

    private lateinit var currentPage: PhotoPage

    private lateinit var adapter: PagerAdapter

    private var isPageScrolling = false

    private var isDataDirty = false

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        LayoutInflater.from(context).inflate(R.layout.photo_browser, this)

        val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.PhotoBrowser, defStyle, 0)

        indicator = when (typedArray.getString(R.styleable.PhotoBrowser_indicator)) {
            "dot" -> {
                IndicatorType.DOT
            }
            "number" -> {
                IndicatorType.NUMBER
            }
            else -> {
                IndicatorType.NONE
            }
        }

        pageMargin = typedArray.getDimensionPixelSize(
            R.styleable.PhotoBrowser_pageMargin,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PAGE_MARGIN.toFloat(), resources.displayMetrics).toInt()
        )

        offscreenPageLimit = typedArray.getInt(R.styleable.PhotoBrowser_offscreenPageLimit, DEFAULT_OFFSCREEN_PAGE_LIMIT)

        typedArray.recycle()



        pager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager.SCROLL_STATE_IDLE -> {
                        isPageScrolling = false
                    }
                    ViewPager.SCROLL_STATE_DRAGGING -> {
                        isPageScrolling = true
                    }
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                index = position + if (positionOffset >= 0.5) 1 else 0
            }

        })

        val onPageUpdate = { photo: Photo ->
            if (isCurrentPhoto(photo)) {
                updateStatus(photo)
            }
        }

        adapter = object: PagerAdapter() {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = PhotoPage(context, pager, configuration, photos[position])
                view.onScaleChange = onPageUpdate
                view.onLoadStart = onPageUpdate
                view.onLoadEnd = onPageUpdate
                view.onDragStart = onPageUpdate
                view.onDragEnd = onPageUpdate
                view.onTap = { photo: Photo ->
                    callback.onTap(photo, position)
                }
                view.onLongPress = { photo: Photo ->
                    callback.onLongPress(photo, position)
                }
                container.addView(view)
                return view
            }

            override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
                currentPage = `object` as PhotoPage
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view == `object`
            }

            override fun getCount(): Int {
                return photos.size
            }

        }

        pager.adapter = adapter

        rawButton.setOnClickListener {
            currentPage.loadRawPhoto()
        }

        saveButton.setOnClickListener {
            saveImage()
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDataDirty = true
        refresh()
    }

    fun saveImage() {

        Util.hideView(saveButton)

        // 让外部决定图片保存在哪
        val success = configuration.save(currentPage.loadedUrl, currentPage.photoView.drawable)
        if (!success) {
            Util.showView(saveButton)
        }

        callback.onSave(currentPage.photo, index, success)

    }

    private fun isCurrentPhoto(photo: Photo): Boolean {
        return index == photos.indexOf(photo)
    }

    private fun refresh() {

        if (index >= 0 && index < photos.count()) {
            if (isDataDirty) {
                isDataDirty = false
                adapter.notifyDataSetChanged()
            }
            if (pager.currentItem != index) {
                pager.currentItem = index
            }
            updateStatus(photos[index])
            callback.onChange(photos[index], index)
        }
        else {
            hideIndicator()
        }

    }

    private fun updateStatus(photo: Photo) {
        if (photo.isDragging) {
            Util.hideView(rawButton)
            Util.hideView(saveButton)
            hideIndicator()
        }
        else {
            if (photo.isRawButtonVisible) {
                Util.showView(rawButton)
            }
            else {
                Util.hideView(rawButton)
            }
            if (photo.isSaveButtonVisible) {
                Util.showView(saveButton)
            }
            else {
                Util.hideView(saveButton)
            }
            showIndicator()
        }
    }

    private fun showIndicator() {
        when (indicator) {
            IndicatorType.DOT -> {
                Util.showView(dotIndicator)
                dotIndicator.invalidate()
            }
            IndicatorType.NUMBER -> {
                Util.showView(numberIndicator)
                numberIndicator.invalidate()
            }
            else -> {

            }
        }
    }

    private fun hideIndicator() {
        when (indicator) {
            IndicatorType.DOT -> {
                Util.hideView(dotIndicator)
            }
            IndicatorType.NUMBER -> {
                Util.hideView(numberIndicator)
            }
            else -> {

            }
        }
    }

    enum class IndicatorType {

        DOT,

        NUMBER,

        NONE

    }


}