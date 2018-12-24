package com.github.herokotlin.photobrowser.view

import android.content.Context
import android.graphics.PointF
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

internal class PhotoViewPager: ViewPager {

    var pagingEnabled = false

    private val lastTouchPoint = PointF()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (pagingEnabled && isPaging(event)) {
            return super.onTouchEvent(event)
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (pagingEnabled && isPaging(event)) {
            return super.onInterceptTouchEvent(event)
        }
        return false
    }

    fun getCount(): Int {
        val pagerAdapter = adapter
        if (pagerAdapter != null) {
            return pagerAdapter.count
        }
        return 0
    }

    fun getFirstPage(): Int {
        val count = getCount()
        if (count > 0) {
            return 0
        }
        return -1
    }

    fun getLastPage(): Int {
        val count = getCount()
        if (count > 0) {
            return count - 1
        }
        return -1
    }

    private fun isPaging(event: MotionEvent?): Boolean {

        var horizontal = 0f

        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchPoint.set(event.x, event.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 1) {
                        horizontal = event.x - lastTouchPoint.x
                    }
                }
            }
        }

        if (currentItem == getFirstPage()) {
            // 在第一页不能往左滑
            if (horizontal > 0) {
                return false
            }
        }
        else if (currentItem == getLastPage()) {
            // 在最后一页不能往右滑
            if (horizontal < 0) {
                return false
            }
        }

        return true

    }
}