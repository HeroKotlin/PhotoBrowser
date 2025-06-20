package com.github.herokotlin.photobrowser

import android.content.Context
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.PagerAdapter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.github.herokotlin.photobrowser.databinding.PhotoBrowserBinding
import com.github.herokotlin.photobrowser.model.Photo
import com.github.herokotlin.photobrowser.util.Util
import com.github.herokotlin.photobrowser.view.PhotoPage

open class PhotoBrowser: RelativeLayout {

    companion object {

        var DEFAULT_PAGE_MARGIN = 30

        var DEFAULT_OFFSCREEN_PAGE_LIMIT = 2

    }

    lateinit var binding: PhotoBrowserBinding

    lateinit var configuration: PhotoBrowserConfiguration

    lateinit var callback: PhotoBrowserCallback

    var indicator = IndicatorType.NONE

        set(value) {
            field = value
            when (value) {
                IndicatorType.DOT -> {
                    Util.showView(binding.dotIndicator)
                    Util.hideView(binding.numberIndicator)
                }
                IndicatorType.NUMBER -> {
                    Util.showView(binding.numberIndicator)
                    Util.hideView(binding.dotIndicator)
                }
                else -> {
                    Util.hideView(binding.dotIndicator)
                    Util.hideView(binding.numberIndicator)
                }
            }
        }

    var pageMargin = 0

        set(value) {
            if (field == value) {
                return
            }
            field = value
            binding.pager.pageMargin = value
        }

    var offscreenPageLimit = 0

        set(value) {
            if (field == value) {
                return
            }
            field = value
            binding.pager.offscreenPageLimit = value
        }

    var photos = listOf<Photo>()

        set(value) {

            field = value

            val count = value.size

            binding.dotIndicator.count = count
            binding.numberIndicator.count = count

            isDataDirty = true
            refresh()

        }

    var index = -1

        set(value) {

            if (field == value) {
                return
            }
            field = value

            binding.dotIndicator.index = value
            binding.numberIndicator.index = value

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

        binding = PhotoBrowserBinding.inflate(LayoutInflater.from(context), this, true)

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



        binding.pager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {

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
                if (isPageScrolling) {
                    index = position + if (positionOffset >= 0.5) 1 else 0
                }
            }

            override fun onPageSelected(position: Int) {
                // 触发此接口，可认为即将滑到目标页
                // 因为后续还会触发几次 onPageScrolled，可在此先把 isPageScrolling 改为 false，避免再触发 onPageScrolled
                isPageScrolling = false
                if (index == position) {
                    refresh()
                }
                else {
                    index = position
                }
            }

        })

        val onPageUpdate = { photo: Photo ->
            if (isCurrentPhoto(photo)) {
                updateStatus(photo)
            }
        }

        adapter = object: PagerAdapter() {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = PhotoPage(context, binding.pager, configuration, photos[position])
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

        binding.pager.adapter = adapter

        binding.rawButton.setOnClickListener {
            currentPage.loadRawPhoto()
        }

        binding.saveButton.setOnClickListener {
            callback.onSavePress(photos[index], index)
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDataDirty = true
        refresh()
    }

    fun saveImage() {

        Util.hideView(binding.saveButton)

        // 让外部决定图片保存在哪
        val success = configuration.save(currentPage.photo.currentUrl, currentPage.binding.photoView.drawable)
        if (!success) {
            Util.showView(binding.saveButton)
        }

        callback.onSave(currentPage.photo, index, success)

    }

    fun decodeQRCode(callback: (String) -> Unit) {

        currentPage.decodeQRCode(callback)

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
            if (binding.pager.currentItem != index) {
                binding.pager.currentItem = index
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
            Util.hideView(binding.rawButton)
            Util.hideView(binding.saveButton)
            hideIndicator()
        }
        else {
            if (photo.isRawButtonVisible) {
                Util.showView(binding.rawButton)
            }
            else {
                Util.hideView(binding.rawButton)
            }
            if (photo.isSaveButtonVisible) {
                Util.showView(binding.saveButton)
            }
            else {
                Util.hideView(binding.saveButton)
            }
            showIndicator()
        }
    }

    private fun showIndicator() {
        when (indicator) {
            IndicatorType.DOT -> {
                Util.showView(binding.dotIndicator)
                binding.dotIndicator.invalidate()
            }
            IndicatorType.NUMBER -> {
                Util.showView(binding.numberIndicator)
                binding.numberIndicator.invalidate()
            }
            else -> {

            }
        }
    }

    private fun hideIndicator() {
        when (indicator) {
            IndicatorType.DOT -> {
                Util.hideView(binding.dotIndicator)
            }
            IndicatorType.NUMBER -> {
                Util.hideView(binding.numberIndicator)
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