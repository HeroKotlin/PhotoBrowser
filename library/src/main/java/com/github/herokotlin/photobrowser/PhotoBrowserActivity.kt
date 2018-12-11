package com.github.herokotlin.photobrowser

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.photo_browser_activity.*

class PhotoBrowserActivity: AppCompatActivity() {

    companion object {

        private const val KEY_INDEX = "index"

        private const val KEY_THUMBNAIL_LIST = "thumbnailList"
        private const val KEY_HIGH_QUALITY_LIST = "highQualityList"
        private const val KEY_RAW_LIST = "rawList"

        fun newInstance(context: Context, index: Int, thumbnailList: Array<String>, highQualityList: Array<String>, rawList: Array<String>) {
            val intent = Intent(context, PhotoBrowserActivity::class.java)
            intent.putExtra(KEY_INDEX, index)
            intent.putExtra(KEY_THUMBNAIL_LIST, thumbnailList)
            intent.putExtra(KEY_HIGH_QUALITY_LIST, highQualityList)
            intent.putExtra(KEY_RAW_LIST, rawList)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var flags = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags = flags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        window.decorView.systemUiVisibility = flags

        supportActionBar?.hide()

        setContentView(R.layout.photo_browser_activity)

        val index = intent.getIntExtra(KEY_INDEX, 0)

        val thumbnailList = intent.getStringArrayExtra(KEY_THUMBNAIL_LIST)
        val highQualityList = intent.getStringArrayExtra(KEY_HIGH_QUALITY_LIST)
        val rawList = intent.getStringArrayExtra(KEY_RAW_LIST)

        browserView.callback = object: PhotoBrowserCallback {
            override fun onTap(x: Float, y: Float) {
                this@PhotoBrowserActivity.finish()
            }

            override fun onDownloadSuccess() {
                Toast.makeText(applicationContext, R.string.photo_browser_download_success, Toast.LENGTH_SHORT).show()
            }

            override fun onDownloadFailure() {
                Toast.makeText(applicationContext, R.string.photo_browser_download_failure, Toast.LENGTH_SHORT).show()
            }
        }

        browserView.index = index

        browserView.photos = thumbnailList.mapIndexed { i, s ->
            PhotoModel(
                s,
                highQualityList[i],
                rawList[i]
            )
        }

    }
}