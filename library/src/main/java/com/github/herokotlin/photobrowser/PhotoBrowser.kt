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
import com.github.herokotlin.photobrowser.loader.PhotoLoader
import com.github.herokotlin.photobrowser.model.Photo
import kotlinx.android.synthetic.main.photo_browser.view.*

class PhotoBrowser: RelativeLayout {

    companion object {

        var DEFAULT_PAGE_MARGIN = 14

        var DEFAULT_OFFSCREEN_PAGE_LIMIT = 1

        lateinit var loader: PhotoLoader

    }

    lateinit var callback: PhotoBrowserCallback

    var indicator: String = ""

        set(value) {
            field = value
            when (value) {
                "dot" -> {
                    dotIndicator.visibility = View.VISIBLE
                    numberIndicator.visibility = View.GONE
                }
                "number" -> {
                    dotIndicator.visibility = View.GONE
                    numberIndicator.visibility = View.VISIBLE
                }
                else -> {
                    dotIndicator.visibility = View.GONE
                    numberIndicator.visibility = View.GONE
                }
            }
        }

    var pageMargin = DEFAULT_PAGE_MARGIN

        set(value) {
            if (field == value) {
                return
            }
            field = value
            pager.pageMargin = value
        }

    var offscreenPageLimit = DEFAULT_OFFSCREEN_PAGE_LIMIT

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
            if (value.size > 1) {
                dotIndicator.count = value.size
                numberIndicator.count = value.size
                dotIndicator.invalidate()
                numberIndicator.invalidate()
                indicator = indicator
            }
            else {
                dotIndicator.visibility = View.GONE
                numberIndicator.visibility = View.GONE
            }
            pager.adapter?.notifyDataSetChanged()
        }

    var index = -1

        set(value) {
            if (field == value) {
                return
            }
            field = value
            dotIndicator.index = value
            numberIndicator.index = value
            dotIndicator.invalidate()
            numberIndicator.invalidate()
            pager.currentItem = value
        }

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

        indicator = typedArray.getString(R.styleable.PhotoBrowser_indicator)

        pageMargin = typedArray.getDimensionPixelSize(
            R.styleable.PhotoBrowser_pageMargin,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PAGE_MARGIN.toFloat(), resources.displayMetrics).toInt()
        )

        offscreenPageLimit = typedArray.getInt(R.styleable.PhotoBrowser_offscreenPageLimit, DEFAULT_OFFSCREEN_PAGE_LIMIT)

        typedArray.recycle()



        pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                index = position
            }

        })

        pager.adapter = object: PagerAdapter() {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = PhotoPage(context, pager, loader, position, photos[position], callback)
                container.addView(view)
                return view
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

    }


}