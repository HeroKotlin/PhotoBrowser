package com.github.herokotlin.photobrowser.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Photo (

    // 缩略图
    // 当用户点击一张缩略图打开 photo browser 时，默认显示的站位图
    // 因为一般会共用图片缓存，所以可认为此图无需加载
    val thumbnailUrl: String,

    // 高清图
    // 打开 photo browser 后自动加载的第一张图片
    val highQualityUrl: String,

    // 原图
    // 当高清图加载完成后，如果原图，会显示【查看原图】按钮
    val rawUrl: String,

    // 当前显示的图片
    var currentUrl: String = "",

    // 记录当前是否需要显示查看原图按钮
    var isRawButtonVisible: Boolean = false,

    // 记录当前是否需要显示保存按钮
    var isSaveButtonVisible: Boolean = false,

    // 记录是否加载过高清图
    var isHighQualityPhotoLoaded: Boolean = false,

    // 记录是否加载过原图
    var isRawPhotoLoaded: Boolean = false,

    // 记录当前是否正在拖拽
    var isDragging: Boolean = false,

    // 记录当前的缩放值
    var scale: Float = 1f

): Parcelable